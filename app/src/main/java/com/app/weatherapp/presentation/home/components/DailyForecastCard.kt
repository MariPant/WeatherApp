package com.app.weatherapp.presentation.home.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.app.weatherapp.R
import com.app.weatherapp.presentation.common.TimeFormats
import com.app.weatherapp.presentation.common.formatTemperature
import com.app.weatherapp.presentation.home.DailyRowUi
import com.app.weatherapp.ui.components.CardDivider
import com.app.weatherapp.ui.components.CardGlyph
import com.app.weatherapp.ui.components.CardHeader
import com.app.weatherapp.ui.components.GlassCard
import com.app.weatherapp.ui.components.TemperatureColors
import com.app.weatherapp.ui.components.WeatherIcon
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.ON_WEATHER_TERTIARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather

private const val TRACK_ALPHA = 0.18f
private const val DAY_COLUMN_WEIGHT = 1.15f
private const val BAR_COLUMN_WEIGHT = 1f
private const val MARKER_OUTLINE_ALPHA = 0.35f
private const val MARKER_INNER_FRACTION = 0.7f
private val DailyRowMinHeight = Dimens.SpaceHuge + Dimens.SpaceS
private val DailyIconSize = Dimens.IconMedium - Dimens.SpaceXs

@Composable
fun DailyForecastCard(rows: List<DailyRowUi>, formats: TimeFormats, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, contentPadding = PaddingValues(Dimens.CardPadding)) {
        CardHeader(title = stringResource(R.string.daily_forecast, rows.size), glyph = CardGlyph.CALENDAR)
        CardDivider(Modifier.padding(top = Dimens.SpaceM))
        rows.forEachIndexed { index, row ->
            if (index > 0) CardDivider()
            DailyRow(row, formats)
        }
    }
}

@Composable
private fun DailyRow(row: DailyRowUi, formats: TimeFormats) {
    val day = if (row.isToday) stringResource(R.string.today) else row.forecast.date.format(formats.dayShort)
    val dayLong = if (row.isToday) day else row.forecast.date.format(formats.dayLong)
    val description = stringResource(row.description)
    val min = formatTemperature(row.forecast.minTemperature)
    val max = formatTemperature(row.forecast.maxTemperature)
    val precipitation = row.forecast.precipitationProbabilityPercent?.takeIf { it >= SHOW_PRECIPITATION_FROM_PERCENT }
    val rangeDescription = stringResource(R.string.temperature_range_description, min, max)
    val precipitationDescription = precipitation?.let { stringResource(R.string.precipitation_chance_description, it) }
    val spokenDescription = listOfNotNull(dayLong, description, rangeDescription, precipitationDescription)
        .joinToString(", ")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = DailyRowMinHeight)
            .padding(vertical = Dimens.SpaceS)
            .clearAndSetSemantics { contentDescription = spokenDescription },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
    ) {
        Column(Modifier.weight(DAY_COLUMN_WEIGHT)) {
            Text(text = day, style = MaterialTheme.typography.titleMedium, color = OnWeather, maxLines = 1)
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Column(
            modifier = Modifier.widthIn(min = Dimens.IconLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            WeatherIcon(type = row.icon, contentDescription = null, size = DailyIconSize)
            if (precipitation != null) {
                Text(
                    text = stringResource(R.string.precipitation_chance, precipitation),
                    style = MaterialTheme.typography.labelMedium,
                    color = PrecipitationText,
                )
            }
        }
        Text(
            text = min,
            style = MaterialTheme.typography.titleMedium,
            color = OnWeather.copy(alpha = ON_WEATHER_TERTIARY_ALPHA),
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = Dimens.TempLabelMinWidth),
            maxLines = 1,
        )
        TemperatureRangeBar(
            row = row,
            modifier = Modifier
                .weight(BAR_COLUMN_WEIGHT)
                .widthIn(min = Dimens.RangeBarMinWidth)
                .height(Dimens.MarkerSize),
        )
        Text(
            text = max,
            style = MaterialTheme.typography.titleMedium,
            color = OnWeather,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(min = Dimens.TempLabelMinWidth),
            maxLines = 1,
        )
    }
}

/**
 * Capsule showing where this day's range sits within the whole forecast period, coloured by
 * absolute temperature. Today's bar also shows a dot for the current temperature.
 */
@Composable
private fun TemperatureRangeBar(row: DailyRowUi, modifier: Modifier = Modifier) {
    val gradient = TemperatureColors.gradient(row.periodMin, row.periodMax)
    Canvas(modifier) {
        val barHeight = Dimens.RangeBarHeight.toPx()
        val top = (size.height - barHeight) / 2f
        val radius = CornerRadius(barHeight / 2f)
        drawRoundRect(
            color = OnWeather.copy(alpha = TRACK_ALPHA),
            topLeft = Offset(0f, top),
            size = Size(size.width, barHeight),
            cornerRadius = radius,
        )
        // Keep at least a dot visible when min == max, without drawing past the track.
        val end = maxOf(row.rangeEnd * size.width, row.rangeStart * size.width + barHeight).coerceAtMost(size.width)
        val start = minOf(row.rangeStart * size.width, end - barHeight).coerceAtLeast(0f)
        drawRoundRect(
            brush = Brush.horizontalGradient(gradient, startX = 0f, endX = size.width),
            topLeft = Offset(start, top),
            size = Size(end - start, barHeight),
            cornerRadius = radius,
        )
        row.currentMarker?.let { marker ->
            val center = Offset(marker * size.width, size.height / 2f)
            val markerRadius = size.height / 2f
            drawCircle(Color.Black.copy(alpha = MARKER_OUTLINE_ALPHA), radius = markerRadius, center = center)
            drawCircle(OnWeather, radius = markerRadius * MARKER_INNER_FRACTION, center = center)
        }
    }
}
