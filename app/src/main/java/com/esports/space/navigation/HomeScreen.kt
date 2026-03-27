package com.esports.space.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.esports.space.MainViewModel
import com.esports.space.agent.sprite.BubbleDialog
import com.esports.space.agent.sprite.SpriteView
import com.esports.space.agent.sprite.rememberSpriteAnimationState
import com.esports.space.agent.ui.AgentViewModel
import com.esports.space.games.ui.GamesScreen
import com.esports.space.livestream.ui.LivestreamPanel
import com.esports.space.livestream.ui.LivestreamViewModel
import com.esports.space.news.ui.NewsPanel
import com.esports.space.news.ui.NewsViewModel
import com.esports.space.performance.ui.PerformancePanel
import com.esports.space.performance.ui.PerformanceViewModel
import com.esports.space.ui.component.BottomPill
import com.esports.space.ui.component.EcgBackground
import com.esports.space.ui.component.StatusBar
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun HomeScreen(
    navController: NavController,
    mainViewModel: MainViewModel
) {
    val theme = LocalThemeConfig.current
    val context = LocalContext.current

    val performanceVm: PerformanceViewModel = hiltViewModel()
    val newsVm: NewsViewModel = hiltViewModel()
    val livestreamVm: LivestreamViewModel = hiltViewModel()
    val agentVm: AgentViewModel = hiltViewModel()

    val perfState by performanceVm.uiState.collectAsStateWithLifecycle()
    val newsState by newsVm.uiState.collectAsStateWithLifecycle()
    val livestreamState by livestreamVm.uiState.collectAsStateWithLifecycle()
    val agentState by agentVm.uiState.collectAsStateWithLifecycle()

    val spriteAnimState = rememberSpriteAnimationState()

    Box(modifier = Modifier.fillMaxSize()) {

        // Layer 1 — Dynamic blurred game backdrop (Galaxy theme only)
        if (theme.usesGameBackdrop) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = null, // Poster would come from last-played game; placeholder for now
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(25.dp)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.78f))
                )
            }
        }

        // Layer 2 — ECG performance heartbeat background
        EcgBackground(
            cpuFreqHistory = perfState.cpuFreqHistory,
            gpuFreqHistory = perfState.gpuFreqHistory,
            modifier = Modifier.fillMaxSize()
        )

        // Layer 3 — Main content: landscape Row layout
        Row(modifier = Modifier.fillMaxSize()) {
            // Left ~70%: Games grid
            Box(
                modifier = Modifier
                    .weight(0.7f)
                    .fillMaxHeight()
            ) {
                GamesScreen()
            }

            // Right ~30%: Side panels
            Column(
                modifier = Modifier
                    .weight(0.3f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
                    .padding(end = 8.dp, top = 32.dp, bottom = 72.dp)
            ) {
                PerformancePanel(
                    metrics = perfState.currentMetrics,
                    onTuneClick = { /* Launch performance tuning app */ },
                    onDetailClick = { navController.navigate(Routes.PERFORMANCE_DETAIL) }
                )

                NewsPanel(
                    newsList = newsState.newsList,
                    onNewsClick = { item ->
                        navController.navigate(Routes.newsDetail(item.detailUrl))
                    },
                    onMoreClick = { /* Could navigate to a full news list */ }
                )

                LivestreamPanel(
                    liveStreams = livestreamState.liveStreams,
                    onStreamClick = { item ->
                        livestreamVm.onStreamClick(item, context)
                    },
                    onMoreClick = { navController.navigate(Routes.VIDEO_LIST) }
                )
            }
        }

        // Layer 4 — StatusBar at top
        StatusBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        )

        // Layer 5 — BottomPill navigation
        BottomPill(
            onDataCenter = { navController.navigate(Routes.DATA_CENTER) },
            onSettings = { navController.navigate(Routes.AGENT_SETTINGS) },
            onSearch = { /* Search overlay — future enhancement */ },
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        // Layer 6 — Agent sprite overlay
        if (agentState.isEnabled) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 96.dp)
            ) {
                SpriteView(
                    spriteAsset = agentState.spriteAppearance,
                    animState = spriteAnimState,
                    onClick = { agentVm.closeBubble() }
                )
                BubbleDialog(
                    visible = agentState.showBubble,
                    action = agentState.currentRecommendation,
                    onAccept = {
                        agentState.currentRecommendation?.let { agentVm.acceptRecommendation(it) }
                    },
                    onDismiss = {
                        agentState.currentRecommendation?.let { agentVm.dismissRecommendation(it) }
                    },
                    onClose = { agentVm.closeBubble() },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(bottom = 8.dp)
                )
            }
        }
    }
}
