package fengxin.anitv.model

data class Anime(
    val title: String,
    val coverUrl: String, // 海报图片的网址
    val detailUrl: String // 动漫详情页的网址 (比如 /bangumi/123.html)
)

data class Category(val title: String, val animeList: List<Anime>)

// 兜底用的假数据
val sampleData = listOf(
    Category("加载中...", listOf(
        Anime("稍等片刻", "", "")
    ))
)