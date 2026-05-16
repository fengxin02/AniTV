package fengxin.anitv.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text

// 引入刚才分出去的数据模型
import fengxin.anitv.model.Anime
import fengxin.anitv.model.sampleData

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(onAnimeClick: (Anime) -> Unit) {
    LazyColumn (
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
                LazyRow (
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