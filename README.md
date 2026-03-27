# 电竞空间 (Esports Space)

Android 平板电竞游戏桌面启动器，为重度游戏用户打造沉浸式主屏体验。

## 功能特性

- **游戏管理** — 自动扫描/手动添加游戏，分类展示（MOBA、FPS、竞速等），支持置顶与长按菜单
- **设备性能监控** — CPU/GPU 温度与频率实时心电图、RAM/电量/网络延迟面板
- **电竞资讯** — 聚合多源赛事新闻，支持 WebView 详情阅读
- **直播聚合** — 整合斗鱼/虎牙/Bilibili 热门直播，深度链接跳转原生 App 或 WebView 播放
- **数据中心** — 游玩时长热力图、游戏占比饼图、周趋势折线图
- **智能助手 (Agent)** — 基于规则引擎的个性化推荐，Lottie 精灵角色 + 气泡对话

## 技术栈

| 领域 | 技术 |
|---|---|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 依赖注入 | Hilt |
| 本地存储 | Room + DataStore |
| 网络 | Retrofit + Moshi |
| 图片加载 | Coil |
| 动画 | Lottie Compose |
| 后台任务 | WorkManager |
| 导航 | Navigation Compose |

## 架构

多模块架构，共 4 个核心模块 + 6 个功能模块 + 1 个 App 壳模块。

### 模块依赖关系

```
app
├── core-common
├── core-data        ← core-common, core-network
├── core-network     ← core-common
├── core-ui          ← core-common
├── feature-games        ← core-common, core-data, core-network, core-ui
├── feature-news         ← core-common, core-data, core-network, core-ui
├── feature-datacenter   ← core-common, core-data, core-ui
├── feature-livestream   ← core-common, core-data, core-network, core-ui
├── feature-performance  ← core-common, core-data, core-ui
└── feature-agent        ← core-common, core-data, core-ui
```

### 层次结构

```
┌─────────────────────────────────────────┐
│                  app                     │  ← 主入口、导航、主题切换
├─────────────────────────────────────────┤
│  feature-   feature-   feature-   ...   │  ← 各功能独立模块
│  games      news       agent            │
├─────────────────────────────────────────┤
│  core-ui    core-data  core-network     │  ← 共享基础设施
├─────────────────────────────────────────┤
│              core-common                │  ← 通用工具/常量
└─────────────────────────────────────────┘
```

## 项目结构

```
Esports_Space/
├── app/                          # 主应用壳
│   └── src/main/java/.../
│       ├── MainActivity.kt       # 入口 Activity（沉浸式全屏）
│       ├── MainViewModel.kt      # 主题管理
│       ├── EsportsSpaceApp.kt    # Application + WorkManager
│       ├── navigation/
│       │   ├── AppNavigation.kt  # NavHost 路由定义
│       │   └── HomeScreen.kt     # 主屏 Composable（多层叠加）
│       └── di/
│           └── AppModule.kt      # Hilt App 级模块
├── core-common/                  # 通用扩展与常量
├── core-data/                    # Room 数据库、DataStore、Worker
├── core-network/                 # Retrofit API 定义与 DTO
├── core-ui/                      # 主题系统、GlassCard、ECG 背景等
├── feature-games/                # 游戏扫描、分类、展示
├── feature-news/                 # 电竞新闻聚合
├── feature-datacenter/           # 数据统计与可视化
├── feature-livestream/           # 直播聚合与 WebView 播放
├── feature-performance/          # 设备性能实时监控
└── feature-agent/                # 智能推荐助手 + Lottie 精灵
```

## 环境要求

- Android SDK 34 (compileSdk)
- JDK 17
- Android Studio Hedgehog 或更高版本
- 目标设备：Android 平板 (API 26+, 横屏)

## 构建与运行

```bash
# 克隆项目
git clone <repo-url>
cd EsportsSpace

# Debug 构建
./gradlew assembleDebug          # macOS / Linux
.\gradlew.bat assembleDebug      # Windows

# 安装到设备
./gradlew installDebug
```

## 主题系统

内置三套主题，可在运行时切换：

| 主题 ID | 名称 | 特点 |
|---|---|---|
| `galaxy` | 星系辐射 | 游戏海报模糊背景 + 径向布局 |
| `neon_tech` | 霓虹科技 | 深蓝底色 + 青色霓虹高亮 |
| `luxury` | 奢华精密 | 暗金配色 + 交错网格布局 |

## License

Private — All rights reserved.
