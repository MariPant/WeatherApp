package com.app.weatherapp.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.app.weatherapp.R
import com.app.weatherapp.presentation.common.TimeFormats
import com.app.weatherapp.presentation.home.SunUi
import com.app.weatherapp.ui.components.CardGlyph
import com.app.weatherapp.ui.components.CardHeader
import com.app.weatherapp.ui.components.GlassCard
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather
import com.app.weatherapp.ui.theme.SunCore
import kotlin.math.PI
import kotlin.math.sin

private const val ARC_SAMPLES = 48
private const val HORIZON_FRACTION = 0.82f
private const val ARC_PEAK_FRACTION = 0.72f
private const val FUTURE_PATH_ALPHA = 0.35f
private const val HORIZON_ALPHA = 0.3f
private const val GLOW_ALPHA = 0.3f
private val DashOn = 4.dp
private val DashOff = 4.dp

/** Sunrise and sunset times with the sun's path across the sky and its current position. */
@Composable
fun SunCard(sun: SunUi, formats: TimeFormats, modifier: Modifier = Modifier) {
    val sunrise = sun.sunrise.format(formats.time)
    val sunset = sun.sunset.format(formats.time)
    val sunriseLabel = stringResource(R.string.sunrise)
    val sunsetLabel = stringResource(R.string.sunset)

    GlassCard(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "$sunriseLabel $sunrise, $sunsetLabel $sunset"
        },
    ) {
        CardHeader(title = stringResource(R.string.sunrise_sunset), glyph = CardGlyph.SUNRISE)
        SunArc(progress = sun.progress, modifier = Modifier.fillMaxWidth().height(Dimens.SunArcHeight).padding(vertical = Dimens.SpaceS))
        Row(verticalAlignment = Alignment.Bottom) {
            SunTime(label = sunriseLabel, time = sunrise, alignEnd = false)
            Spacer(Modifier.weight(1f))
            SunTime(label = sunsetLabel, time = sunset, alignEnd = true)
        }
    }
}

@Composable
private fun SunTime(label: String, time: String, alignEnd: Boolean) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA))
        Text(time, style = MaterialTheme.typography.headlineSmall, color = OnWeather)
    }
}

@Composable
private fun SunArc(progress: Float?, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val horizonY = size.height * HORIZON_FRACTION
        val peak = size.height * ARC_PEAK_FRACTION
        val strokeWidth = 2.dp.toPx()
        fun pointAt(t: Float) = Offset(t * size.width, horizonY - sin(PI * t).toFloat() * peak)

        val arc = Path().apply {
            moveTo(0f, horizonY)
            for (i in 1..ARC_SAMPLES) {
                val point = pointAt(i / ARC_SAMPLES.toFloat())
                lineTo(point.x, point.y)
            }
        }

        drawLine(
            color = OnWeather.copy(alpha = HORIZON_ALPHA),
            start = Offset(0f, horizonY),
            end = Offset(size.width, horizonY),
            strokeWidth = 1.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashOn.toPx(), DashOff.toPx())),
        )
        // Upcoming part of the path: faint and dashed.
        drawPath(
            path = arc,
            color = OnWeather.copy(alpha = FUTURE_PATH_ALPHA),
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(DashOn.toPx(), DashOff.toPx())),
            ),
        )
        if (progress != null) {
            // Travelled part: solid, clipped at the sun's current x position.
            val sunPosition = pointAt(progress)
            clipRect(right = sunPosition.x) {
                drawPath(arc, OnWeather, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
            val sunRadius = 7.dp.toPx()
            drawCircle(SunCore.copy(alpha = GLOW_ALPHA), radius = sunRadius * 2f, center = sunPosition)
            drawCircle(SunCore, radius = sunRadius, center = sunPosition)
        }
    }
}
