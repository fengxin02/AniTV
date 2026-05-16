package fengxin.anitv.network

import android.util.Base64
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLDecoder
import java.util.regex.Pattern

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
}