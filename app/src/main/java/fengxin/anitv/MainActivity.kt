package fengxin.anitv

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlin.concurrent.thread

// 引入我们的自己写的模块
import fengxin.anitv.network.AnimeParser
import fengxin.anitv.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    private val TAG = "AniTV"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 挂载 Compose 瀑布流界面
        setContent {
            HomeScreen { clickedAnime ->
                Log.d(TAG, "你用遥控器点击了: ${clickedAnime.title}")

                // 如果这个动漫有配置播放链接，我们就去解析它
                if (clickedAnime.playUrl.isNotEmpty()) {
                    // 安卓规定：网络请求必须在子线程运行
                    thread {
                        // 呼叫我们刚刚拆分出去的单例爬虫工具！
                        val m3u8Url = AnimeParser.parseM3u8Url(clickedAnime.playUrl)

                        if (m3u8Url != null) {
                            Log.d(TAG, "解析成功！准备把链接交给播放器去播放：\n$m3u8Url")
                            // TODO: 下一步就是带着这串链接，跳到播放器页面！
                        } else {
                            Log.e(TAG, "解析失败了，可能是链接不对或者防盗链更新了。")
                        }
                    }
                }
            }
        }
    }
}