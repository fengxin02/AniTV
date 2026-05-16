package fengxin.anitv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlin.concurrent.thread
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import fengxin.anitv.model.AnimeDetail
import fengxin.anitv.model.Episode
import fengxin.anitv.network.AnimeParser
import androidx.compose.foundation.focusable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Border
import androidx.tv.material3.Glow
@Composable
fun DetailScreen(detailUrl: String, onBack: () -> Unit, onPlayEpisode: (Episode) -> Unit) {
    // 监听返回键
    BackHandler(onBack = onBack)

    var animeDetail by remember { mutableStateOf<AnimeDetail?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // 进入页面时，立刻去后台线程抓取详情数据
    LaunchedEffect(detailUrl) {
        thread {
            val result = AnimeParser.fetchAnimeDetail(detailUrl)
            animeDetail = result
            isLoading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF141414))) {
        if (isLoading) {
            // 简单的加载中提示
            Text("正在努力加载详情...", color = Color.White, modifier = Modifier.padding(50.dp))
        } else if (animeDetail == null) {
            Text("加载失败，请按返回键重试", color = Color.Red, modifier = Modifier.padding(50.dp))
        } else {
            val detail = animeDetail!!
            Row(modifier = Modifier.fillMaxSize().padding(32.dp)) {

                // 左侧：海报和简介
                Column(modifier = Modifier.weight(1f).padding(end = 32.dp)) {
                    AsyncImage(
                        model = detail.coverUrl,
                        contentDescription = "海报",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.width(200.dp).height(300.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = detail.title,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // 优化的简介显示：增加行高，并支持遥控器向下滚动浏览长文
                    Text(
                        text = detail.description,
                        color = Color.LightGray,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .focusable() // ✨【极其关键】加上这行，遥控器才能选中这段字往下按！
                    )
                }

                // 右侧：选集网格列表 (支持多线路分类)
                Column(modifier = Modifier.weight(2f)) {
                    Text("选集播放", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))

                    // 用 LazyColumn 支持整个右侧区域的上下滑动
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        detail.playlists.forEach { playlist ->

                            // 1. 渲染线路标题 (例如 "繁中", "简中")
                            item {
                                Text(
                                    text = "▶ ${playlist.name}",
                                    color = Color(0xFFE50914),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(top = 12.dp, bottom = 8.dp)
                                )
                            }

                            // 2. 将这一组集数按“每行 4 个”切块
                            val chunkedEpisodes = playlist.episodes.chunked(4)

                            items(chunkedEpisodes.size) { rowIndex ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.padding(bottom = 12.dp)
                                ) {
                                    chunkedEpisodes[rowIndex].forEach { episode ->
                                        Button(
                                            onClick = { onPlayEpisode(episode) },
                                            // 固定宽度，让排版像强迫症一样整齐
                                            modifier = Modifier.width(80.dp),

                                            colors = ButtonDefaults.colors(
                                                // 1. 普通状态：使用你喜欢的那张图里的深灰色 (#323232)
                                                containerColor = Color(0xFF323232),
                                                contentColor = Color.White,

                                                // 2. 选中（获焦）状态：变成纯白色，文字变黑，对比度极高，一眼就能看出在哪
                                                focusedContainerColor = Color.White,
                                                focusedContentColor = Color.Black,

                                                // 3. 按下状态：稍微深一点的灰色
                                                pressedContainerColor = Color.LightGray,
                                                pressedContentColor = Color.Black
                                            )

                                        ) {
                                            // 注意：内部 Text 的颜色已经由 colors 统一接管了，不需要再单独写 Color.White
                                            Text(text = episode.title)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}