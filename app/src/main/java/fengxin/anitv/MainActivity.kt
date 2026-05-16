package fengxin.anitv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import fengxin.anitv.model.sampleData
import kotlin.concurrent.thread

import fengxin.anitv.network.AnimeParser
import fengxin.anitv.ui.screens.HomeScreen
import fengxin.anitv.ui.screens.PlayerScreen
import fengxin.anitv.model.Category
import fengxin.anitv.model.sampleData
import androidx.compose.runtime.LaunchedEffect
class MainActivity : ComponentActivity() {
    private val TAG = "AniTV"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            var currentPlayingUrl by remember { mutableStateOf<String?>(null) }
            // 新增：保存首页分类数据的变量
            var homeCategories by remember { mutableStateOf<List<Category>>(sampleData) }

            // 新增：App 启动时，开启协程去抓取首页数据
            LaunchedEffect(Unit) {
                thread {
                    val realData = AnimeParser.fetchHomePage()
                    if (realData.isNotEmpty()) {
                        // 抓到了，回到主线程更新 UI
                        runOnUiThread { homeCategories = realData }
                    }
                }
            }

            if (currentPlayingUrl == null) {
                // 把动态数据传给 HomeScreen
                HomeScreen(categories = homeCategories) { clickedAnime ->
                    Log.d(TAG, "你点击了: ${clickedAnime.title}, 详情页是: ${clickedAnime.detailUrl}")

                    // TODO: 注意！现在我们点的是主页海报，拿到的是【详情页】(比如 /bangumi/123.html)
                    // 而不是之前的【播放页】(比如 /playGV123-1-1.html)
                    // 所以点击后，我们需要写个新方法，去详情页里把真实的播放页找出来，再调用 parseM3u8Url
                }
            } else {
                PlayerScreen(
                    m3u8Url = currentPlayingUrl!!,
                    onBack = { currentPlayingUrl = null }
                )
            }
        }
    }
}