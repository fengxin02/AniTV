package fengxin.anitv.network

import android.util.Base64
import android.util.Log
import fengxin.anitv.model.Anime
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import java.net.URLDecoder
import java.util.regex.Pattern
import fengxin.anitv.model.Category

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
}