package fengxin.anitv.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import fengxin.anitv.model.Anime
import fengxin.anitv.model.sampleData
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import fengxin.anitv.model.Category

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
// 注意这里：我们将外部传入的动态数据替换掉原来的 sampleData
fun HomeScreen(categories: List<Category>, onSearchClick: () -> Unit, onAnimeClick: (Anime) -> Unit) {
    LazyColumn (
        modifier = Modifier.fillMaxSize().background(Color(0xFF141414)).padding(top = 24.dp, bottom = 24.dp),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
            var isFocused by remember { mutableStateOf(false) }
            val scale by animateFloatAsState(
                targetValue = if (isFocused) 1.04f else 1f,
                animationSpec = tween(durationMillis = 200)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 24.dp)
                    .height(44.dp)
                    .scale(scale)
                    .onFocusChanged { isFocused = it.isFocused }
                    .clickable(onClick = onSearchClick)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isFocused) {
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(20.dp)
                            .background(Color.White.copy(alpha = 0.8f))
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = "搜索动漫",
                    fontSize = 16.sp,
                    color = if (isFocused) Color.White else Color.White.copy(alpha = 0.4f)
                )
            }
        }
        items(categories.size) { index ->
            val category = categories[index]
            Column(modifier = Modifier.padding(bottom = 24.dp)) {
                Text(
                    text = category.title,
                    color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 32.dp, bottom = 12.dp)
                )
                LazyRow (
                    contentPadding = PaddingValues(start = 32.dp, end = 32.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(category.animeList.size) { animeIndex ->
                        val anime = category.animeList[animeIndex]
                        Card(
                            onClick = { onAnimeClick(anime) },
                            modifier = Modifier.width(150.dp).height(220.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2F2F2F))) {
                                if (anime.coverUrl.isNotEmpty()) {
                                    AsyncImage(
                                        model = anime.coverUrl,
                                        contentDescription = anime.title,
                                        contentScale = ContentScale.Crop, // 自动裁剪填充卡片
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                                // 底部加上半透明的黑色渐变，防止图片太亮看不清字
                                Box(modifier = Modifier.fillMaxSize().background(Color(0x66000000)))

                                // 动漫名字显示在左下角
                                Text(
                                    text = anime.title,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}