package com.app.weatherapp.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

/** Monochrome mini glyphs for card headers. Decorative: the header text carries the meaning. */
enum class CardGlyph { CLOCK, CALENDAR, THERMOMETER, DROP, WIND, SUN, EYE, GAUGE, SUNRISE }

private const val STROKE_FRACTION = 0.11f
private const val SUN_RAY_COUNT = 8
private const val DEGREES_PER_RAY = 45.0

internal fun DrawScope.drawCardGlyph(glyph: CardGlyph, color: Color) {
    val s = size.minDimension
    val strokeWidth = s * STROKE_FRACTION
    val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Round)
    val center = Offset(s / 2f, s / 2f)

    when (glyph) {
        CardGlyph.CLOCK -> {
            drawCircle(color, radius = s * 0.44f, center = center, style = stroke)
            drawLine(color, center, Offset(s * 0.5f, s * 0.24f), strokeWidth, StrokeCap.Round)
            drawLine(color, center, Offset(s * 0.7f, s * 0.6f), strokeWidth, StrokeCap.Round)
        }
        CardGlyph.CALENDAR -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(s * 0.08f, s * 0.16f),
                size = Size(s * 0.84f, s * 0.76f),
                cornerRadius = CornerRadius(s * 0.14f),
                style = stroke,
            )
            drawLine(color, Offset(s * 0.08f, s * 0.4f), Offset(s * 0.92f, s * 0.4f), strokeWidth)
            drawLine(color, Offset(s * 0.3f, s * 0.04f), Offset(s * 0.3f, s * 0.24f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(s * 0.7f, s * 0.04f), Offset(s * 0.7f, s * 0.24f), strokeWidth, StrokeCap.Round)
        }
        CardGlyph.THERMOMETER -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(s * 0.38f, s * 0.04f),
                size = Size(s * 0.24f, s * 0.62f),
                cornerRadius = CornerRadius(s * 0.12f),
                style = stroke,
            )
            drawCircle(color, radius = s * 0.2f, center = Offset(s * 0.5f, s * 0.76f))
        }
        CardGlyph.DROP -> {
            val drop = Path().apply {
                moveTo(s * 0.5f, s * 0.04f)
                cubicTo(s * 0.5f, s * 0.04f, s * 0.14f, s * 0.48f, s * 0.14f, s * 0.64f)
                cubicTo(s * 0.14f, s * 0.84f, s * 0.3f, s * 0.96f, s * 0.5f, s * 0.96f)
                cubicTo(s * 0.7f, s * 0.96f, s * 0.86f, s * 0.84f, s * 0.86f, s * 0.64f)
                cubicTo(s * 0.86f, s * 0.48f, s * 0.5f, s * 0.04f, s * 0.5f, s * 0.04f)
                close()
            }
            drawPath(drop, color)
        }
        CardGlyph.WIND -> {
            listOf(0.3f to 0.72f, 0.52f to 0.92f, 0.74f to 0.6f).forEach { (y, endX) ->
                drawLine(color, Offset(s * 0.06f, s * y), Offset(s * endX, s * y), strokeWidth, StrokeCap.Round)
            }
        }
        CardGlyph.SUN -> {
            drawCircle(color, radius = s * 0.2f, center = center)
            repeat(SUN_RAY_COUNT) { index ->
                val angle = Math.toRadians(index * DEGREES_PER_RAY)
                val direction = Offset(cos(angle).toFloat(), sin(angle).toFloat())
                drawLine(color, center + direction * (s * 0.32f), center + direction * (s * 0.46f), strokeWidth, StrokeCap.Round)
            }
        }
        CardGlyph.EYE -> {
            val eye = Path().apply {
                moveTo(s * 0.04f, s * 0.5f)
                quadraticTo(s * 0.5f, s * 0.04f, s * 0.96f, s * 0.5f)
                quadraticTo(s * 0.5f, s * 0.96f, s * 0.04f, s * 0.5f)
                close()
            }
            drawPath(eye, color, style = stroke)
            drawCircle(color, radius = s * 0.13f, center = center)
        }
        CardGlyph.GAUGE -> {
            drawArc(
                color = color,
                startAngle = 150f,
                sweepAngle = 240f,
                useCenter = false,
                topLeft = Offset(s * 0.06f, s * 0.06f),
                size = Size(s * 0.88f, s * 0.88f),
                style = stroke,
            )
            drawLine(color, center, Offset(s * 0.72f, s * 0.3f), strokeWidth, StrokeCap.Round)
        }
        CardGlyph.SUNRISE -> {
            drawArc(
                color = color,
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = true,
                topLeft = Offset(s * 0.26f, s * 0.36f),
                size = Size(s * 0.48f, s * 0.48f),
            )
            drawLine(color, Offset(s * 0.04f, s * 0.64f), Offset(s * 0.96f, s * 0.64f), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(s * 0.5f, s * 0.04f), Offset(s * 0.5f, s * 0.2f), strokeWidth, StrokeCap.Round)
        }
    }
}
