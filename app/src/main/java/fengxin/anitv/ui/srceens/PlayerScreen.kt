package fengxin.anitv.ui.screens

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.PlayerView

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(m3u8Url: String, onBack: () -> Unit) {
    val context = LocalContext.current

    // 监听电视遥控器的“返回键”，按下时退出播放
    BackHandler(onBack = onBack)

    // 初始化 ExoPlayer 实例
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            // 1. 核心！设置防盗链请求头
            val dataSourceFactory = DefaultHttpDataSource.Factory()
                .setAllowCrossProtocolRedirects(true) // 关键修复 1：允许服务器做跳转重定向
                .setDefaultRequestProperties(
                    mapOf(
                        // 伪装成正常的谷歌浏览器
                        "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",

                        // 关键修复 2：告诉视频服务器，我是从你的官方内嵌播放器来的！
                        "Origin" to "https://m3u8.girigirilove.com",
                        "Referer" to "https://m3u8.girigirilove.com/"
                    )
                )

            // 2. 告诉播放器这是一个 HLS (m3u8) 视频流
            val hlsMediaSource = HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(MediaItem.fromUri(m3u8Url))

            // 3. 装载视频并准备播放
            setMediaSource(hlsMediaSource)
            prepare()
            playWhenReady = true // 准备好后自动播放
        }
    }

    // 生命周期管理：当离开这个界面时，释放播放器内存，防止后台继续发声
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 将 ExoPlayer 的画面嵌入到 Compose 中
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = true // 开启默认的播放控制条
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // 电视端特化：确保播放器能获取遥控器焦点
                isFocusable = true
                requestFocus()
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // 播放器背景设为纯黑
    )
}