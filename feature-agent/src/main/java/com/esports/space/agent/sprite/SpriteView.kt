package com.esports.space.agent.sprite

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.esports.space.ui.theme.LocalThemeConfig
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun SpriteView(
    spriteAsset: String,
    animState: SpriteAnimationState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalThemeConfig.current
    val scope = rememberCoroutineScope()

    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("lottie/$spriteAsset.json")
    )

    AnimatedSprite(
        animState = animState,
        onClick = onClick
    ) { scale, floatY, glowAlpha ->
        Box(
            modifier = modifier
                .offset { IntOffset(offsetX.roundToInt(), (offsetY + floatY).roundToInt()) }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            scope.launch {
                                animState.transitionTo(SpriteState.ACTIVE)
                                animState.transitionTo(SpriteState.IDLE)
                            }
                            onClick()
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(
                        elevation = (8 * glowAlpha).dp,
                        shape = CircleShape,
                        ambientColor = theme.primaryAccent.copy(alpha = glowAlpha),
                        spotColor = theme.primaryAccent.copy(alpha = glowAlpha)
                    )
                    .clip(CircleShape)
                    .background(theme.surface.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = composition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(64.dp)
                )
            }
        }
    }
}
