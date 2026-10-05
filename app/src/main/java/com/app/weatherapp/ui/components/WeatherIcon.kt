package com.app.weatherapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.app.weatherapp.presentation.common.WeatherIconType
import com.app.weatherapp.ui.theme.CloudDark
import com.app.weatherapp.ui.theme.CloudDarkShade
import com.app.weatherapp.ui.theme.CloudLight
import com.app.weatherapp.ui.theme.CloudLightShade
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.Lightning
import com.app.weatherapp.ui.theme.MoonColor
import com.app.weatherapp.ui.theme.RainDrop
import com.app.weatherapp.ui.theme.SnowFlake
import com.app.weatherapp.ui.theme.SunCore
import com.app.weatherapp.ui.theme.SunEdge
import kotlin.math.cos
import kotlin.math.sin

/**
 * Multicolour weather glyphs drawn with Canvas: crisp at any size, no image assets, and they
 * pick up no proprietary artwork. Pass a [contentDescription] when the icon carries meaning that
 * is not already in nearby text; pass null for decorative use.
 */
@Composable
fun WeatherIcon(
    type: WeatherIconType,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = Dimens.IconMedium,
) {
    val semanticsModifier = if (contentDescription != null) {
        Modifier.semantics {
            this.contentDescription = contentDescription
            role = Role.Image
        }
    } else {
        Modifier.clearAndSetSemantics { }
    }
    Canvas(modifier = modifier.size(size).then(semanticsModifier)) {
        drawWeatherGlyph(type)
    }
}

private const val FULL_CIRCLE_DEGREES = 360.0
private const val SUN_RAYS = 8
private const val PREVIEW_ICONS_PER_ROW = 5

/** Draws on a square canvas; all coordinates are fractions of its side so the glyph scales. */
private fun DrawScope.drawWeatherGlyph(type: WeatherIconType) {
    val s = size.minDimension
    fun p(x: Float, y: Float) = Offset(x * s, y * s)

    when (type) {
        WeatherIconType.SUN -> drawSun(p(0.5f, 0.5f), 0.22f * s)
        WeatherIconType.MOON -> drawMoon(p(0.5f, 0.5f), 0.32f * s)
        WeatherIconType.PARTLY_CLOUDY_DAY -> {
            drawSun(p(0.62f, 0.36f), 0.16f * s)
            drawCloud(p(0.06f, 0.4f), 0.76f * s, 0.44f * s, dark = false)
        }
        WeatherIconType.PARTLY_CLOUDY_NIGHT -> {
            drawMoon(p(0.64f, 0.34f), 0.22f * s)
            drawCloud(p(0.06f, 0.4f), 0.76f * s, 0.44f * s, dark = false)
        }
        WeatherIconType.CLOUD -> drawCloud(p(0.08f, 0.24f), 0.84f * s, 0.5f * s, dark = false)
        WeatherIconType.FOG -> {
            drawCloud(p(0.14f, 0.1f), 0.72f * s, 0.42f * s, dark = false)
            drawHorizontalLines(
                lines = listOf(0.66f to (0.12f..0.82f), 0.78f to (0.2f..0.9f), 0.9f to (0.12f..0.7f)),
                strokeWidth = 0.06f * s,
                color = CloudLightShade,
            )
        }
        WeatherIconType.DRIZZLE -> {
            drawCloud(p(0.1f, 0.12f), 0.8f * s, 0.46f * s, dark = true)
            drawDrops(listOf(0.34f, 0.54f, 0.74f), top = 0.7f, length = 0.12f)
        }
        WeatherIconType.RAIN -> {
            drawCloud(p(0.1f, 0.12f), 0.8f * s, 0.46f * s, dark = true)
            drawDrops(listOf(0.32f, 0.52f, 0.72f), top = 0.66f, length = 0.22f)
        }
        WeatherIconType.HEAVY_RAIN -> {
            drawCloud(p(0.1f, 0.1f), 0.8f * s, 0.46f * s, dark = true)
            drawDrops(listOf(0.26f, 0.42f, 0.58f, 0.74f), top = 0.64f, length = 0.26f)
        }
        WeatherIconType.SLEET -> {
            drawCloud(p(0.1f, 0.12f), 0.8f * s, 0.46f * s, dark = true)
            drawDrops(listOf(0.32f, 0.64f), top = 0.66f, length = 0.2f)
            drawSnowflake(p(0.48f, 0.82f), 0.07f * s)
        }
        WeatherIconType.SNOW -> {
            drawCloud(p(0.1f, 0.12f), 0.8f * s, 0.46f * s, dark = false)
            drawSnowflake(p(0.3f, 0.76f), 0.08f * s)
            drawSnowflake(p(0.52f, 0.86f), 0.08f * s)
            drawSnowflake(p(0.72f, 0.74f), 0.08f * s)
        }
        WeatherIconType.THUNDERSTORM -> {
            drawCloud(p(0.1f, 0.08f), 0.8f * s, 0.46f * s, dark = true)
            drawBolt(s)
        }
        WeatherIconType.WIND -> drawWind(s)
        WeatherIconType.SUNRISE -> drawSunHorizon(s, rising = true)
        WeatherIconType.SUNSET -> drawSunHorizon(s, rising = false)
    }
}

private fun DrawScope.drawSun(center: Offset, radius: Float) {
    val rayStart = radius * 1.38f
    val rayEnd = radius * 1.8f
    repeat(SUN_RAYS) { index ->
        val angle = Math.toRadians(index * FULL_CIRCLE_DEGREES / SUN_RAYS)
        val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
        drawLine(
            color = SunCore,
            start = center + direction * rayStart,
            end = center + direction * rayEnd,
            strokeWidth = radius * 0.22f,
            cap = StrokeCap.Round,
        )
    }
    drawCircle(
        brush = Brush.radialGradient(listOf(SunCore, SunEdge), center = center, radius = radius),
        radius = radius,
        center = center,
    )
}

private fun DrawScope.drawMoon(center: Offset, radius: Float) {
    val disc = Path().apply { addOval(Rect(center, radius)) }
    val bite = Path().apply {
        addOval(Rect(center + Offset(radius * 0.55f, -radius * 0.35f), radius * 0.82f))
    }
    val crescent = Path().apply { op(disc, bite, PathOperation.Difference) }
    drawPath(crescent, MoonColor)
}

private fun DrawScope.drawCloud(topLeft: Offset, width: Float, height: Float, dark: Boolean) {
    val colors = if (dark) listOf(CloudDark, CloudDarkShade) else listOf(CloudLight, CloudLightShade)
    val brush = Brush.verticalGradient(colors, startY = topLeft.y, endY = topLeft.y + height)
    val baseTop = topLeft.y + height * 0.45f
    val baseHeight = height * 0.55f
    drawRoundRect(
        brush = brush,
        topLeft = Offset(topLeft.x, baseTop),
        size = Size(width, baseHeight),
        cornerRadius = CornerRadius(baseHeight / 2f),
    )
    drawCircle(brush, radius = height * 0.3f, center = Offset(topLeft.x + width * 0.3f, topLeft.y + height * 0.55f))
    drawCircle(brush, radius = height * 0.42f, center = Offset(topLeft.x + width * 0.6f, topLeft.y + height * 0.42f))
}

private fun DrawScope.drawDrops(xs: List<Float>, top: Float, length: Float) {
    val s = size.minDimension
    xs.forEach { x ->
        drawLine(
            color = RainDrop,
            start = Offset(x * s, top * s),
            end = Offset((x - length * 0.3f) * s, (top + length) * s),
            strokeWidth = 0.055f * s,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawSnowflake(center: Offset, radius: Float) {
    repeat(3) { index ->
        val angle = Math.toRadians(index * 60.0 + 90.0)
        val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat()) * radius
        drawLine(SnowFlake, center - direction, center + direction, strokeWidth = radius * 0.35f, cap = StrokeCap.Round)
    }
}

private fun DrawScope.drawBolt(s: Float) {
    val bolt = Path().apply {
        moveTo(0.54f * s, 0.5f * s)
        lineTo(0.36f * s, 0.75f * s)
        lineTo(0.5f * s, 0.75f * s)
        lineTo(0.43f * s, 0.96f * s)
        lineTo(0.68f * s, 0.66f * s)
        lineTo(0.54f * s, 0.66f * s)
        lineTo(0.62f * s, 0.5f * s)
        close()
    }
    drawPath(bolt, Lightning)
}

private fun DrawScope.drawHorizontalLines(
    lines: List<Pair<Float, ClosedFloatingPointRange<Float>>>,
    strokeWidth: Float,
    color: Color,
) {
    val s = size.minDimension
    lines.forEach { (y, xRange) ->
        drawLine(
            color = color,
            start = Offset(xRange.start * s, y * s),
            end = Offset(xRange.endInclusive * s, y * s),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

private fun DrawScope.drawWind(s: Float) {
    val stroke = Stroke(width = 0.065f * s, cap = StrokeCap.Round)
    fun gust(y: Float, endX: Float, curlUp: Boolean) {
        val r = 0.09f * s
        val direction = if (curlUp) -1f else 1f
        val path = Path().apply {
            moveTo(0.1f * s, y * s)
            lineTo(endX * s, y * s)
            cubicTo(
                endX * s + r * 1.5f, y * s,
                endX * s + r * 1.5f, y * s + direction * r * 2f,
                endX * s, y * s + direction * r * 2f,
            )
        }
        drawPath(path, CloudLight, style = stroke)
    }
    gust(y = 0.38f, endX = 0.62f, curlUp = true)
    gust(y = 0.54f, endX = 0.76f, curlUp = true)
    gust(y = 0.7f, endX = 0.52f, curlUp = false)
}

private fun DrawScope.drawSunHorizon(s: Float, rising: Boolean) {
    val horizon = 0.66f * s
    clipRect(bottom = horizon) {
        drawSun(Offset(0.5f * s, horizon), 0.2f * s)
    }
    drawLine(CloudLight, Offset(0.08f * s, horizon), Offset(0.92f * s, horizon), 0.05f * s, StrokeCap.Round)
    // Small chevron under the horizon: up for sunrise, down for sunset.
    val tipY = if (rising) 0.78f * s else 0.92f * s
    val baseY = if (rising) 0.92f * s else 0.78f * s
    val chevron = Path().apply {
        moveTo(0.4f * s, baseY)
        lineTo(0.5f * s, tipY)
        lineTo(0.6f * s, baseY)
    }
    drawPath(chevron, CloudLight, style = Stroke(width = 0.05f * s, cap = StrokeCap.Round))
}

@Preview(backgroundColor = 0xFF1E62C4, showBackground = true)
@Composable
private fun WeatherIconsPreview() {
    Column {
        WeatherIconType.entries.chunked(PREVIEW_ICONS_PER_ROW).forEach { row ->
            Row { row.forEach { WeatherIcon(it, contentDescription = null, size = Dimens.IconLarge) } }
        }
    }
}
