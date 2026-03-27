package com.esports.space.agent.sprite

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class SpriteState { IDLE, ACTIVE, SHOWING_RECOMMENDATION }

@Stable
class SpriteAnimationState {
    var currentState by mutableStateOf(SpriteState.IDLE)
        internal set

    internal val scaleAnim = Animatable(1f)
    internal val bounceAnim = Animatable(0f)

    suspend fun transitionTo(target: SpriteState) {
        currentState = target
        when (target) {
            SpriteState.ACTIVE -> {
                scaleAnim.animateTo(1.15f, spring(dampingRatio = 0.4f, stiffness = 600f))
                scaleAnim.animateTo(1f, spring(dampingRatio = 0.5f))
            }
            SpriteState.SHOWING_RECOMMENDATION -> {
                scaleAnim.animateTo(1.2f, tween(300, easing = FastOutSlowInEasing))
            }
            SpriteState.IDLE -> {
                scaleAnim.animateTo(1f, tween(400))
            }
        }
    }

    suspend fun bounce() {
        bounceAnim.animateTo(-12f, tween(120))
        bounceAnim.animateTo(0f, spring(dampingRatio = 0.3f, stiffness = 400f))
    }
}

@Composable
fun rememberSpriteAnimationState(): SpriteAnimationState {
    return remember { SpriteAnimationState() }
}

@Composable
fun idleFloatOffset(): State<Float> {
    val transition = rememberInfiniteTransition(label = "sprite_float")
    return transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "float_offset"
    )
}

@Composable
fun idleGlowAlpha(): State<Float> {
    val transition = rememberInfiniteTransition(label = "sprite_glow")
    return transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
}

@Composable
fun AnimatedSprite(
    animState: SpriteAnimationState,
    onClick: () -> Unit,
    content: @Composable (scale: Float, floatY: Float, glowAlpha: Float) -> Unit
) {
    val floatOffset by idleFloatOffset()
    val glowAlpha by idleGlowAlpha()

    LaunchedEffect(animState.currentState) {
        if (animState.currentState == SpriteState.ACTIVE) {
            animState.bounce()
        }
    }

    content(animState.scaleAnim.value, floatOffset + animState.bounceAnim.value, glowAlpha)
}
