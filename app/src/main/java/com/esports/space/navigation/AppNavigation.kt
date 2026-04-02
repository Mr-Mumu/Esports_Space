package com.esports.space.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.esports.space.agent.sprite.SpriteSettingsScreen
import com.esports.space.agent.ui.AgentViewModel
import com.esports.space.datacenter.ui.DataCenterScreen
import com.esports.space.datacenter.ui.DataCenterViewModel
import com.esports.space.livestream.ui.LivestreamViewModel
import com.esports.space.livestream.ui.VideoListScreen
import com.esports.space.livestream.ui.WebViewPlayer
import com.esports.space.news.ui.NewsDetailScreen
import com.esports.space.news.ui.NewsViewModel
import com.esports.space.performance.ui.PerformanceDetailScreen
import com.esports.space.performance.ui.PerformanceViewModel
import java.net.URLDecoder
import java.net.URLEncoder

object Routes {
    const val HOME = "home"
    const val DATA_CENTER = "datacenter"
    const val PERFORMANCE_DETAIL = "performance_detail"
    const val NEWS_DETAIL = "news_detail/{url}"
    const val AGENT_SETTINGS = "agent_settings"
    const val VIDEO_LIST = "video_list"
    const val WEBVIEW_PLAYER = "webview_player/{url}"

    fun newsDetail(url: String): String =
        "news_detail/${URLEncoder.encode(url, "UTF-8")}"

    fun webviewPlayer(url: String): String =
        "webview_player/${URLEncoder.encode(url, "UTF-8")}"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {

        composable(Routes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(Routes.DATA_CENTER) {
            val viewModel: DataCenterViewModel = hiltViewModel()
            DataCenterScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.PERFORMANCE_DETAIL) {
            val viewModel: PerformanceViewModel = hiltViewModel()
            PerformanceDetailScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onTuneClick = { /* Performance tuning app launch */ }
            )
        }

        composable(
            route = Routes.NEWS_DETAIL,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = URLDecoder.decode(encodedUrl, "UTF-8")
            NewsDetailScreen(
                url = url,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.AGENT_SETTINGS) {
            val viewModel: AgentViewModel = hiltViewModel()
            SpriteSettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.VIDEO_LIST) {
            val viewModel: LivestreamViewModel = hiltViewModel()
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            VideoListScreen(
                videos = uiState.videoHighlights,
                onVideoClick = { video ->
                    navController.navigate(Routes.webviewPlayer(video.videoUrl))
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.WEBVIEW_PLAYER,
            arguments = listOf(navArgument("url") { type = NavType.StringType })
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
            val url = URLDecoder.decode(encodedUrl, "UTF-8")
            WebViewPlayer(
                url = url,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
