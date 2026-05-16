package fengxin.anitv.model

// --- 数据模型 ---
data class Anime(val id: String, val title: String, val playUrl: String)
data class Category(val title: String, val animeList: List<Anime>)

// --- 假数据 (以后我们会用网络请求真实数据替换掉它) ---
val sampleData = listOf(
    Category("我的追番", listOf(
        Anime("GV26992", "神奈牡丹 第5集", "https://ani.girigirilove.com/playGV26992-1-5/")
    )),
    Category("近期热门", (1..10).map { Anime("h$it", "热门动漫 $it", "") }),
    Category("热血战斗", (1..10).map { Anime("a$it", "战斗番 $it", "") })
)