package fengxin.anitv.model

data class Anime(
    val title: String,
    val coverUrl: String, // 海报图片的网址
    val detailUrl: String // 动漫详情页的网址 (比如 /bangumi/123.html)
)

data class Category(val title: String, val animeList: List<Anime>)
data class Playlist(val name: String, val episodes: List<Episode>)
data class Episode(val title: String, val playUrl: String)

// 新增：动漫详情页数据模型
data class AnimeDetail(
    val title: String,
    val coverUrl: String,
    val description: String,     // 剧情简介
    val playlists: List<Playlist>  // 所有的集数列表
)

// 兜底用的假数据
val sampleData = listOf(
    Category("加载中...", listOf(
        Anime("稍等片刻", "", "")
    ))
)