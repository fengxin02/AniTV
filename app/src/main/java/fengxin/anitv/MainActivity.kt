package fengxin.anitv

import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.foundation.lazy.list.TvLazyRow
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.util.regex.Pattern
import kotlin.concurrent.thread

// --- 数据模型 ---
data class Anime(val id: String, val title: String, val playUrl: String)
data class Category(val title: String, val animeList: List<Anime>)

// --- 假数据 (包含了我们要测试的那个网页链接) ---
val sampleData = listOf(
    Category("我的追番", listOf(
        Anime("GV26992", "神奈牡丹 第5集", "https://ani.girigirilove.com/playGV26992-1-5/")
    )),
    Category("近期热门", (1..10).map { Anime("h$it", "热门动漫 $it", "") }),
    Category("热血战斗", (1..10).map { Anime("a$it", "战斗番 $it", "") })
)

class MainActivity : ComponentActivity() {
    private val TAG = "AniTV"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 挂载 Compose 瀑布流界面
        setContent {
            HomeScreen { clickedAnime ->
                // 当用户点击卡片时，触发这里的代码
                Log.d(TAG, "你用遥控器点击了: ${clickedAnime.title}")
                if (clickedAnime.playUrl.isNotEmpty()) {
                    thread { runSpider(clickedAnime.playUrl) }
                }
            }
        }
    }

    // --- 我们的核心爬虫引擎 ---
    private fun runSpider(targetUrl: String) {
        Log.d(TAG, "🚀 开始解析视频直链: $targetUrl")
        val client = OkHttpClient()
        val request = Request.Builder()
            .url(targetUrl)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Chrome/120.0.0.0 Safari/537.36")
            .header("Referer", "https://ani.girigirilove.com/")
            .build()

        try {
            val response = client.newCall(request).execute()
            val htmlContent = response.body?.string() ?: return

            val playerPattern = Pattern.compile("(?:player_aaaa|mac_player_info)\\s*=\\s*(\\{.*)")
            val playerMatcher = playerPattern.matcher(htmlContent)

            if (playerMatcher.find()) {
                val playerStr = playerMatcher.group(1) ?: ""
                val urlMatcher = Pattern.compile("\"url\"\\s*:\\s*\"([^\"]+)\"").matcher(playerStr)
                val encryptMatcher = Pattern.compile("\"encrypt\"\\s*:\\s*(\\d)").matcher(playerStr)

                if (urlMatcher.find()) {
                    var rawUrl = urlMatcher.group(1)?.replace("\\/", "/") ?: ""
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
                    // TODO: 下一步，我们将在这里把链接传给 ExoPlayer 进行播放！
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "💥 请求发生错误: ${e.message}")
        }
    }
}

// --- Compose UI 界面 ---
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(onAnimeClick: (Anime) -> Unit) {
    TvLazyColumn(
        modifier = Modifier.fillMaxSize().background(Color(0xFF141414)).padding(top = 24.dp, bottom = 24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        items(sampleData.size) { index ->
            val category = sampleData[index]
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = category.title,
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 32.dp, bottom = 12.dp)
                )
                TvLazyRow(
                    contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(category.animeList.size) { animeIndex ->
                        val anime = category.animeList[animeIndex]
                        Card(
                            onClick = { onAnimeClick(anime) }, // 绑定点击事件
                            modifier = Modifier.width(150.dp).height(220.dp)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize().background(Color(0xFF2F2F2F)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = anime.title, color = Color.LightGray, modifier = Modifier.padding(8.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}
