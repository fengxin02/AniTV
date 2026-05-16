package fengxin.anitv.network

import android.util.Base64
import android.util.Log
import fengxin.anitv.model.Anime
import fengxin.anitv.model.AnimeDetail
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.util.regex.Pattern
import fengxin.anitv.model.Category
import fengxin.anitv.model.Episode
import fengxin.anitv.model.Playlist

// 使用 object 关键字，让它成为一个可以随时调用的单例工具类
object AnimeParser {
    private const val TAG = "AnimeParser"
    // 复用同一个 OkHttpClient 可以节省资源和时间
    private val client = OkHttpClient()

    // 传入播放页 URL，返回解析好的 m3u8 直链（如果失败返回 null）
    fun parseM3u8Url(targetUrl: String): String? {
        Log.d(TAG, "🚀 开始解析视频直链: $targetUrl")
        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
            .header("Referer", "https://ani.girigirilove.com/")
            .build()

        try {
            val response = client.newCall(request).execute()
            val htmlContent = response.body?.string() ?: return null

            val playerPattern = Pattern.compile("(?:player_aaaa|mac_player_info)\\s*=\\s*(\\{.*)")
            val playerMatcher = playerPattern.matcher(htmlContent)

            if (playerMatcher.find()) {
                val playerStr = playerMatcher.group(1) ?: ""
                val urlMatcher = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(playerStr)
                val encryptMatcher = Pattern.compile("\"encrypt\"\\s*:\\s*(\\d)").matcher(playerStr)

                if (urlMatcher.find()) {
                    val rawUrl = urlMatcher.group(1)?.replace("\\/", "/") ?: ""
                    val encryptType = if (encryptMatcher.find()) encryptMatcher.group(1)?.toInt() ?: 0 else 0

                    var decryptedUrl = rawUrl
                    if (encryptType == 1) {
                        decryptedUrl = URLDecoder.decode(rawUrl, "UTF-8")
                    } else if (encryptType == 2) {
                        val base64Bytes = Base64.decode(rawUrl, Base64.DEFAULT)
                        decryptedUrl = URLDecoder.decode(String(base64Bytes, Charsets.UTF_8), "UTF-8")
                    }

                    if (decryptedUrl.contains("url=")) {
                        decryptedUrl = URLDecoder.decode(decryptedUrl.substringAfter("url="), "UTF-8")
                    }

                    Log.d(TAG, "🎯 成功提取 m3u8 直链：\n$decryptedUrl")
                    return decryptedUrl // 成功！把链接返回出去
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 请求发生错误: ${e.message}")
        }
        return null // 失败了返回 null
    }

    fun fetchHomePage(): List<Category> {
        val targetUrl = "https://ani.girigirilove.com/"
        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
            .build()

        val categoryList = mutableListOf<Category>()

        try {
            val response = client.newCall(request).execute()
            val htmlContent = response.body?.string() ?: return emptyList()

            val document = Jsoup.parse(htmlContent)

            // 1. 精准抓取包裹着分类的大框 (根据源码，是 .box-width)
            val categoryBlocks = document.select(".box-width")

            for (block in categoryBlocks) {
                // 2. 精准抓取分类标题
                val categoryTitle = block.select(".title-h").text()
                if (categoryTitle.isEmpty()) continue

                val animeList = mutableListOf<Anime>()

                // 3. 精准抓取每一部动漫卡片
                val items = block.select(".public-list-box")

                for (item in items) {
                    // 抓取 <a> 标签
                    val aTag = item.select("a.public-list-exp")
                    if (aTag.isEmpty()) continue

                    // 提取名字和详情页链接
                    val title = aTag.attr("title")
                    val detailUrl = aTag.attr("href")

                    // 提取海报图片：注意破解懒加载，优先抓取 data-src！
                    val imgTag = item.select("img")
                    var coverUrl = imgTag.attr("data-src")
                    if (coverUrl.isEmpty()) {
                        coverUrl = imgTag.attr("src")
                    }

                    if (title.isNotEmpty() && detailUrl.isNotEmpty() && coverUrl.isNotEmpty()) {
                        // 补全前面的域名
                        val fullDetailUrl = if (detailUrl.startsWith("http")) detailUrl else "https://ani.girigirilove.com$detailUrl"
                        val fullCoverUrl = if (coverUrl.startsWith("http")) coverUrl else "https://ani.girigirilove.com$coverUrl"

                        animeList.add(Anime(title, fullCoverUrl, fullDetailUrl))
                    }
                }

                // 如果这个分类下有动漫，加入总列表
                if (animeList.isNotEmpty()) {
                    categoryList.add(Category(categoryTitle, animeList))
                }
            }

            Log.d(TAG, "🎉 首页数据抓取成功！共抓到 ${categoryList.size} 个分类")
            categoryList.forEach {
                Log.d(TAG, "📺 分类: ${it.title} -> 包含 ${it.animeList.size} 部动漫")
            }

        } catch (e: Exception) {
            Log.e(TAG, "💥 首页抓取发生错误: ${e.message}")
        }

        return categoryList
    }


    // 新增：抓取详情页和选集列表
    fun fetchAnimeDetail(detailUrl: String): AnimeDetail? {
        Log.d(TAG, "🚀 开始抓取详情页: $detailUrl")
        val request = Request.Builder()
            .url(detailUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
            .build()

        try {
            val response = client.newCall(request).execute()
            val htmlContent = response.body?.string() ?: return null
            val document = Jsoup.parse(htmlContent)

            // 1. 提取标题 (加入了新暗号 .slide-info-title)
            // 1. 提取标题 (精准提取第一个，并且抛弃容易误伤的 .title 类名)
            var title = document.select(".slide-info-title, .module-info-heading h1").first()?.text()

            // 终极兜底方案：如果上面没抓到，直接从网页最顶部的 <title> 标签里切出名字
            if (title.isNullOrEmpty()) {
                title = document.select("title").text().substringBefore("_").substringBefore("-").trim()
            }
            if (title.isEmpty()) title = "未知标题"

            // 2. 提取海报图片 (加入了新暗号 .detail-pic img)
            var coverUrl = document.select(".module-info-poster img, .stui-content__thumb img, .picture img, .detail-pic img").attr("data-src")
            if (coverUrl.isEmpty()) coverUrl = document.select(".module-info-poster img, .stui-content__thumb img, .detail-pic img").attr("src")
            val fullCoverUrl = if (coverUrl.startsWith("http") || coverUrl.isEmpty()) coverUrl else "https://ani.girigirilove.com$coverUrl"

            // 3. 提取剧情简介 (精准狙击，防止把网页按钮文字吸进来)
            var description = document.select("#height_limit").text()
            if (description.isEmpty()) {
                // 如果没找到，再尝试其他备用方案，并使用 first() 确保只抓取第一块纯净文本
                description = document.select(".module-info-introduction, .stui-content__detail .desc").first()?.text() ?: "暂无简介"
            }

            // 4. 提取播放列表（按线路/语言分类打包）
            val playlists = mutableListOf<Playlist>()
            val tabElements = document.select(".anthology-tab a")
            val boxElements = document.select(".anthology-list-box")

            // 检查是不是多线路结构 (有标签，且标签数量等于列表框数量)
            if (tabElements.isNotEmpty() && tabElements.size == boxElements.size) {
                for (i in tabElements.indices) {
                    // ownText() 可以完美避开 <span> 里的数字，只拿 "繁中" 两个字
                    val tabName = tabElements[i].ownText().replace(Regex("[^\\u4e00-\\u9fa5a-zA-Z0-9]"), "").trim()
                    val finalTabName = if (tabName.isEmpty()) "播放列表 ${i + 1}" else tabName

                    val episodes = mutableListOf<Episode>()
                    // 只抓取对应这个标签的盒子里的 a 标签
                    val aTags = boxElements[i].select("ul li a")
                    for (aTag in aTags) {
                        val epTitle = aTag.text()
                        val epPlayUrl = aTag.attr("href")
                        if (epTitle.isNotEmpty() && epPlayUrl.contains("play")) {
                            val fullPlayUrl = if (epPlayUrl.startsWith("http")) epPlayUrl else "https://ani.girigirilove.com$epPlayUrl"
                            episodes.add(Episode(epTitle, fullPlayUrl))
                        }
                    }
                    if (episodes.isNotEmpty()) {
                        playlists.add(Playlist(finalTabName, episodes))
                    }
                }
            } else {
                // 终极兜底：如果有些老番没有分繁简，只有一个列表，就沿用老方法
                val episodes = mutableListOf<Episode>()
                val playList = document.select(".module-play-list a, .stui-content__playlist a, .anthology-list-play a")
                for (aTag in playList) {
                    val epTitle = aTag.text()
                    val epPlayUrl = aTag.attr("href")
                    if (epTitle.isNotEmpty() && epPlayUrl.contains("play")) {
                        val fullPlayUrl = if (epPlayUrl.startsWith("http")) epPlayUrl else "https://ani.girigirilove.com$epPlayUrl"
                        episodes.add(Episode(epTitle, fullPlayUrl))
                    }
                }
                if (episodes.isNotEmpty()) {
                    playlists.add(Playlist("默认播放", episodes))
                }
            }

            Log.d(TAG, "🎉 详情抓取成功！共找到 ${playlists.size} 个播放列表")
            return AnimeDetail(title, fullCoverUrl, description, playlists)

        } catch (e: Exception) {
            Log.e(TAG, "💥 详情页抓取发生错误: ${e.message}")
        }
        return null
    }
}