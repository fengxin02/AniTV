package fengxin.anitv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import fengxin.anitv.model.AnimeDetail
import fengxin.anitv.model.Episode
import fengxin.anitv.network.AnimeParser
import kotlin.concurrent.thread

// 主题色定义
private val Background = Color(0xFF0D1117)
private val Surface = Color(0xFF161B22)
private val SurfaceBorder = Color(0xFF30363D)
private val Accent = Color(0xFFF78166)
private val AccentPressed = Color(0xFFE06B50)
private val TextPrimary = Color(0xFFF0F6FC)
private val TextSecondary = Color(0xFFC9D1D9)
private val TextMuted = Color(0xFF8B949E)
private val BtnDefaultBg = Color(0xFF21262D)
private val BtnDefaultText = Color(0xFFC9D1D9)

@Composable
fun DetailScreen(detailUrl: String, onBack: () -> Unit, onPlayEpisode: (Episode) -> Unit) {
    BackHandler(onBack = onBack)

    var animeDetail by remember { mutableStateOf<AnimeDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(detailUrl) {
        thread {
            val result = AnimeParser.fetchAnimeDetail(detailUrl)
            animeDetail = result
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Background)) {
        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("正在加载详情...", color = TextMuted, fontSize = 20.sp)
                }
            }
            animeDetail == null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("加载失败", color = Accent, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("请按返回键重试", color = TextMuted, fontSize = 16.sp)
                    }
                }
            }
            else -> {
                val detail = animeDetail!!
                Row(modifier = Modifier.fillMaxSize().padding(40.dp)) {

                    // ═══════════════════════════════════
                    // 左侧：海报 + 标题（固定不动）
                    // ═══════════════════════════════════
                    Column(
                        modifier = Modifier.weight(0.28f).padding(end = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AsyncImage(
                            model = detail.coverUrl,
                            contentDescription = "海报",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(220.dp)
                                .height(320.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = detail.title,
                            color = TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // ═══════════════════════════════════
                    // 右侧：简介 + 剧集列表（整体可滚动）
                    // ═══════════════════════════════════
                    LazyColumn(
                        modifier = Modifier.weight(0.72f).fillMaxHeight()
                    ) {
                        // ── 简介标题 ──
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "▎",
                                    color = Accent,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "剧情简介",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                        }

                        // ── 简介内容卡片 ──
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
                                    .background(Surface)
                                    .padding(horizontal = 20.dp, vertical = 18.dp)
                            ) {
                                Text(
                                    text = detail.description,
                                    color = TextSecondary,
                                    fontSize = 16.sp,
                                    lineHeight = 28.sp
                                )
                            }
                        }

                        // ── 分隔区域 ──
                        item {
                            Spacer(modifier = Modifier.height(32.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(SurfaceBorder)
                            )
                            Spacer(modifier = Modifier.height(28.dp))
                        }

                        // ── 剧集标题 ──
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "▎",
                                    color = Accent,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "选集播放",
                                    color = TextPrimary,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(18.dp))
                        }

                        // ── 各线路剧集网格 ──
                        detail.playlists.forEach { playlist ->
                            item {
                                Text(
                                    text = "▶ ${playlist.name}",
                                    color = TextMuted,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 12.dp)
                                )
                            }

                            val chunkedEpisodes = playlist.episodes.chunked(5)
                            items(chunkedEpisodes.size) { rowIndex ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.padding(bottom = 10.dp)
                                ) {
                                    chunkedEpisodes[rowIndex].forEach { episode ->
                                        Button(
                                            onClick = { onPlayEpisode(episode) },
                                            modifier = Modifier
                                                .width(88.dp)
                                                .height(42.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            colors = ButtonDefaults.colors(
                                                containerColor = BtnDefaultBg,
                                                contentColor = BtnDefaultText,
                                                focusedContainerColor = Accent,
                                                focusedContentColor = Color.White,
                                                pressedContainerColor = AccentPressed,
                                                pressedContentColor = Color.White
                                            )
                                        ) {
                                            Text(
                                                text = episode.title,
                                                fontSize = 13.sp,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }

                            // 线路之间的间距
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        // 底部留白，避免最后一行被边缘遮挡
                        item { Spacer(modifier = Modifier.height(48.dp)) }
                    }
                }
            }
        }
    }
}
