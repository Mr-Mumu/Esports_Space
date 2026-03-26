# 电竞空间（Esports Space）实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个 Android 平板电竞沉浸式 Launcher 桌面，包含游戏入口、资讯、数据中心、直播、性能监控和智能 Agent 桌面精灵。

**Architecture:** Launcher + 轻量云服务混合方案。多模块 Android 工程，Hilt 依赖注入，ViewModel + StateFlow 状态管理，Room 本地存储，Retrofit 网络通信。Agent 采用本地规则引擎 + 云端增强。

**Tech Stack:** Kotlin, Android SDK (minSdk 26), Jetpack Compose, Hilt, Room, Retrofit + OkHttp, Lottie, Kotlin Coroutines + Flow, DataStore

**Spec:** `docs/superpowers/specs/2026-03-26-esports-space-design.md`

---

## 文件结构总览

```
Esports_Space/
├── build.gradle.kts                          # 根构建文件
├── settings.gradle.kts                       # 模块注册
├── gradle.properties                         # Gradle 配置
├── app/
│   ├── build.gradle.kts
│   ├── src/main/
│   │   ├── AndroidManifest.xml               # Launcher intent-filter + 权限声明
│   │   ├── java/com/esports/space/
│   │   │   ├── EsportsSpaceApp.kt            # Application 类（Hilt + WorkManager 入口）
│   │   │   ├── MainActivity.kt               # LauncherActivity（CATEGORY_HOME）
│   │   │   ├── MainViewModel.kt              # 主界面 ViewModel
│   │   │   ├── navigation/
│   │   │   │   └── AppNavigation.kt          # 导航图
│   │   │   └── di/
│   │   │       └── AppModule.kt              # 应用级 Hilt Module
│   │   └── res/
│   │       ├── values/themes.xml
│   │       └── drawable/                     # 图标资源
│   └── src/test/                             # 单元测试
│
├── core-common/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/common/
│       ├── util/TimeUtils.kt                 # 时间工具
│       ├── util/PermissionHelper.kt          # 权限请求工具
│       ├── util/ForegroundAppTracker.kt      # 前台应用检测（UsageStatsManager）
│       └── result/Result.kt                  # 统一结果封装
│
├── core-data/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/data/
│       ├── db/
│       │   ├── EsportsDatabase.kt            # Room 数据库
│       │   ├── entity/GameRecordEntity.kt
│       │   ├── entity/PlaySessionEntity.kt
│       │   ├── entity/DeviceSnapshotEntity.kt
│       │   ├── entity/AgentEventEntity.kt
│       │   ├── dao/GameRecordDao.kt
│       │   ├── dao/PlaySessionDao.kt
│       │   ├── dao/DeviceSnapshotDao.kt
│       │   ├── dao/AgentEventDao.kt
│       │   └── DataCleanupWorker.kt          # 数据清理 WorkManager
│       ├── datastore/
│       │   └── UserPreferenceStore.kt        # DataStore 偏好
│       └── di/
│           └── DataModule.kt                 # 数据层 Hilt Module
│
├── core-network/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/network/
│       ├── api/
│       │   ├── NewsApi.kt                    # 资讯 API 接口
│       │   ├── LiveApi.kt                    # 直播 API 接口
│       │   ├── VideosApi.kt                  # 视频集锦 API 接口
│       │   ├── GamesApi.kt                   # 游戏推荐/白名单 API
│       │   └── AgentApi.kt                   # Agent 规则/增强 API
│       ├── model/
│       │   ├── NewsResponse.kt
│       │   ├── LiveResponse.kt               # LiveItem 独立数据类
│       │   ├── VideoResponse.kt              # 视频集锦响应
│       │   ├── NewGameResponse.kt
│       │   ├── WhitelistResponse.kt
│       │   ├── AgentRulesResponse.kt
│       │   └── ApiError.kt
│       ├── interceptor/
│       │   ├── ApiKeyInterceptor.kt          # X-API-Key 认证拦截器
│       │   └── RetryInterceptor.kt           # 错误重试拦截器
│       ├── interceptor/
│       │   └── ApiKeyInterceptor.kt          # X-API-Key 认证拦截器
│       └── di/
│           └── NetworkModule.kt              # 网络层 Hilt Module
│
├── core-ui/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/ui/
│       ├── theme/
│       │   ├── ThemeConfig.kt                # 主题配置数据类
│       │   ├── EsportsTheme.kt              # Compose Theme
│       │   ├── NeonTechTheme.kt             # 霓虹科技主题
│       │   ├── LuxuryTheme.kt              # 奢华精密主题
│       │   └── GalaxyTheme.kt              # 星系辐射主题
│       ├── component/
│       │   ├── GlassCard.kt                 # 毛玻璃卡片组件
│       │   ├── EcgBackground.kt             # 心电图背景组件
│       │   ├── BottomPill.kt                # 底部胶囊导航
│       │   └── StatusBar.kt                 # 自定义状态栏
│       └── di/
│           └── UiModule.kt
│
├── feature-games/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/games/
│       ├── data/
│       │   ├── GameScanner.kt               # 游戏扫描（PackageManager）
│       │   └── GameRepository.kt            # 游戏数据仓库
│       ├── domain/
│       │   ├── GameClassifier.kt            # 四层分类算法
│       │   └── model/ClassifiedGame.kt      # 分类后的游戏模型
│       ├── ui/
│       │   ├── GamesScreen.kt               # 游戏星系布局 Composable
│       │   ├── GamesViewModel.kt
│       │   ├── GamePosterCard.kt            # 大海报卡片
│       │   ├── GameLogoIcon.kt              # Logo 图标
│       │   └── NewGameBadge.kt              # NEW 呼吸光晕徽章
│       └── di/
│           └── GamesModule.kt
│
├── feature-news/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/news/
│       ├── data/
│       │   └── NewsRepository.kt
│       ├── ui/
│       │   ├── NewsPanel.kt                 # 右侧紧凑资讯面板
│       │   ├── NewsDetailScreen.kt          # 资讯详情（WebView）
│       │   └── NewsViewModel.kt
│       └── di/
│           └── NewsModule.kt
│
├── feature-datacenter/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/datacenter/
│       ├── data/
│       │   └── StatsRepository.kt           # 游戏统计数据仓库
│       ├── ui/
│       │   ├── DataCenterScreen.kt          # 全屏数据中心面板
│       │   ├── DataCenterViewModel.kt
│       │   ├── PlayTimeChart.kt             # 时长折线图
│       │   ├── GamePieChart.kt              # 游戏占比饼图
│       │   └── HeatMapView.kt              # 每日时段热力图
│       └── di/
│           └── DataCenterModule.kt
│
├── feature-livestream/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/livestream/
│       ├── data/
│       │   └── LivestreamRepository.kt
│       ├── ui/
│       │   ├── LivestreamPanel.kt           # 直播入口面板
│       │   ├── LivestreamViewModel.kt
│       │   └── WebViewPlayer.kt             # WebView 播放器（PiP 备用）
│       └── di/
│           └── LivestreamModule.kt
│
├── feature-performance/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/performance/
│       ├── data/
│       │   ├── DeviceMonitor.kt             # 设备指标采集 Service
│       │   └── PerformanceRepository.kt
│       ├── ui/
│       │   ├── PerformancePanel.kt          # 右侧性能数值面板
│       │   ├── PerformanceDetailScreen.kt   # 性能详情全屏面板
│       │   └── PerformanceViewModel.kt
│       └── di/
│           └── PerformanceModule.kt
│
├── feature-agent/
│   ├── build.gradle.kts
│   └── src/main/java/com/esports/space/agent/
│       ├── perception/
│       │   ├── PerceptionEngine.kt          # 感知引擎总线
│       │   ├── TimePerception.kt            # 时间感知
│       │   ├── UsageHabitPerception.kt      # 使用习惯感知
│       │   ├── DeviceStatePerception.kt     # 设备状态感知
│       │   ├── CalendarPerception.kt        # 日历感知
│       │   └── HealthPerception.kt          # 健康数据感知
│       ├── rules/
│       │   ├── RuleEngine.kt               # 规则引擎
│       │   ├── Rule.kt                     # 规则数据模型
│       │   └── RuleParser.kt               # JSON 规则解析
│       ├── recommendation/
│       │   ├── RecommendationManager.kt     # 推荐管理器
│       │   └── RecommendationCard.kt        # 推荐卡片数据模型
│       ├── sprite/
│       │   ├── SpriteView.kt               # 精灵悬浮窗 View
│       │   ├── SpriteService.kt            # 悬浮窗 Service
│       │   ├── SpriteAnimator.kt           # 精灵动画控制
│       │   ├── BubbleDialog.kt             # 气泡对话框
│       │   └── SpriteSettingsScreen.kt     # Agent 设置页面
│       ├── ui/
│       │   ├── AgentViewModel.kt
│       │   └── RecommendationListPanel.kt   # 推荐列表展开面板
│       └── di/
│           └── AgentModule.kt
```

---

## Task 1: 工程脚手架搭建

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (根)
- Create: `gradle.properties`
- Create: `app/build.gradle.kts`
- Create: `core-common/build.gradle.kts`
- Create: `core-data/build.gradle.kts`
- Create: `core-network/build.gradle.kts`
- Create: `core-ui/build.gradle.kts`
- Create: `feature-games/build.gradle.kts`
- Create: `feature-news/build.gradle.kts`
- Create: `feature-datacenter/build.gradle.kts`
- Create: `feature-livestream/build.gradle.kts`
- Create: `feature-performance/build.gradle.kts`
- Create: `feature-agent/build.gradle.kts`

- [ ] **Step 1: 初始化 Git 仓库和 Gradle Wrapper**

```powershell
cd d:\Users\XX\Documents\project\Esports_Space
git init
```

使用文件创建工具写入 `.gitignore`（内容如下）：
```
*.iml
.gradle/
build/
.idea/
local.properties
*.apk
*.aab
```

然后生成 Gradle Wrapper：
```powershell
gradle wrapper --gradle-version 8.6
```

验证 wrapper 存在：
```powershell
Test-Path .\gradlew.bat
```
Expected: `True`

> **注意**：后续所有 Gradle 命令均使用 `.\gradlew.bat`（Windows 环境）。

- [ ] **Step 2: 创建根 `build.gradle.kts`**

```kotlin
// build.gradle.kts
plugins {
    id("com.android.application") version "8.4.0" apply false
    id("com.android.library") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false
    id("com.google.dagger.hilt.android") version "2.50" apply false
    id("com.google.devtools.ksp") version "1.9.22-1.0.17" apply false
}
```

- [ ] **Step 3: 创建 `settings.gradle.kts`**

注册所有 11 个模块（1 app + 4 core + 6 feature）。

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "EsportsSpace"
include(":app")
include(":core-common")
include(":core-data")
include(":core-network")
include(":core-ui")
include(":feature-games")
include(":feature-news")
include(":feature-datacenter")
include(":feature-livestream")
include(":feature-performance")
include(":feature-agent")
```

- [ ] **Step 4: 创建 `gradle.properties`**

```properties
android.useAndroidX=true
android.nonTransitiveRClass=true
org.gradle.jvmargs=-Xmx2048m
org.gradle.parallel=true
```

- [ ] **Step 5: 创建 `app/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.esports.space"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.esports.space"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
    kotlinOptions { jvmTarget = "17" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-data"))
    implementation(project(":core-network"))
    implementation(project(":core-ui"))
    implementation(project(":feature-games"))
    implementation(project(":feature-news"))
    implementation(project(":feature-datacenter"))
    implementation(project(":feature-livestream"))
    implementation(project(":feature-performance"))
    implementation(project(":feature-agent"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.hilt:hilt-work:1.1.0")
    ksp("androidx.hilt:hilt-compiler:1.1.0")
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
}
```

- [ ] **Step 6: 创建各 core 和 feature 模块的 `build.gradle.kts`**

所有 library 模块使用统一模板（以 `core-data` 为例）：

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.esports.space.data"
    compileSdk = 34
    defaultConfig { minSdk = 26 }
    kotlinOptions { jvmTarget = "17" }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation(project(":core-common"))
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    // WorkManager (for cleanup)
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```

各模块完整依赖如下：

**core-common** (`namespace = "com.esports.space.common"`):
```kotlin
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    testImplementation("junit:junit:4.13.2")
}
```
不需要 Hilt/Room/Compose。

**core-network** (`namespace = "com.esports.space.network"`):
```kotlin
dependencies {
    implementation(project(":core-common"))
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-moshi:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.squareup.moshi:moshi:1.15.0")
    ksp("com.squareup.moshi:moshi-kotlin-codegen:1.15.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("com.squareup.moshi:moshi-kotlin:1.15.0")
}
```

**core-ui** (`namespace = "com.esports.space.ui"`):
```kotlin
android {
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}
dependencies {
    implementation(project(":core-common"))
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    implementation("io.coil-kt:coil-compose:2.5.0")
    debugImplementation("androidx.compose.ui:ui-tooling")
    testImplementation("junit:junit:4.13.2")
}
```
不需要 Hilt。

**每个 feature-* 模块**的通用依赖模板（以 feature-games 为例，`namespace = "com.esports.space.games"`）：
```kotlin
android {
    buildFeatures { compose = true }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.8" }
}
dependencies {
    implementation(project(":core-common"))
    implementation(project(":core-data"))
    implementation(project(":core-network"))
    implementation(project(":core-ui"))
    implementation("com.google.dagger:hilt-android:2.50")
    ksp("com.google.dagger:hilt-compiler:2.50")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("io.coil-kt:coil-compose:2.5.0")
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}
```
各 feature 模块 namespace 对应调整：`com.esports.space.news` / `.datacenter` / `.livestream` / `.performance` / `.agent`。feature-agent 额外加 `implementation("com.airbnb.android:lottie-compose:6.3.0")`。feature-livestream 额外加 `implementation("androidx.webkit:webkit:1.10.0")`。

- [ ] **Step 7: 创建 `app/src/main/AndroidManifest.xml`**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.QUERY_ALL_PACKAGES" />
    <uses-permission android:name="android.permission.PACKAGE_USAGE_STATS"
        tools:ignore="ProtectedPermissions" />
    <uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.READ_CALENDAR" />

    <application
        android:name=".EsportsSpaceApp"
        android:label="电竞空间"
        android:theme="@style/Theme.EsportsSpace"
        android:supportsRtl="true">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:launchMode="singleTask"
            android:screenOrientation="landscape"
            android:configChanges="orientation|screenSize|keyboardHidden">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.HOME" />
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>
</manifest>
```

- [ ] **Step 8: 创建 `EsportsSpaceApp.kt` Hilt Application**

```kotlin
package com.esports.space

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class EsportsSpaceApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}
```

- [ ] **Step 9: 创建空的 `MainActivity.kt` 占位**

```kotlin
package com.esports.space

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // TODO: 后续 Task 填充
        }
    }
}
```

- [ ] **Step 10: 验证项目可编译**

```bash
.\gradlew.bat assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **Step 11: 提交**

```bash
git add -A
git commit -m "chore: scaffold multi-module Android project with Hilt"
```

---

## Task 2: core-common 基础工具

**Files:**
- Create: `core-common/src/main/java/com/esports/space/common/result/Result.kt`
- Create: `core-common/src/main/java/com/esports/space/common/util/TimeUtils.kt`
- Create: `core-common/src/main/java/com/esports/space/common/util/PermissionHelper.kt`
- Test: `core-common/src/test/java/com/esports/space/common/util/TimeUtilsTest.kt`

- [ ] **Step 1: 编写 `TimeUtils` 测试**

```kotlin
class TimeUtilsTest {
    @Test
    fun `getTimeSlot returns EVENING for hour 19`() {
        assertEquals(TimeSlot.EVENING, TimeUtils.getTimeSlot(19))
    }

    @Test
    fun `getTimeSlot returns LATE_NIGHT for hour 1`() {
        assertEquals(TimeSlot.LATE_NIGHT, TimeUtils.getTimeSlot(1))
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

```bash
.\gradlew.bat :core-common:test
```
Expected: FAIL — class not found

- [ ] **Step 3: 实现 `Result.kt`**

```kotlin
package com.esports.space.common.result

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val code: Int, val message: String) : Result<Nothing>()
    data object Loading : Result<Nothing>()
}
```

- [ ] **Step 4: 实现 `TimeUtils.kt`**

```kotlin
package com.esports.space.common.util

enum class TimeSlot { MORNING, AFTERNOON, EVENING, LATE_NIGHT }

object TimeUtils {
    fun getTimeSlot(hour: Int): TimeSlot = when (hour) {
        in 6..11 -> TimeSlot.MORNING
        in 12..17 -> TimeSlot.AFTERNOON
        in 18..23 -> TimeSlot.EVENING
        else -> TimeSlot.LATE_NIGHT
    }
}
```

- [ ] **Step 5: 实现 `PermissionHelper.kt`**

```kotlin
package com.esports.space.common.util

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings

object PermissionHelper {
    fun hasUsageStatsPermission(context: Context): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageStatsSettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    fun canDrawOverlays(context: Context): Boolean =
        Settings.canDrawOverlays(context)

    fun openOverlaySettings(context: Context) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }
}
```

- [ ] **Step 6: 实现 `ForegroundAppTracker.kt`**

```kotlin
package com.esports.space.common.util

import android.app.usage.UsageStatsManager
import android.content.Context
import java.util.concurrent.TimeUnit

object ForegroundAppTracker {
    fun getCurrentForegroundPackage(context: Context): String? {
        val usm = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val now = System.currentTimeMillis()
        val stats = usm.queryUsageStats(
            UsageStatsManager.INTERVAL_DAILY,
            now - TimeUnit.MINUTES.toMillis(5),
            now
        )
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}
```

- [ ] **Step 7: 运行测试确认通过**

```powershell
.\gradlew.bat :core-common:test
```
Expected: PASS

- [ ] **Step 8: 提交**

```bash
git add -A
git commit -m "feat: add core-common with Result, TimeUtils, PermissionHelper, ForegroundAppTracker"
```

---

## Task 3: core-data 数据层

**Files:**
- Create: `core-data/src/main/java/com/esports/space/data/db/entity/*.kt` (4 entities)
- Create: `core-data/src/main/java/com/esports/space/data/db/dao/*.kt` (4 DAOs)
- Create: `core-data/src/main/java/com/esports/space/data/db/EsportsDatabase.kt`
- Create: `core-data/src/main/java/com/esports/space/data/db/DataCleanupWorker.kt`
- Create: `core-data/src/main/java/com/esports/space/data/datastore/UserPreferenceStore.kt`
- Create: `core-data/src/main/java/com/esports/space/data/di/DataModule.kt`
- Test: `core-data/src/test/java/com/esports/space/data/db/entity/GameRecordEntityTest.kt`

- [ ] **Step 1: 编写 `GameRecordEntity` 测试**

```kotlin
class GameRecordEntityTest {
    @Test
    fun `entity creation with all fields`() {
        val record = GameRecordEntity(
            packageName = "com.tencent.tmgp.sgame",
            displayName = "王者荣耀",
            iconUri = "content://...",
            posterUri = null,
            category = GameCategory.FREQUENT,
            totalPlayTime = 3600000L,
            lastPlayedAt = System.currentTimeMillis(),
            launchCount = 42,
            pinned = false
        )
        assertEquals("com.tencent.tmgp.sgame", record.packageName)
        assertEquals(GameCategory.FREQUENT, record.category)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

- [ ] **Step 3: 实现 4 个 Entity**

`GameRecordEntity.kt`:
```kotlin
package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class GameCategory { PREDICTED, FREQUENT, INFREQUENT, NEW }

@Entity(tableName = "game_records")
data class GameRecordEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val iconUri: String,
    val posterUri: String?,
    val category: GameCategory,
    val totalPlayTime: Long,
    val lastPlayedAt: Long,
    val launchCount: Int,
    val pinned: Boolean
)
```

`PlaySessionEntity.kt`:
```kotlin
package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "play_sessions",
    foreignKeys = [ForeignKey(
        entity = GameRecordEntity::class,
        parentColumns = ["packageName"],
        childColumns = ["packageName"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PlaySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMs: Long
)
```

`DeviceSnapshotEntity.kt`:
```kotlin
package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "device_snapshots")
data class DeviceSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val cpuTemp: Float?,
    val gpuTemp: Float?,
    val cpuFreqMhz: Int?,
    val gpuFreqMhz: Int?,
    val ramUsagePercent: Float,
    val networkLatencyMs: Int,
    val batteryPercent: Int
)
```

`AgentEventEntity.kt`:
```kotlin
package com.esports.space.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AgentEventType { RECOMMENDATION, REMINDER, ALERT }
enum class UserAction { ACCEPTED, DISMISSED, IGNORED }

@Entity(tableName = "agent_events")
data class AgentEventEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long,
    val eventType: AgentEventType,
    val triggerSource: String,
    val content: String,
    val userAction: UserAction?
)
```

- [ ] **Step 4: 实现 4 个 DAO**

`GameRecordDao.kt`:
```kotlin
package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.GameCategory
import com.esports.space.data.db.entity.GameRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameRecordDao {
    @Query("SELECT * FROM game_records ORDER BY lastPlayedAt DESC")
    fun getAll(): Flow<List<GameRecordEntity>>

    @Query("SELECT * FROM game_records WHERE category = :category")
    fun getByCategory(category: GameCategory): Flow<List<GameRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(record: GameRecordEntity)

    @Update
    suspend fun update(record: GameRecordEntity)

    @Delete
    suspend fun delete(record: GameRecordEntity)
}
```

`PlaySessionDao.kt`:
```kotlin
package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.PlaySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaySessionDao {
    @Insert
    suspend fun insert(session: PlaySessionEntity)

    @Query("SELECT * FROM play_sessions WHERE packageName = :pkg ORDER BY startTime DESC")
    fun getByPackage(pkg: String): Flow<List<PlaySessionEntity>>

    @Query("SELECT SUM(durationMs) FROM play_sessions WHERE startTime >= :since")
    suspend fun getTotalPlayTimeSince(since: Long): Long?

    @Query("DELETE FROM play_sessions WHERE startTime < :before")
    suspend fun deleteOlderThan(before: Long)
}
```

`DeviceSnapshotDao.kt`:
```kotlin
package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.DeviceSnapshotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DeviceSnapshotDao {
    @Insert
    suspend fun insert(snapshot: DeviceSnapshotEntity)

    @Query("SELECT * FROM device_snapshots ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<DeviceSnapshotEntity>>

    @Query("DELETE FROM device_snapshots WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
```

`AgentEventDao.kt`:
```kotlin
package com.esports.space.data.db.dao

import androidx.room.*
import com.esports.space.data.db.entity.AgentEventEntity
import com.esports.space.data.db.entity.AgentEventType
import com.esports.space.data.db.entity.UserAction
import kotlinx.coroutines.flow.Flow

@Dao
interface AgentEventDao {
    @Insert
    suspend fun insert(event: AgentEventEntity)

    @Query("SELECT * FROM agent_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<AgentEventEntity>>

    @Query("UPDATE agent_events SET userAction = :action WHERE id = :id")
    suspend fun updateUserAction(id: Long, action: UserAction)

    @Query("SELECT COUNT(*) FROM agent_events WHERE eventType = :type AND userAction = :action AND timestamp >= :since")
    suspend fun countByTypeAndAction(type: AgentEventType, action: UserAction, since: Long): Int

    @Query("DELETE FROM agent_events WHERE timestamp < :before")
    suspend fun deleteOlderThan(before: Long)
}
```

- [ ] **Step 5: 实现 `EsportsDatabase.kt`**

```kotlin
package com.esports.space.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.esports.space.data.db.dao.*
import com.esports.space.data.db.entity.*

@Database(
    entities = [
        GameRecordEntity::class,
        PlaySessionEntity::class,
        DeviceSnapshotEntity::class,
        AgentEventEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EsportsDatabase : RoomDatabase() {
    abstract fun gameRecordDao(): GameRecordDao
    abstract fun playSessionDao(): PlaySessionDao
    abstract fun deviceSnapshotDao(): DeviceSnapshotDao
    abstract fun agentEventDao(): AgentEventDao
}
```

- [ ] **Step 6: 实现 `DataCleanupWorker.kt`**

```kotlin
package com.esports.space.data.db

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.esports.space.data.db.dao.AgentEventDao
import com.esports.space.data.db.dao.DeviceSnapshotDao
import com.esports.space.data.db.dao.PlaySessionDao
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

@HiltWorker
class DataCleanupWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val snapshotDao: DeviceSnapshotDao,
    private val sessionDao: PlaySessionDao,
    private val eventDao: AgentEventDao
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val now = System.currentTimeMillis()
        snapshotDao.deleteOlderThan(now - TimeUnit.DAYS.toMillis(7))
        sessionDao.deleteOlderThan(now - TimeUnit.DAYS.toMillis(90))
        eventDao.deleteOlderThan(now - TimeUnit.DAYS.toMillis(30))
        return Result.success()
    }
}
```

- [ ] **Step 7: 实现 `UserPreferenceStore.kt`**

```kotlin
package com.esports.space.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

@Singleton
class UserPreferenceStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val AGENT_ENABLED = booleanPreferencesKey("agent_enabled")
        val AGENT_FREQUENCY = stringPreferencesKey("agent_frequency")
        val SPRITE_APPEARANCE = stringPreferencesKey("sprite_appearance")
    }

    val theme: Flow<String> = context.dataStore.data.map { it[THEME_KEY] ?: "galaxy" }
    val agentEnabled: Flow<Boolean> = context.dataStore.data.map { it[AGENT_ENABLED] ?: true }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { it[THEME_KEY] = theme }
    }

    suspend fun setAgentEnabled(enabled: Boolean) {
        context.dataStore.edit { it[AGENT_ENABLED] = enabled }
    }
}
```

- [ ] **Step 8: 实现 `DataModule.kt` Hilt Module**

```kotlin
package com.esports.space.data.di

import android.content.Context
import androidx.room.Room
import com.esports.space.data.db.EsportsDatabase
import com.esports.space.data.db.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides @Singleton
    fun provideDatabase(@ApplicationContext context: Context): EsportsDatabase =
        Room.databaseBuilder(context, EsportsDatabase::class.java, "esports_space.db").build()

    @Provides fun provideGameRecordDao(db: EsportsDatabase): GameRecordDao = db.gameRecordDao()
    @Provides fun providePlaySessionDao(db: EsportsDatabase): PlaySessionDao = db.playSessionDao()
    @Provides fun provideDeviceSnapshotDao(db: EsportsDatabase): DeviceSnapshotDao = db.deviceSnapshotDao()
    @Provides fun provideAgentEventDao(db: EsportsDatabase): AgentEventDao = db.agentEventDao()
}
```

- [ ] **Step 9: 运行测试确认通过**

```bash
.\gradlew.bat :core-data:test
```
Expected: PASS

- [ ] **Step 10: 提交**

```bash
git add -A
git commit -m "feat: add core-data with Room entities, DAOs, DataStore, cleanup worker"
```

---

## Task 4: core-network 网络层

**Files:**
- Create: `core-network/src/main/java/com/esports/space/network/model/*.kt` (6 response models)
- Create: `core-network/src/main/java/com/esports/space/network/api/*.kt` (4 API interfaces)
- Create: `core-network/src/main/java/com/esports/space/network/interceptor/ApiKeyInterceptor.kt`
- Create: `core-network/src/main/java/com/esports/space/network/di/NetworkModule.kt`
- Test: `core-network/src/test/java/com/esports/space/network/model/NewsResponseTest.kt`

- [ ] **Step 1: 编写 `NewsResponse` 序列化测试**

```kotlin
class NewsResponseTest {
    @Test
    fun `deserialize news item from json`() {
        val json = """{"id":"news_001","title":"KPL决赛","summary":"...","source":"腾讯电竞","image_url":"https://img","detail_url":"https://detail","published_at":"2026-03-26T14:00:00Z","tags":["KPL"],"is_live":true,"live_url":"https://live"}"""
        val adapter = Moshi.Builder().build().adapter(NewsItem::class.java)
        val item = adapter.fromJson(json)!!
        assertEquals("news_001", item.id)
        assertTrue(item.isLive)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

- [ ] **Step 3: 实现 Response 模型**

`NewsResponse.kt`:
```kotlin
package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    val code: Int,
    val data: T?,
    val message: String?
)

@JsonClass(generateAdapter = true)
data class NewsPageData(
    val total: Int,
    val items: List<NewsItem>
)

@JsonClass(generateAdapter = true)
data class NewsItem(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    @Json(name = "image_url") val imageUrl: String?,
    @Json(name = "detail_url") val detailUrl: String,
    @Json(name = "published_at") val publishedAt: String,
    val tags: List<String>,
    @Json(name = "is_live") val isLive: Boolean,
    @Json(name = "live_url") val liveUrl: String?
)
```

`NewGameResponse.kt`:
```kotlin
package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NewGameItem(
    @Json(name = "package_name") val packageName: String,
    @Json(name = "display_name") val displayName: String,
    @Json(name = "icon_url") val iconUrl: String,
    @Json(name = "poster_url") val posterUrl: String?,
    val description: String,
    @Json(name = "store_url") val storeUrl: String,
    val tags: List<String>
)
```

`LiveResponse.kt`:
```kotlin
package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LiveItem(
    val id: String,
    val title: String,
    val platform: String,
    val streamer: String?,
    @Json(name = "viewer_count") val viewerCount: Int,
    @Json(name = "stream_url") val streamUrl: String,
    @Json(name = "deep_link") val deepLink: String?,
    @Json(name = "thumbnail_url") val thumbnailUrl: String?,
    val tags: List<String>
)
```

`VideoResponse.kt`:
```kotlin
package com.esports.space.network.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class VideoItem(
    val id: String,
    val title: String,
    @Json(name = "video_url") val videoUrl: String,
    @Json(name = "thumbnail_url") val thumbnailUrl: String?,
    @Json(name = "duration_seconds") val durationSeconds: Int,
    val tags: List<String>
)
```

`VideosApi.kt`:
```kotlin
package com.esports.space.network.api

import com.esports.space.network.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface VideosApi {
    @GET("v1/videos")
    suspend fun getVideos(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("game_filter") gameFilter: String? = null
    ): ApiResponse<List<VideoItem>>
}
```

`ApiError.kt`:
```kotlin
package com.esports.space.network.model

data class ApiError(
    val code: Int,
    val message: String
) {
    companion object {
        const val AUTH_FAILED = 1001
        const val INVALID_PARAMS = 1002
        const val SERVER_ERROR = 2001
        const val RATE_LIMITED = 3001
    }
}
```

`RetryInterceptor.kt`:
```kotlin
package com.esports.space.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

class RetryInterceptor(private val maxRetries: Int = 3) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null
        while (attempt < maxRetries) {
            try {
                val response = chain.proceed(chain.request())
                if (response.code == 429) {
                    val retryAfter = response.header("Retry-After")?.toLongOrNull() ?: (2L shl attempt)
                    response.close()
                    Thread.sleep(retryAfter * 1000)
                    attempt++
                    continue
                }
                if (response.code in 500..599 && attempt < maxRetries - 1) {
                    response.close()
                    Thread.sleep((2L shl attempt) * 1000)
                    attempt++
                    continue
                }
                return response
            } catch (e: IOException) {
                lastException = e
                attempt++
                if (attempt < maxRetries) Thread.sleep((2L shl attempt) * 1000)
            }
        }
        throw lastException ?: IOException("Max retries exceeded")
    }
}
```

`WhitelistResponse.kt`, `AgentRulesResponse.kt` — 按 Spec §8 定义的结构实现。

- [ ] **Step 4: 实现 4 个 API 接口**

`NewsApi.kt`:
```kotlin
package com.esports.space.network.api

import com.esports.space.network.model.*
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApi {
    @GET("v1/news")
    suspend fun getNews(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
        @Query("game_filter") gameFilter: String? = null
    ): ApiResponse<NewsPageData>
}
```

`LiveApi.kt`:
```kotlin
package com.esports.space.network.api

import com.esports.space.network.model.*
import retrofit2.http.GET

interface LiveApi {
    @GET("v1/live")
    suspend fun getLiveStreams(): ApiResponse<List<LiveItem>>
}
```

`GamesApi.kt`:
```kotlin
package com.esports.space.network.api

import com.esports.space.network.model.*
import retrofit2.http.GET

interface GamesApi {
    @GET("v1/games/new")
    suspend fun getNewGames(): ApiResponse<List<NewGameItem>>

    @GET("v1/games/whitelist")
    suspend fun getWhitelist(): ApiResponse<List<String>>
}
```

`AgentApi.kt`:
```kotlin
package com.esports.space.network.api

import com.esports.space.network.model.*
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AgentApi {
    @GET("v1/agent/rules")
    suspend fun getRules(): ApiResponse<AgentRulesResponse>

    @POST("v1/agent/enhance")
    suspend fun enhance(@Body summary: Map<String, Any>): ApiResponse<List<String>>
}
```

- [ ] **Step 5: 实现 `ApiKeyInterceptor.kt`**

```kotlin
package com.esports.space.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response

class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("X-API-Key", apiKey)
            .build()
        return chain.proceed(request)
    }
}
```

- [ ] **Step 6: 实现 `NetworkModule.kt`**

```kotlin
package com.esports.space.network.di

import com.esports.space.network.api.*
import com.esports.space.network.interceptor.ApiKeyInterceptor
import com.squareup.moshi.Moshi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    private const val BASE_URL = "https://api.esports-space.com/"

    @Provides @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder().build()

    @Provides @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(ApiKeyInterceptor("YOUR_API_KEY"))
        .addInterceptor(RetryInterceptor(maxRetries = 3))
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient, moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides @Singleton fun provideNewsApi(retrofit: Retrofit): NewsApi = retrofit.create(NewsApi::class.java)
    @Provides @Singleton fun provideLiveApi(retrofit: Retrofit): LiveApi = retrofit.create(LiveApi::class.java)
    @Provides @Singleton fun provideVideosApi(retrofit: Retrofit): VideosApi = retrofit.create(VideosApi::class.java)
    @Provides @Singleton fun provideGamesApi(retrofit: Retrofit): GamesApi = retrofit.create(GamesApi::class.java)
    @Provides @Singleton fun provideAgentApi(retrofit: Retrofit): AgentApi = retrofit.create(AgentApi::class.java)
}
```

- [ ] **Step 7: 运行测试确认通过**

```bash
.\gradlew.bat :core-network:test
```
Expected: PASS

- [ ] **Step 8: 提交**

```bash
git add -A
git commit -m "feat: add core-network with Retrofit APIs, Moshi models, API key interceptor"
```

---

## Task 5: core-ui 主题系统与共享组件

**Files:**
- Create: `core-ui/src/main/java/com/esports/space/ui/theme/ThemeConfig.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/theme/EsportsTheme.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/theme/NeonTechTheme.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/theme/LuxuryTheme.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/theme/GalaxyTheme.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/component/GlassCard.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/component/EcgBackground.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/component/BottomPill.kt`
- Create: `core-ui/src/main/java/com/esports/space/ui/component/StatusBar.kt`

- [ ] **Step 1: 实现 `ThemeConfig.kt`**

```kotlin
package com.esports.space.ui.theme

import androidx.compose.ui.graphics.Color

data class ThemeConfig(
    val id: String,
    val name: String,
    val background: Color,
    val surface: Color,
    val primaryAccent: Color,
    val secondaryAccent: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val liveIndicator: Color,
    val usesGameBackdrop: Boolean,
    val layoutMode: LayoutMode
)

enum class LayoutMode { THREE_COLUMN, STAGGERED, GALAXY_RADIAL }
```

- [ ] **Step 2: 实现三套主题定义**

`NeonTechTheme.kt` — 霓虹科技（青紫光效）
`LuxuryTheme.kt` — 奢华精密（琥珀金）
`GalaxyTheme.kt` — 星系辐射（游戏蒙版底图 + 琥珀金）

每个文件导出一个 `val` 类型的 `ThemeConfig` 实例。

- [ ] **Step 3: 实现 `EsportsTheme.kt` Compose Theme 包装**

```kotlin
package com.esports.space.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*

val LocalThemeConfig = staticCompositionLocalOf { GalaxyThemeConfig }

@Composable
fun EsportsTheme(
    themeConfig: ThemeConfig = GalaxyThemeConfig,
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        background = themeConfig.background,
        surface = themeConfig.surface,
        primary = themeConfig.primaryAccent,
        secondary = themeConfig.secondaryAccent,
        onBackground = themeConfig.textPrimary,
        onSurface = themeConfig.textSecondary,
        error = themeConfig.liveIndicator
    )
    CompositionLocalProvider(LocalThemeConfig provides themeConfig) {
        MaterialTheme(colorScheme = colorScheme, content = content)
    }
}
```

- [ ] **Step 4: 实现 `GlassCard.kt` 毛玻璃卡片**

使用 `Modifier.background()` + `alpha` + `BlurEffect` 实现毛玻璃效果。

- [ ] **Step 5: 实现 `EcgBackground.kt` 心电图背景**

使用 `Canvas` Composable + 环形缓冲区数据绘制双线波形。接受 `cpuFreqHistory: List<Float>` 和 `gpuFreqHistory: List<Float>` 参数。

- [ ] **Step 6: 实现 `BottomPill.kt` 底部胶囊导航**

毛玻璃胶囊样式，包含数据中心/设置/搜索三个图标按钮。

- [ ] **Step 7: 实现 `StatusBar.kt` 自定义状态栏**

左侧时间，右侧 Wi-Fi + 电量图标，极简风格。

- [ ] **Step 8: 验证编译通过**

```bash
.\gradlew.bat :core-ui:assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: 提交**

```bash
git add -A
git commit -m "feat: add core-ui with 3 themes, glass card, ECG background, navigation components"
```

---

## Task 6: feature-performance 性能监控

**Files:**
- Create: `feature-performance/src/main/java/com/esports/space/performance/data/DeviceMonitor.kt`
- Create: `feature-performance/src/main/java/com/esports/space/performance/data/PerformanceRepository.kt`
- Create: `feature-performance/src/main/java/com/esports/space/performance/ui/PerformancePanel.kt`
- Create: `feature-performance/src/main/java/com/esports/space/performance/ui/PerformanceDetailScreen.kt`
- Create: `feature-performance/src/main/java/com/esports/space/performance/ui/PerformanceViewModel.kt`
- Create: `feature-performance/src/main/java/com/esports/space/performance/di/PerformanceModule.kt`
- Test: `feature-performance/src/test/java/com/esports/space/performance/data/DeviceMonitorTest.kt`

- [ ] **Step 1: 编写 `DeviceMonitor` 测试**

测试温度读取降级逻辑：当 `/sys/` 节点不可读时返回 null。

- [ ] **Step 2: 运行测试确认失败**

- [ ] **Step 3: 实现 `DeviceMonitor.kt`**

通过 `CoroutineScope` + `delay(5000)` 循环采集 CPU 温度（`/sys/class/thermal/`）、CPU 频率（`/sys/devices/system/cpu/`）、RAM（`ActivityManager.MemoryInfo`）、电量（`BatteryManager`）、网络延迟（HTTP HEAD ping）。GPU 相关指标尝试读取，不可用返回 null。

- [ ] **Step 4: 实现 `PerformanceRepository.kt`**

聚合 DeviceMonitor 的实时 Flow 和 DeviceSnapshotDao 的历史数据。

- [ ] **Step 5: 实现 `PerformancePanel.kt`**

右侧紧凑面板 Composable：CPU/GPU 温度、RAM、延迟、FPS + "性能调节 →" 按钮。

- [ ] **Step 6: 实现 `PerformanceDetailScreen.kt`**

全屏性能详情面板，含历史折线图。

- [ ] **Step 7: 实现 `PerformanceViewModel.kt`**

StateFlow 暴露实时性能数据和心电图历史缓冲区。

- [ ] **Step 8: 实现 `PerformanceModule.kt` Hilt Module**

- [ ] **Step 9: 运行测试确认通过**

```bash
.\gradlew.bat :feature-performance:test
```
Expected: PASS

- [ ] **Step 10: 提交**

```bash
git add -A
git commit -m "feat: add performance monitoring with device metrics, ECG data, degradation"
```

---

## Task 7: feature-games 游戏入口

**Files:**
- Create: `feature-games/src/main/java/com/esports/space/games/data/GameScanner.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/data/GameRepository.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/domain/GameClassifier.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/domain/model/ClassifiedGame.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/ui/GamesScreen.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/ui/GamesViewModel.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/ui/GamePosterCard.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/ui/GameLogoIcon.kt`
- Create: `feature-games/src/main/java/com/esports/space/games/ui/NewGameBadge.kt`
- Test: `feature-games/src/test/java/com/esports/space/games/domain/GameClassifierTest.kt`

- [ ] **Step 1: 编写 `GameClassifier` 测试**

测试加权评分算法：验证给定 PlaySession 数据后，评分排序正确。

```kotlin
class GameClassifierTest {
    @Test
    fun `classify returns top 3 as PREDICTED sorted by score`() {
        val records = listOf(
            fakeRecord("game.a", launchCount = 50, lastPlayed = now()),
            fakeRecord("game.b", launchCount = 5, lastPlayed = daysAgo(30)),
            fakeRecord("game.c", launchCount = 30, lastPlayed = daysAgo(1))
        )
        val sessions = mapOf(/* ... per-hour session counts ... */)
        val result = GameClassifier().classify(records, sessions, currentHour = 20)
        assertEquals(GameCategory.PREDICTED, result[0].category)
        assertEquals("game.a", result[0].packageName)
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

- [ ] **Step 3: 实现 `GameClassifier.kt`**

按 Spec §7.2 的加权评分公式：`score = 0.4×时段偏好 + 0.3×近期频率 + 0.2×新鲜度 + 0.1×总时长`。

- [ ] **Step 4: 实现 `GameScanner.kt`**

使用 `PackageManager.getInstalledApplications()` + 白名单匹配识别游戏。

- [ ] **Step 5: 实现 `GameRepository.kt`**

整合 Scanner + Dao + 云端白名单，提供 `Flow<List<ClassifiedGame>>`。

- [ ] **Step 6: 实现星系辐射布局 `GamesScreen.kt`**

Compose Canvas + 自定义布局，中心大海报 → 中等 Logo → 小 Logo 辐射分布。

- [ ] **Step 7: 实现 `GamePosterCard.kt`、`GameLogoIcon.kt`、`NewGameBadge.kt`**

海报卡片：无边框圆角 + AsyncImage。Logo 图标：圆形/圆角方形。NEW 徽章：金色呼吸动画（`infiniteTransition` + `animateFloat`）。

- [ ] **Step 8: 实现 `GamesViewModel.kt`**

管理分类列表 StateFlow，处理游戏启动/长按菜单事件。

- [ ] **Step 9: 实现手动添加游戏功能**

在长按菜单中添加"添加游戏"入口，弹出已安装 App 列表（非游戏也可选），添加后写入 GameRecord。

- [ ] **Step 10: 实现新游/白名单定时刷新**

在 `GameRepository` 中基于 DataStore 时间戳实现缓存过期：新游推荐每 12 小时刷新，白名单每日刷新。

- [ ] **Step 11: 运行测试确认通过**

```powershell
.\gradlew.bat :feature-games:test
```
Expected: PASS

- [ ] **Step 12: 提交**

```bash
git add -A
git commit -m "feat: add game launcher with scanner, classifier, galaxy radial layout, manual add"
```

---

## Task 8: feature-news 电竞资讯

**Files:**
- Create: `feature-news/src/main/java/com/esports/space/news/data/NewsRepository.kt`
- Create: `feature-news/src/main/java/com/esports/space/news/ui/NewsPanel.kt`
- Create: `feature-news/src/main/java/com/esports/space/news/ui/NewsDetailScreen.kt`
- Create: `feature-news/src/main/java/com/esports/space/news/ui/NewsViewModel.kt`

- [ ] **Step 1: 实现 `NewsRepository.kt`**

调用 `NewsApi` + `LiveApi`，缓存 20 条到内存，提供 `Flow<List<NewsItem>>`。

- [ ] **Step 2: 实现 `NewsPanel.kt`**

右侧紧凑列表 Composable，3 条新闻 + LIVE 红点 + "更多资讯 >" 链接。

- [ ] **Step 3: 实现 `NewsDetailScreen.kt`**

全屏 WebView 加载资讯详情 URL。

- [ ] **Step 4: 实现 `NewsViewModel.kt`**

30 分钟自动轮询刷新，支持手动下拉刷新。

- [ ] **Step 5: 验证编译**

```bash
.\gradlew.bat :feature-news:assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: 提交**

```bash
git add -A
git commit -m "feat: add news module with API integration, compact panel, WebView detail"
```

---

## Task 9: feature-datacenter 个人数据中心

**Files:**
- Create: `feature-datacenter/src/main/java/com/esports/space/datacenter/data/StatsRepository.kt`
- Create: `feature-datacenter/src/main/java/com/esports/space/datacenter/ui/DataCenterScreen.kt`
- Create: `feature-datacenter/src/main/java/com/esports/space/datacenter/ui/DataCenterViewModel.kt`
- Create: `feature-datacenter/src/main/java/com/esports/space/datacenter/ui/PlayTimeChart.kt`
- Create: `feature-datacenter/src/main/java/com/esports/space/datacenter/ui/GamePieChart.kt`
- Create: `feature-datacenter/src/main/java/com/esports/space/datacenter/ui/HeatMapView.kt`
- Test: `feature-datacenter/src/test/java/com/esports/space/datacenter/data/StatsRepositoryTest.kt`

- [ ] **Step 1: 编写 `StatsRepository` 测试**

验证按天/周/月汇总时长逻辑。

- [ ] **Step 2: 运行测试确认失败**

- [ ] **Step 3: 实现 `StatsRepository.kt`**

从 PlaySessionDao 查询数据，按时间窗口聚合为今日/本周/本月时长 + 各游戏占比。

- [ ] **Step 4: 实现图表组件**

`PlayTimeChart.kt`（折线图）、`GamePieChart.kt`（饼图）、`HeatMapView.kt`（热力图），均用 Compose Canvas 绘制。

- [ ] **Step 5: 实现 `DataCenterScreen.kt` + `DataCenterViewModel.kt`**

全屏面板，展示所有图表 + 健康提示。

- [ ] **Step 6: 运行测试确认通过**

```bash
.\gradlew.bat :feature-datacenter:test
```
Expected: PASS

- [ ] **Step 7: 提交**

```bash
git add -A
git commit -m "feat: add data center with play time stats, charts, heatmap"
```

---

## Task 10: feature-livestream 直播/视频

**Files:**
- Create: `feature-livestream/src/main/java/com/esports/space/livestream/data/LivestreamRepository.kt`
- Create: `feature-livestream/src/main/java/com/esports/space/livestream/ui/LivestreamPanel.kt`
- Create: `feature-livestream/src/main/java/com/esports/space/livestream/ui/LivestreamViewModel.kt`
- Create: `feature-livestream/src/main/java/com/esports/space/livestream/ui/WebViewPlayer.kt`

- [ ] **Step 1: 实现 `LivestreamRepository.kt`**

调用 `LiveApi`，5 分钟轮询，提供 `Flow<List<LiveItem>>`。

- [ ] **Step 2: 实现 `LivestreamPanel.kt`**

资讯区 LIVE 入口 Composable，展示正在直播的赛事。

- [ ] **Step 3: 实现跳转逻辑**

检测已安装直播 App → Deep Link 跳转；未安装 → WebView 备用。

- [ ] **Step 4: 实现 `WebViewPlayer.kt`**

WebView 播放器，支持 PiP（`enterPictureInPictureMode`）。

- [ ] **Step 5: 实现视频集锦功能**

在 `LivestreamRepository` 中添加调用 `VideosApi.getVideos()` 的方法。创建 `VideoListScreen.kt` 竖向信息流浏览页面，展示精彩集锦视频列表，点击跳转 WebView 播放。

- [ ] **Step 6: 验证编译**

```powershell
.\gradlew.bat :feature-livestream:assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 7: 提交**

```bash
git add -A
git commit -m "feat: add livestream module with Deep Link, WebView fallback, PiP, video highlights"
```

---

## Task 11: feature-agent 智能 Agent（感知 + 规则引擎）

**Files:**
- Create: `feature-agent/src/main/java/com/esports/space/agent/perception/*.kt` (5 感知)
- Create: `feature-agent/src/main/java/com/esports/space/agent/rules/Rule.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/rules/RuleParser.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/rules/RuleEngine.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/recommendation/RecommendationManager.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/recommendation/RecommendationCard.kt`
- Test: `feature-agent/src/test/java/com/esports/space/agent/rules/RuleEngineTest.kt`
- Test: `feature-agent/src/test/java/com/esports/space/agent/rules/RuleParserTest.kt`

- [ ] **Step 1: 编写 `RuleParser` 测试**

验证 JSON 规则能正确反序列化为 `Rule` 数据类。

```kotlin
class RuleParserTest {
    @Test
    fun `parse rule with AND conditions`() {
        val json = """{"id":"test","priority":1,"conditions":{"operator":"AND","items":[{"dimension":"time","field":"hour","op":"between","value":[17,21]}]},"action":{"type":"RECOMMENDATION","template":"test {hour}"},"cooldown_minutes":60,"max_dismissals_per_day":2}"""
        val rule = RuleParser.parse(json)
        assertEquals("test", rule.id)
        assertEquals(1, rule.conditions.items.size)
        assertEquals("between", rule.conditions.items[0].op)
    }
}
```

- [ ] **Step 2: 编写 `RuleEngine` 测试**

验证规则评估逻辑：给定感知上下文，匹配正确的规则。

```kotlin
class RuleEngineTest {
    @Test
    fun `evaluate triggers fatigue rule after 3 hours`() {
        val context = PerceptionContext(
            currentHour = 19,
            continuousNonGameMinutes = 200,
            batteryPercent = 80,
            isCharging = false
        )
        val rules = listOf(fatigueRule, lowBatteryRule)
        val triggered = RuleEngine().evaluate(rules, context)
        assertEquals(1, triggered.size)
        assertEquals("fatigue_reminder", triggered[0].ruleId)
    }
}
```

- [ ] **Step 3: 运行测试确认失败**

- [ ] **Step 4: 实现 `Rule.kt` 数据模型**

```kotlin
package com.esports.space.agent.rules

data class Rule(
    val id: String,
    val priority: Int,
    val conditions: ConditionGroup,
    val action: RuleAction,
    val cooldownMinutes: Int,
    val maxDismissalsPerDay: Int
)

data class ConditionGroup(
    val operator: String, // "AND" | "OR"
    val items: List<Condition>
)

data class Condition(
    val dimension: String,
    val field: String,
    val op: String, // ==, !=, >, >=, <, <=, between, in
    val value: Any
)

data class RuleAction(
    val type: String, // RECOMMENDATION, REMINDER, ALERT
    val template: String,
    val gameFilter: GameFilter? = null
)

data class GameFilter(
    val maxSessionMinutes: Int? = null,
    val tags: List<String>? = null
)
```

- [ ] **Step 5: 实现 `RuleParser.kt`**

使用 Moshi 解析 JSON 到 `Rule` 列表。

- [ ] **Step 6: 实现 `RuleEngine.kt`**

遍历规则列表，按优先级排序，逐条评估条件组（AND/OR），支持所有 8 种操作符。检查冷却时间和当日忽略次数。

- [ ] **Step 7: 实现 `TimePerception.kt`**

返回当前时间、星期、是否节假日、当前 `TimeSlot`。

- [ ] **Step 8: 实现 `UsageHabitPerception.kt`**

从 `PlaySessionDao` + `ForegroundAppTracker` 查询：连续非游戏时长、最近 7 天各游戏启动频率、当前时段历史偏好。

- [ ] **Step 9: 实现 `DeviceStatePerception.kt`**

从 `DeviceMonitor` 获取电量、温度、是否充电、网络状态。

- [ ] **Step 10: 实现 `CalendarPerception.kt`**

通过 `CalendarProvider`（需 `READ_CALENDAR` 权限）查询未来 2 小时内的日程。权限未授予时返回空。

- [ ] **Step 11: 实现 `HealthPerception.kt`**

通过 Health Connect API 查询最近心率数据。未安装/未授权时返回空。

- [ ] **Step 12: 实现 `PerceptionEngine.kt`**

汇总所有 5 个感知模块的输出为统一的 `PerceptionContext` 数据类。

- [ ] **Step 13: 实现 `RecommendationManager.kt`**

定时（5 分钟间隔）运行感知采集 → 规则评估 → 生成推荐卡片。管理主动推荐的频率限制（30 分钟最多一次、忽略降级等）。

- [ ] **Step 14: 运行测试确认通过**

```powershell
.\gradlew.bat :feature-agent:test
```
Expected: PASS

- [ ] **Step 15: 提交**

```bash
git add -A
git commit -m "feat: add agent perception engine, rule parser, rule engine, recommendation manager"
```

---

## Task 12: feature-agent 桌面精灵 UI

**Files:**
- Create: `feature-agent/src/main/java/com/esports/space/agent/sprite/SpriteView.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/sprite/SpriteService.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/sprite/SpriteAnimator.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/sprite/BubbleDialog.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/sprite/SpriteSettingsScreen.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/ui/AgentViewModel.kt`
- Create: `feature-agent/src/main/java/com/esports/space/agent/ui/RecommendationListPanel.kt`

- [ ] **Step 1: 实现 `SpriteView.kt`**

自定义 View，使用 Lottie 渲染精灵动画。支持触摸拖拽（`onTouchEvent` ACTION_MOVE）。形象层和动效层分离：形象由 Lottie JSON/图片资源驱动，呼吸/浮动动效独立运行。

- [ ] **Step 2: 实现 `SpriteService.kt`**

前台 Service，通过 `WindowManager.addView()` 添加悬浮 `SpriteView`。管理精灵显示/隐藏。检测前台 App 为游戏时自动隐藏。

在 `AndroidManifest.xml` 注册 Service：
```xml
<service android:name="com.esports.space.agent.sprite.SpriteService"
    android:foregroundServiceType="specialUse" />
```

- [ ] **Step 3: 实现 `SpriteAnimator.kt`**

控制精灵的闲置微动画（浮动、旋转、偶尔变换形态）和交互动画（点击弹跳、推荐弹出等）。

- [ ] **Step 4: 实现 `BubbleDialog.kt`**

毛玻璃气泡对话框 Composable，展示推荐文案 + 行动按钮（"开始游戏"/"去观看"/"去休息"）。带入场/出场动画。

- [ ] **Step 5: 实现 `SpriteSettingsScreen.kt`**

Agent 设置全屏 Compose 页面：外观选择（预设网格 + 自定义导入按钮）、推荐频率、感知权限开关、偏好设置、免打扰时段、总开关。

- [ ] **Step 6: 实现 `AgentViewModel.kt`**

连接 RecommendationManager，管理推荐列表 StateFlow，处理用户操作（接受/忽略/进入设置）。

- [ ] **Step 7: 实现 `RecommendationListPanel.kt`**

点击精灵后展开的推荐列表面板（最近 3-5 条），每条带推荐理由和行动按钮。

- [ ] **Step 8: 验证编译**

```bash
.\gradlew.bat :feature-agent:assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: 提交**

```bash
git add -A
git commit -m "feat: add agent sprite with floating window, bubble dialog, settings, animations"
```

---

## Task 13: 主界面整合

**Files:**
- Modify: `app/src/main/java/com/esports/space/MainActivity.kt`
- Create: `app/src/main/java/com/esports/space/MainViewModel.kt`
- Create: `app/src/main/java/com/esports/space/navigation/AppNavigation.kt`
- Create: `app/src/main/java/com/esports/space/di/AppModule.kt`

- [ ] **Step 1: 实现 `MainViewModel.kt`**

管理主题切换、导航状态、权限请求流程。组合各 feature ViewModel 的数据。

- [ ] **Step 2: 实现 `AppNavigation.kt`**

Compose Navigation：主界面（Home）、数据中心、性能详情、资讯详情、Agent 设置。

- [ ] **Step 3: 完善 `MainActivity.kt`**

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            val viewModel: MainViewModel = hiltViewModel()
            val theme by viewModel.currentTheme.collectAsState()
            EsportsTheme(themeConfig = theme) {
                AppNavigation()
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        // HOME 键按下时回到主界面
    }
}
```

- [ ] **Step 4: 组装主界面 Home 屏**

在 Home Composable 中由底至顶分层组合：
1. **动态背景图层**：根据 Galaxy 主题加载最近常玩游戏的海报作为底图，`Modifier.blur(25.dp)` 模糊 + 78% 暗色遮罩
2. `EcgBackground`（心电图背景层，叠加在底图之上）
3. `GamesScreen`（游戏星系布局，~70%）
4. `PerformancePanel`（右上性能面板）
5. `NewsPanel`（右中资讯面板）
6. `LivestreamPanel`（直播入口集成在资讯内）
7. 数据摘要（右下）
8. `StatusBar`（顶部）
9. `BottomPill`（底部导航）

- [ ] **Step 5: 实现渐进式权限引导流程**

在首次启动时检查 `QUERY_ALL_PACKAGES` 和 `PACKAGE_USAGE_STATS`，弹出说明引导页，解释权限用途和好处，引导用户跳转系统设置授权。被拒绝后降级处理（参考 Spec §7.3）。

- [ ] **Step 5: 集成 SpriteService 启动逻辑**

在 `MainActivity.onCreate` 中检查悬浮窗权限 → 启动 `SpriteService`。

- [ ] **Step 6: 集成 DataCleanupWorker 定时任务**

在 `EsportsSpaceApp.onCreate` 中注册每日凌晨 3:00 的清理任务。

- [ ] **Step 7: 实现性能调节跳转**

性能面板"性能调节 →" 按钮：
```kotlin
val intent = context.packageManager.getLaunchIntentForPackage("com.your.performance.app")
if (intent != null) context.startActivity(intent)
```

- [ ] **Step 8: 全流程编译验证**

```bash
.\gradlew.bat assembleDebug
```
Expected: BUILD SUCCESSFUL

- [ ] **Step 9: 提交**

```bash
git add -A
git commit -m "feat: integrate all modules into main launcher screen with navigation"
```

---

## Task 14: 端到端验证与收尾

- [ ] **Step 1: 在模拟器/平板上安装运行**

```bash
.\gradlew.bat installDebug
```

验证：
- 可设为默认桌面
- 游戏扫描和分类正常
- 心电图背景渲染流畅
- 性能面板数据显示（不可用指标显示"—"）
- 精灵悬浮窗出现且可拖拽
- 点击精灵弹出推荐列表
- 底部导航跳转正常
- 主题切换生效

- [ ] **Step 2: 修复发现的问题**

针对验证中发现的 bug 逐一修复。

- [ ] **Step 3: 添加 README.md**

项目简介、构建说明、模块结构说明。

- [ ] **Step 4: 最终提交**

```bash
git add -A
git commit -m "feat: complete MVP of Esports Space launcher"
```

---

## 依赖关系图

```
Task 1 (脚手架)
├── Task 2 (core-common)
├── Task 3 (core-data)  ← depends on Task 2
├── Task 4 (core-network) ← depends on Task 2
├── Task 5 (core-ui) ← depends on Task 2
│
├── Task 6 (performance) ← depends on Task 3, 5
├── Task 7 (games) ← depends on Task 3, 4, 5
├── Task 8 (news) ← depends on Task 4, 5
├── Task 9 (datacenter) ← depends on Task 3, 5
├── Task 10 (livestream) ← depends on Task 4, 5
├── Task 11 (agent-engine) ← depends on Task 3, 4
├── Task 12 (agent-sprite) ← depends on Task 5, 11
│
├── Task 13 (整合) ← depends on ALL above
└── Task 14 (验证) ← depends on Task 13
```

Tasks 6-12 可并行开发（不同开发者或 subagent）。
