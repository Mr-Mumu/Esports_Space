package com.esports.space.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.esports.space.agent.sprite.BubbleDialog
import com.esports.space.agent.sprite.SpriteView
import com.esports.space.agent.sprite.rememberSpriteAnimationState
import com.esports.space.agent.ui.AgentViewModel
import com.esports.space.agent.ui.RecommendationListPanel
import com.esports.space.games.ui.GamesScreen
import com.esports.space.livestream.ui.LivestreamPanel
import com.esports.space.livestream.ui.LivestreamViewModel
import com.esports.space.news.ui.NewsPanel
import com.esports.space.news.ui.NewsViewModel
import com.esports.space.performance.ui.PerformancePanel
import com.esports.space.performance.ui.PerformanceViewModel
import com.esports.space.ui.component.BottomPill
import com.esports.space.ui.component.EcgBackground
import com.esports.space.ui.component.GlassCard
import com.esports.space.ui.component.StatusBar
import com.esports.space.ui.theme.LocalThemeConfig

@Composable
fun HomeScreen(navController: NavController) {
    val theme = LocalThemeConfig.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val performanceVm: PerformanceViewModel = hiltViewModel()
    val newsVm: NewsViewModel = hiltViewModel()
    val livestreamVm: LivestreamViewModel = hiltViewModel()
    val agentVm: AgentViewModel = hiltViewModel()

    val perfState by performanceVm.uiState.collectAsStateWithLifecycle()
    val newsState by newsVm.uiState.collectAsStateWithLifecycle()
    val livestreamState by livestreamVm.uiState.collectAsStateWithLifecycle()
    val agentState by agentVm.uiState.collectAsStateWithLifecycle()
    var spriteAnchor by remember { mutableStateOf(IntOffset(agentState.spritePosX, agentState.spritePosY)) }
    var panelDragOffset by remember { mutableStateOf(IntOffset(agentState.panelOffsetX, agentState.panelOffsetY)) }
    var spriteLayerVisible by remember { mutableStateOf(true) }

    LaunchedEffect(agentState.spritePosX, agentState.spritePosY) {
        spriteAnchor = IntOffset(agentState.spritePosX, agentState.spritePosY)
    }

    LaunchedEffect(agentState.panelOffsetX, agentState.panelOffsetY) {
        panelDragOffset = IntOffset(agentState.panelOffsetX, agentState.panelOffsetY)
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> spriteLayerVisible = true
                Lifecycle.Event.ON_PAUSE,
                Lifecycle.Event.ON_STOP -> spriteLayerVisible = false
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

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

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            theme.background.copy(alpha = 0.22f),
                            theme.background.copy(alpha = 0.52f)
                        )
                    )
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            theme.primaryAccent.copy(alpha = 0.16f),
                            Color.Transparent
                        ),
                        radius = 1200f
                    )
                )
        )

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
        AnimatedVisibility(
            visible = agentState.isEnabled && spriteLayerVisible,
            enter = fadeIn(animationSpec = tween(420)) + scaleIn(initialScale = 0.92f),
            exit = fadeOut(animationSpec = tween(220)) + scaleOut(targetScale = 0.92f)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                SpriteView(
                    spriteAsset = agentState.spriteAppearance,
                    animState = spriteAnimState,
                    onClick = { agentVm.onSpriteTapped() },
                    initialPosition = IntOffset(agentState.spritePosX, agentState.spritePosY),
                    onPositionChanged = { spriteAnchor = it },
                    onDragFinished = { pos ->
                        agentVm.persistSpritePosition(pos.x, pos.y)
                    },
                    modifier = Modifier.fillMaxSize()
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
                        .align(Alignment.BottomEnd)
                        .padding(end = 112.dp, bottom = 180.dp)
                )

                AnimatedVisibility(
                    visible = agentState.showRecommendationPanel,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.offset {
                        IntOffset(
                            x = (spriteAnchor.x - 360 + panelDragOffset.x).coerceAtLeast(0),
                            y = (spriteAnchor.y - 180 + panelDragOffset.y).coerceAtLeast(0)
                        )
                    }
                ) {
                    GlassCard {
                        RecommendationListPanel(
                            events = agentState.recentList,
                            onAccept = { agentVm.acceptEvent(it) },
                            onDismiss = { agentVm.dismissEvent(it) },
                            maxItems = 5,
                            modifier = Modifier
                                .fillMaxWidth(0.36f)
                                .padding(vertical = 8.dp)
                                .pointerInput(Unit) {
                                    detectDragGestures(
                                        onDragEnd = {
                                            agentVm.persistPanelOffset(
                                                panelDragOffset.x,
                                                panelDragOffset.y
                                            )
                                        }
                                    ) { change, dragAmount ->
                                        change.consume()
                                        panelDragOffset = IntOffset(
                                            panelDragOffset.x + dragAmount.x.toInt(),
                                            panelDragOffset.y + dragAmount.y.toInt()
                                        )
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}
