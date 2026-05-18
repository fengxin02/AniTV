##  AniTV - Android TV 智能原生影视聚合客户端
一个专门为大屏智能电视（Android TV / Box）打造的流媒体聚合客户端。基于最新的 Jetpack Compose 现代 UI 框架开发，底层搭载大屏最强音视频解码引擎 Google Media3 ExoPlayer

<img width="1920" height="1080" alt="Screenshot_20260518_135250" src="https://github.com/user-attachments/assets/5a1f8c3f-d06c-48f9-abe3-e49cc979409b" />

---

##  核心亮点与工程架构

本项目严格遵循现代 Android 开源工程的标准进行模块化重构 **三层架构（MVC）**：

*  爬虫 (`network/`)：基于 `Jsoup` 精准提取目标站点的 HTML 节点。
*  多线路分类网格系统 (`model/ & ui/`)：高智商解析繁体、简体等多条独立播放线路，采用 Compose 的数组切块排版术（`chunked(4)`），完美呈现原生电视选集网格。
*  电视特化遥控器交互 (`ui/screens/`)：完美移除手机端触摸依赖。赋予界面元素 `focusable()` 灵魂。动漫海报瀑布流、剧情简介长文本均全面支持遥控器焦点选中与平滑滚动浏览。

---

##  技术栈与依赖库

本项目全部使用 **Kotlin** 语言编写，其核心依赖声明在 `app/build.gradle.kts` 中：

* **UI 框架**：`Compose BOM` (2023.10.01 兼容版本)
* **电视组件**：`TV Material` & `TV Foundation` (Alpha11)
* **网络请求**：`OkHttp`
* **HTML 解析**：`Jsoup`
* **图片加载**：`Coil Compose`
* **播放内核**：`Media3 ExoPlayer` & `Media3 UI` & `Media3 ExoPlayer HLS`

---

##  项目目录树结构

```text
app/src/main/java/fengxin/anitv/
│
├── MainActivity.kt          # 应用总调度器，控制 HOME/DETAIL/PLAYER 页面状态机
│
├── model/
│   └── Anime.kt             # 数据核心层：定义 Anime、Category、Playlist 关系模型
│
├── network/
│   └── AnimeParser.kt       # 核心引擎层：包含首页多雷达盲猜、详情页提纯、m3u8 智能解密
│
└── ui/
    ├── theme/               # 主题特化层：定制暗黑沉浸式电视配色方案
    └── screens/             # 视图表现层
        ├── HomeScreen.kt    # 首页：推荐动漫海报瀑布流海报墙
        ├── DetailScreen.kt  # 详情页：左侧可滚动简介 + 右侧多线路选集网格
        └── PlayerScreen.kt  # 播放页：全屏包裹的 ExoPlayer (Media3) 原生大屏解码器
```

## 电视端安装与调试指南

 *  Releases里面下载apk，使用u盘安装到电视即可
---

##  隐私与合规性说明
*  应用在 `AndroidManifest.xml` 中已依法声明 `<uses-permission android:name="android.permission.INTERNET" />` 联网权限

---
## 免责
* 本项目（AniTV）仅作为一个大屏幕安卓电视端（Android TV）的技术探索与个人学习开源示例。应用内所有核心功能、重构逻辑及架构设计，均用于测试最新的 Jetpack Compose 框架、Media3 播放组件以及 Jsoup 节点解析技术

* 本项目承诺不保存任何第三方用户信息

* 本项目代码仅供学习交流，不得用于商业用途，若侵权请联系

* 应用本身不创建、不托管、不存储任何视频、音频、图片或文字内容。所有界面中动态加载呈现的影视数据（包括但不限于动漫名称、海报封面、剧情简介、播放线路等），均实时、公开引用自互联网第三方源站
* 本项目的代码开源基于 AGPLv3 国际协议；在非商业领域，任何团队或个人不得将本项目用于商业牟利。
* 本项目的网页解析逻辑完全基于公开引用资源。如果源站认为相关引用涉及侵权，请联系源站的版权利害关系人进行下架处理。一旦源站内容更新或关闭，本项目的公开引用节点亦将同步失效
* 任何下载、编译、安装并运行本项目的用户，均视为已完全知晓并同意本声明。因用户将本项目代码用于非学习目的而引发的任何形式的版权纠纷或法律责任，均由使用者本人承担，与本项目发起人及开源贡献者无关
* 本程序开发过程中使用了 AI 大语言模型，使用此软件风险自负
