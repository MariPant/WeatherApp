package com.app.weatherapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val APPEAR_DURATION_MILLIS = 450
private const val APPEAR_STAGGER_MILLIS = 70L
private val AppearOffset = 24.dp

/**
 * Fades and lifts a block in the first time it is shown, staggered by [index]. The flag is kept
 * with rememberSaveable so items do not re-animate when scrolled back into a lazy list.
 */
@Composable
fun Modifier.appearAnimation(index: Int): Modifier {
    var hasAppeared by rememberSaveable { mutableStateOf(false) }
    val progress = remember { Animatable(if (hasAppeared) 1f else 0f) }
    LaunchedEffect(Unit) {
        if (progress.value < 1f) {
            delay(index * APPEAR_STAGGER_MILLIS)
            progress.animateTo(1f, tween(APPEAR_DURATION_MILLIS, easing = FastOutSlowInEasing))
        }
        hasAppeared = true
    }
    return graphicsLayer {
        alpha = progress.value
        translationY = (1f - progress.value) * AppearOffset.toPx()
    }
}
