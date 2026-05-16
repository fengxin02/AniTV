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
import fengxin.anitv.model.Anime
import fengxin.anitv.ui.screens.DetailScreen
import fengxin.anitv.ui.screens.SearchScreen


enum class ScreenState { HOME, SEARCH, DETAIL, PLAYER }
class MainActivity : ComponentActivity() {
    private val TAG = "AniTV"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // 控制当前显示哪个界面的状态
            var currentScreen by remember { mutableStateOf(ScreenState.HOME) }

            // 记录选中的数据
            var homeCategories by remember { mutableStateOf<List<Category>>(sampleData) }
            var selectedAnime by remember { mutableStateOf<Anime?>(null) }
            var currentPlayingUrl by remember { mutableStateOf<String?>(null) }

            // 启动时抓取首页
            LaunchedEffect(Unit) {
                thread {
                    val realData = AnimeParser.fetchHomePage()
                    if (realData.isNotEmpty()) {
                        runOnUiThread { homeCategories = realData }
                    }
                }
            }

            // 根据状态切换 UI
            when (currentScreen) {
                ScreenState.HOME -> {
                    HomeScreen(categories = homeCategories, onSearchClick = { currentScreen = ScreenState.SEARCH }) { clickedAnime ->
                        Log.d(TAG, "点击了海报，进入详情页: ${clickedAnime.detailUrl}")
                        selectedAnime = clickedAnime
                        currentScreen = ScreenState.DETAIL // 切换到详情页
                    }
                }
                ScreenState.SEARCH -> {
                    SearchScreen(
                        onBack = { currentScreen = ScreenState.HOME }, // 返回退回主页
                        onAnimeClick = { clickedAnime ->
                            selectedAnime = clickedAnime
                            currentScreen = ScreenState.DETAIL // 在搜索页点击海报，直接跳详情页！
                        }
                    )
                }

                ScreenState.DETAIL -> {
                    DetailScreen(
                        detailUrl = selectedAnime!!.detailUrl,
                        onBack = { currentScreen = ScreenState.HOME }, // 返回首页
                        onPlayEpisode = { episode ->
                            Log.d(TAG, "点击了集数: ${episode.title}, 去解析播放直链...")
                            thread {
                                // 呼叫我们最初写的最核心的爬虫！
                                val m3u8Url = AnimeParser.parseM3u8Url(episode.playUrl)
                                if (m3u8Url != null) {
                                    runOnUiThread {
                                        currentPlayingUrl = m3u8Url
                                        currentScreen = ScreenState.PLAYER // 解析成功，切换到播放页
                                    }
                                } else {
                                    Log.e(TAG, "解析 m3u8 失败！")
                                }
                            }
                        }
                    )
                }

                ScreenState.PLAYER -> {
                    PlayerScreen(
                        m3u8Url = currentPlayingUrl!!,
                        onBack = {
                            currentPlayingUrl = null
                            currentScreen = ScreenState.DETAIL // 从播放器返回，退回到详情页
                        }
                    )
                }
            }
        }
    }
}