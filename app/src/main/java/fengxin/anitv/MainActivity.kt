package fengxin.anitv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlin.concurrent.thread

import fengxin.anitv.network.AnimeParser
import fengxin.anitv.ui.screens.HomeScreen
import fengxin.anitv.ui.screens.PlayerScreen

class MainActivity : ComponentActivity() {
    private val TAG = "AniTV"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // 这个变量用来记录当前要播放的视频链接，如果为空，就显示主页
            var currentPlayingUrl by remember { mutableStateOf<String?>(null) }

            if (currentPlayingUrl == null) {
                // --- 显示主页瀑布流 ---
                HomeScreen { clickedAnime ->
                    Log.d(TAG, "你用遥控器点击了: ${clickedAnime.title}")

                    if (clickedAnime.playUrl.isNotEmpty()) {
                        thread {
                            val m3u8Url = AnimeParser.parseM3u8Url(clickedAnime.playUrl)
                            if (m3u8Url != null) {
                                Log.d(TAG, "解析成功！准备播放：$m3u8Url")
                                // 抓取成功后，在主线程更新 UI 状态，触发播放界面
                                //val appleTestStream = "https://ai.girigirilove.net/zijian/oldanime/2026/04/cht/KamiinaBotanYoeruSugatawaYurinoHanaCHT/05/playlist.m3u8"
                                runOnUiThread {
                                    currentPlayingUrl = m3u8Url
                                     //currentPlayingUrl = appleTestStream
                                }
                            } else {
                                Log.e(TAG, "解析失败，无法播放。")
                            }
                        }
                    }
                }
            } else {
                // --- 显示全屏播放器 ---
                // 传入刚才抓取到的 m3u8 直链
                PlayerScreen(
                    m3u8Url = currentPlayingUrl!!,
                    onBack = {
                        // 当用户在播放界面按返回键时，把链接清空，就会自动切回主页
                        currentPlayingUrl = null
                    }
                )
            }
        }
    }
}