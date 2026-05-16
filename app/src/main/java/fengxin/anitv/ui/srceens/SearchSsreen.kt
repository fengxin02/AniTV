package fengxin.anitv.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Card
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import coil.compose.AsyncImage
import kotlin.concurrent.thread

import fengxin.anitv.model.Anime
import fengxin.anitv.network.AnimeParser

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(onBack: () -> Unit, onAnimeClick: (Anime) -> Unit) {
    BackHandler(onBack = onBack)

    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<Anime>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    var hasSearched by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF141414)).padding(32.dp)) {

        // 搜索框
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("在此输入动漫名称或关键字...", color = Color.Gray) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(
                onSearch = {
                    if (searchQuery.isNotBlank()) {
                        isSearching = true
                        hasSearched = false
                        // 直接用子线程去调取轻量级的 AJAX 接口
                        thread {
                            val results = AnimeParser.searchAnime(searchQuery)
                            searchResults = results
                            hasSearched = true
                            isSearching = false
                        }
                    }
                }
            ),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.DarkGray
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 搜索状态与结果展示
        if (isSearching) {
            Text("🚀 正在通过隐秘通道搜索...", color = Color.White, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else if (hasSearched && searchResults.isEmpty()) {
            Text("😢 没有找到相关动漫，换个关键字试试吧？", color = Color.Gray, fontSize = 20.sp, modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(searchResults.size) { index ->
                    val anime = searchResults[index]
                    Card(
                        onClick = { onAnimeClick(anime) },
                        modifier = Modifier.width(140.dp).height(200.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(Color(0xFF2F2F2F))) {
                            AsyncImage(
                                model = anime.coverUrl,
                                contentDescription = anime.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(modifier = Modifier.fillMaxSize().background(Color(0x88000000)))
                            Text(
                                text = anime.title,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                modifier = Modifier.align(Alignment.BottomStart).padding(8.dp),
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}