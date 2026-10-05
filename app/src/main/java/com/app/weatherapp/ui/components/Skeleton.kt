package com.app.weatherapp.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import com.app.weatherapp.ui.theme.Dimens

private const val SHIMMER_DURATION_MILLIS = 1_300
private const val SHIMMER_WIDTH_FACTOR = 2f

private val LocalShimmerProgress = compositionLocalOf<State<Float>> { mutableFloatStateOf(0f) }

/**
 * Drives one shared shimmer animation for all [SkeletonBlock]s inside, so they move in sync.
 * The animated value is read only in the draw phase: no recomposition per frame.
 */
@Composable
fun ShimmerContainer(content: @Composable () -> Unit) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val progress = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(SHIMMER_DURATION_MILLIS, easing = LinearEasing), RepeatMode.Restart),
        label = "shimmerProgress",
    )
    CompositionLocalProvider(LocalShimmerProgress provides progress, content = content)
}

@Composable
fun SkeletonBlock(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(Dimens.SpaceS),
    baseColor: Color,
    highlightColor: Color,
) {
    val progress = LocalShimmerProgress.current
    Box(
        modifier = modifier
            .clip(shape)
            .drawBehind {
                drawRect(baseColor)
                val sweep = size.width * SHIMMER_WIDTH_FACTOR
                val x = -sweep + progress.value * (size.width + sweep)
                drawRect(
                    Brush.linearGradient(
                        colors = listOf(Color.Transparent, highlightColor, Color.Transparent),
                        start = Offset(x, 0f),
                        end = Offset(x + sweep, size.height),
                    ),
                )
            },
    )
}
