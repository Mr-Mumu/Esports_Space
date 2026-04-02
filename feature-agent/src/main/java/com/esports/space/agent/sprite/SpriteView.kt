package com.esports.space.agent.sprite

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.esports.space.ui.theme.LocalThemeConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt
import kotlin.random.Random

@Composable
fun SpriteView(
    spriteAsset: String,
    animState: SpriteAnimationState,
    onClick: () -> Unit,
    initialPosition: IntOffset = IntOffset(0, 0),
    onPositionChanged: (IntOffset) -> Unit = {},
    onDragFinished: (IntOffset) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var isDragging by remember { mutableStateOf(false) }
    var facingRight by remember { mutableStateOf(true) }
    val customSkinUri = spriteAsset.takeIf { it.startsWith("custom:") }?.removePrefix("custom:")
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("lottie/${resolveSpriteAsset(spriteAsset)}.json")
    )

    BoxWithConstraints(modifier = modifier) {
        val spriteSize = 80.dp
        val spriteSizePx = with(density) { spriteSize.toPx() }
        val maxX = with(density) { maxWidth.toPx() - spriteSizePx }.coerceAtLeast(0f)
        val maxY = with(density) { maxHeight.toPx() - spriteSizePx }.coerceAtLeast(0f)

        LaunchedEffect(maxX, maxY) {
            if (offsetX.value == 0f && offsetY.value == 0f && initialPosition == IntOffset.Zero) {
                offsetX.snapTo(maxX * 0.78f)
                offsetY.snapTo(maxY * 0.72f)
            } else if (offsetX.value == 0f && offsetY.value == 0f) {
                offsetX.snapTo(initialPosition.x.toFloat().coerceIn(0f, maxX))
                offsetY.snapTo(initialPosition.y.toFloat().coerceIn(0f, maxY))
            }
        }

        LaunchedEffect(maxX, maxY, isDragging) {
            while (!isDragging && maxX > 0f && maxY > 0f) {
                delay(Random.nextLong(900L, 2200L))
                if (isDragging) break
                val targetX = (offsetX.value + Random.nextInt(-180, 181)).coerceIn(0f, maxX)
                val targetY = (offsetY.value + Random.nextInt(-110, 111)).coerceIn(0f, maxY)
                facingRight = targetX >= offsetX.value
                val durationMs = Random.nextInt(1100, 2200)
                offsetX.animateTo(targetX, tween(durationMs, easing = LinearOutSlowInEasing))
                offsetY.animateTo(targetY, tween(durationMs, easing = LinearOutSlowInEasing))
            }
        }

        AnimatedSprite(animState = animState, onClick = onClick) { scale, floatY, glowAlpha ->
            val currentOffset = IntOffset(
                offsetX.value.roundToInt(),
                (offsetY.value + floatY).roundToInt()
            )
            onPositionChanged(currentOffset)
            Box(
                modifier = Modifier
                    .offset {
                        currentOffset
                    }
                    .graphicsLayer {
                        scaleX = if (facingRight) scale else -scale
                        scaleY = scale
                    }
                    .pointerInput(maxX, maxY) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                onDragFinished(
                                    IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt())
                                )
                            },
                            onDragCancel = { isDragging = false }
                        ) { change, dragAmount ->
                            change.consume()
                            facingRight = dragAmount.x >= 0f
                            val targetX = (offsetX.value + dragAmount.x).coerceIn(0f, maxX)
                            val targetY = (offsetY.value + dragAmount.y).coerceIn(0f, maxY)
                            scope.launch {
                                offsetX.snapTo(targetX)
                                offsetY.snapTo(targetY)
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = {
                            scope.launch {
                                animState.transitionTo(SpriteState.ACTIVE)
                                animState.transitionTo(SpriteState.IDLE)
                            }
                            onClick()
                        })
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(spriteSize)
                        .shadow(
                            elevation = (8 * glowAlpha).dp,
                            shape = CircleShape,
                            ambientColor = theme.primaryAccent.copy(alpha = glowAlpha),
                            spotColor = theme.primaryAccent.copy(alpha = glowAlpha)
                        )
                        .clip(CircleShape)
                        .background(theme.surface.copy(alpha = 0.1f))
                        .border(1.dp, theme.primaryAccent.copy(alpha = 0.35f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!customSkinUri.isNullOrBlank()) {
                        AsyncImage(
                            model = customSkinUri,
                            contentDescription = "自定义精灵皮肤",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                        )
                    } else if (composition != null) {
                        LottieAnimation(
                            composition = composition,
                            iterations = LottieConstants.IterateForever,
                            modifier = Modifier.size(64.dp)
                        )
                    } else {
                        val style = styleForSprite(spriteAsset)
                        FallbackSpriteVisual(
                            style = style,
                            pulse = glowAlpha,
                            isWalking = abs(floatY) > 3f
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FallbackSpriteVisual(
    style: SpriteFallbackStyle,
    pulse: Float,
    isWalking: Boolean
) {
    Box(modifier = Modifier.size(64.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(style.primary.copy(alpha = 0.9f), style.secondary.copy(alpha = 0.22f))
                )
            )
        }
        Canvas(
            modifier = Modifier
                .size(56.dp)
                .graphicsLayer { alpha = 0.45f + pulse * 0.25f }
        ) {
            drawCircle(color = style.secondary.copy(alpha = 0.35f))
        }
        Icon(
            imageVector = style.icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.88f),
            modifier = Modifier
                .size(if (isWalking) 24.dp else 20.dp)
                .graphicsLayer { alpha = 0.65f + pulse * 0.35f }
        )
    }
}

private fun resolveSpriteAsset(id: String): String = when (id) {
    "flame", "ice", "forest", "shadow", "gold", "default" -> id
    else -> "default"
}

private data class SpriteFallbackStyle(
    val primary: Color,
    val secondary: Color,
    val icon: ImageVector
)

private fun styleForSprite(id: String): SpriteFallbackStyle = when (id) {
    "flame" -> SpriteFallbackStyle(
        primary = Color(0xFFFF6B35),
        secondary = Color(0xFFFFB26A),
        icon = Icons.Default.LocalFireDepartment
    )
    "ice" -> SpriteFallbackStyle(
        primary = Color(0xFF4FC3F7),
        secondary = Color(0xFFB3E5FC),
        icon = Icons.Default.AcUnit
    )
    "forest" -> SpriteFallbackStyle(
        primary = Color(0xFF66BB6A),
        secondary = Color(0xFFA5D6A7),
        icon = Icons.Default.Park
    )
    "shadow" -> SpriteFallbackStyle(
        primary = Color(0xFF8E24AA),
        secondary = Color(0xFFCE93D8),
        icon = Icons.Default.NightlightRound
    )
    "gold" -> SpriteFallbackStyle(
        primary = Color(0xFFFFD54F),
        secondary = Color(0xFFFFF59D),
        icon = Icons.Default.Star
    )
    else -> SpriteFallbackStyle(
        primary = Color(0xFF6C63FF),
        secondary = Color(0xFFB39DDB),
        icon = Icons.Default.SportsEsports
    )
}
