package com.app.weatherapp.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Sets status/navigation bar icon colour for the current screen. Weather screens always sit on a
 * dark gradient (light icons); list screens follow the app theme.
 */
@Composable
fun SystemBarIcons(useDarkIcons: Boolean) {
    val view = LocalView.current
    if (view.isInEditMode) return
    LaunchedEffect(useDarkIcons) {
        val window = view.context.findActivity()?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, view).apply {
            isAppearanceLightStatusBars = useDarkIcons
            isAppearanceLightNavigationBars = useDarkIcons
        }
    }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
