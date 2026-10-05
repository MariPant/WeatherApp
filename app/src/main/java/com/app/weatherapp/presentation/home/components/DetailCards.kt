package com.app.weatherapp.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.app.weatherapp.R
import com.app.weatherapp.domain.model.FeelsLike
import com.app.weatherapp.domain.model.PressureLevel
import com.app.weatherapp.domain.model.UvLevel
import com.app.weatherapp.domain.model.VisibilityLevel
import com.app.weatherapp.presentation.common.formatTemperature
import com.app.weatherapp.presentation.home.DetailsUi
import com.app.weatherapp.ui.components.CardGlyph
import com.app.weatherapp.ui.components.CardHeader
import com.app.weatherapp.ui.components.GlassCard
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather
import java.util.Locale
import kotlin.math.roundToInt

private val DetailCardMinHeight = 148.dp
private val CompassSize = 52.dp
private const val UV_SCALE_MAX = 11.0
private const val METERS_IN_KM = 1_000.0
private const val WHOLE_KM_FROM = 10.0
private const val COMPASS_RING_ALPHA = 0.3f
private const val HALF_TURN_DEGREES = 180f
private val UvScale = listOf(
    Color(0xFF4CD964), Color(0xFFFFD60A), Color(0xFFFF9F0A), Color(0xFFFF453A), Color(0xFFBF5AF2),
)

/** Grid of square-ish tiles; the column count adapts to the available width. */
@Composable
fun DetailsGrid(details: DetailsUi, columns: Int, modifier: Modifier = Modifier) {
    val tiles = listOf<@Composable (Modifier) -> Unit>(
        { FeelsLikeTile(details, it) },
        { HumidityTile(details, it) },
        { WindTile(details, it) },
        { UvTile(details, it) },
        { VisibilityTile(details, it) },
        { PressureTile(details, it) },
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM)) {
        tiles.chunked(columns).forEach { rowTiles ->
            Row(
                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
            ) {
                rowTiles.forEach { tile -> tile(Modifier.weight(1f).fillMaxHeight()) }
                repeat(columns - rowTiles.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun DetailTile(
    title: String,
    glyph: CardGlyph,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    GlassCard(modifier = modifier.heightIn(min = DetailCardMinHeight)) {
        CardHeader(title = title, glyph = glyph)
        Spacer(Modifier.height(Dimens.SpaceS))
        content()
    }
}

/** Big number with an optional smaller unit ("1015 hPa"), so values fit narrow tiles. */
@Composable
private fun TileValue(value: String, unit: String? = null) {
    val unitSize = MaterialTheme.typography.bodyLarge.fontSize
    Text(
        text = buildAnnotatedString {
            append(value)
            if (unit != null) {
                withStyle(SpanStyle(fontSize = unitSize)) { append(" $unit") }
            }
        },
        style = MaterialTheme.typography.displaySmall,
        color = OnWeather,
    )
}

@Composable
private fun ColumnScope.TileCaption(text: String) {
    Spacer(Modifier.weight(1f).heightIn(min = Dimens.SpaceS))
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
    )
}

@Composable
private fun FeelsLikeTile(details: DetailsUi, modifier: Modifier) {
    DetailTile(stringResource(R.string.detail_feels_like), CardGlyph.THERMOMETER, modifier) {
        TileValue(formatTemperature(details.apparentTemperature))
        TileCaption(
            stringResource(
                when (details.feelsLike) {
                    FeelsLike.SIMILAR -> R.string.feels_like_similar
                    FeelsLike.COLDER -> R.string.feels_like_colder
                    FeelsLike.WARMER -> R.string.feels_like_warmer
                },
            ),
        )
    }
}

@Composable
private fun HumidityTile(details: DetailsUi, modifier: Modifier) {
    DetailTile(stringResource(R.string.detail_humidity), CardGlyph.DROP, modifier) {
        TileValue("${details.humidityPercent}%")
        details.dewPoint?.let { TileCaption(stringResource(R.string.dew_point, formatTemperature(it))) }
    }
}

@Composable
private fun WindTile(details: DetailsUi, modifier: Modifier) {
    DetailTile(stringResource(R.string.detail_wind), CardGlyph.WIND, modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                TileValue(details.windSpeedKmh.roundToInt().toString(), stringResource(R.string.unit_kmh))
                Text(
                    text = stringResource(R.string.wind_from, details.windCompass),
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
                )
            }
            WindCompass(
                directionDegrees = details.windDirectionDegrees,
                description = stringResource(R.string.wind_direction_description, details.windCompass),
            )
        }
        details.windGustsKmh?.let { TileCaption(stringResource(R.string.wind_gusts, it.roundToInt())) }
    }
}

/** Ring with an arrow pointing where the wind blows to (meteorological direction + 180°). */
@Composable
private fun WindCompass(directionDegrees: Int, description: String) {
    Canvas(
        Modifier
            .size(CompassSize)
            .semantics { contentDescription = description },
    ) {
        val radius = size.minDimension / 2f
        val strokeWidth = 2.dp.toPx()
        drawCircle(OnWeather.copy(alpha = COMPASS_RING_ALPHA), radius = radius - strokeWidth, style = Stroke(strokeWidth))
        rotate(directionDegrees + HALF_TURN_DEGREES) {
            val top = Offset(center.x, center.y - radius * 0.7f)
            val bottom = Offset(center.x, center.y + radius * 0.7f)
            drawLine(OnWeather, bottom, top, strokeWidth, StrokeCap.Round)
            val head = Path().apply {
                moveTo(top.x, top.y - strokeWidth)
                lineTo(top.x - radius * 0.22f, top.y + radius * 0.28f)
                lineTo(top.x + radius * 0.22f, top.y + radius * 0.28f)
                close()
            }
            drawPath(head, OnWeather)
            drawCircle(OnWeather, radius = strokeWidth * 1.6f, center = bottom)
        }
    }
}

@Composable
private fun UvTile(details: DetailsUi, modifier: Modifier) {
    DetailTile(stringResource(R.string.detail_uv_index), CardGlyph.SUN, modifier) {
        val uv = details.uvIndex
        val level = details.uvLevel
        if (uv == null || level == null) {
            TileValue(stringResource(R.string.not_available))
            return@DetailTile
        }
        TileValue(uv.roundToInt().toString())
        Text(
            text = stringResource(
                when (level) {
                    UvLevel.LOW -> R.string.uv_low
                    UvLevel.MODERATE -> R.string.uv_moderate
                    UvLevel.HIGH -> R.string.uv_high
                    UvLevel.VERY_HIGH -> R.string.uv_very_high
                    UvLevel.EXTREME -> R.string.uv_extreme
                },
            ),
            style = MaterialTheme.typography.titleMedium,
            color = OnWeather,
        )
        Spacer(Modifier.weight(1f).heightIn(min = Dimens.SpaceS))
        UvBar(fraction = (uv / UV_SCALE_MAX).toFloat().coerceIn(0f, 1f))
    }
}

@Composable
private fun UvBar(fraction: Float) {
    Canvas(
        Modifier
            .fillMaxWidth()
            .height(Dimens.MarkerSize),
    ) {
        val barHeight = Dimens.UvBarHeight.toPx()
        val top = (size.height - barHeight) / 2f
        drawRoundRect(
            brush = Brush.horizontalGradient(UvScale),
            topLeft = Offset(0f, top),
            size = Size(size.width, barHeight),
            cornerRadius = CornerRadius(barHeight / 2f),
        )
        val markerRadius = size.height / 2f
        val x = (fraction * size.width).coerceIn(markerRadius, size.width - markerRadius)
        drawCircle(Color.Black.copy(alpha = COMPASS_RING_ALPHA), radius = markerRadius, center = Offset(x, center.y))
        drawCircle(OnWeather, radius = markerRadius * 0.7f, center = Offset(x, center.y))
    }
}

@Composable
private fun VisibilityTile(details: DetailsUi, modifier: Modifier) {
    DetailTile(stringResource(R.string.detail_visibility), CardGlyph.EYE, modifier) {
        val meters = details.visibilityMeters
        val level = details.visibilityLevel
        if (meters == null || level == null) {
            TileValue(stringResource(R.string.not_available))
            return@DetailTile
        }
        val km = meters / METERS_IN_KM
        val kmText = if (km >= WHOLE_KM_FROM) km.roundToInt().toString() else String.format(Locale.getDefault(), "%.1f", km)
        TileValue(kmText, stringResource(R.string.unit_km))
        TileCaption(
            stringResource(
                when (level) {
                    VisibilityLevel.VERY_POOR -> R.string.visibility_very_poor
                    VisibilityLevel.POOR -> R.string.visibility_poor
                    VisibilityLevel.MODERATE -> R.string.visibility_moderate
                    VisibilityLevel.GOOD -> R.string.visibility_good
                    VisibilityLevel.EXCELLENT -> R.string.visibility_excellent
                },
            ),
        )
    }
}

@Composable
private fun PressureTile(details: DetailsUi, modifier: Modifier) {
    DetailTile(stringResource(R.string.detail_pressure), CardGlyph.GAUGE, modifier) {
        TileValue(details.pressureHpa.roundToInt().toString(), stringResource(R.string.unit_hpa))
        TileCaption(
            stringResource(
                when (details.pressureLevel) {
                    PressureLevel.LOW -> R.string.pressure_low
                    PressureLevel.NORMAL -> R.string.pressure_normal
                    PressureLevel.HIGH -> R.string.pressure_high
                },
            ),
        )
    }
}
