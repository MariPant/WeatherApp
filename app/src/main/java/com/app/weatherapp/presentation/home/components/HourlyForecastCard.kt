package com.app.weatherapp.presentation.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import com.app.weatherapp.R
import com.app.weatherapp.domain.model.HourlyTimelineItem
import com.app.weatherapp.presentation.common.TimeFormats
import com.app.weatherapp.presentation.common.WeatherIconType
import com.app.weatherapp.presentation.common.WeatherVisualMapper
import com.app.weatherapp.presentation.common.formatTemperature
import com.app.weatherapp.ui.components.CardDivider
import com.app.weatherapp.ui.components.CardGlyph
import com.app.weatherapp.ui.components.CardHeader
import com.app.weatherapp.ui.components.GlassCard
import com.app.weatherapp.ui.components.WeatherIcon
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather

/** Precipitation chance shown under an icon only when it is worth mentioning. */
internal const val SHOW_PRECIPITATION_FROM_PERCENT = 20
internal val PrecipitationText = Color(0xFF9FDBFF)
private const val NOW_HIGHLIGHT_ALPHA = 0.14f

@Composable
fun HourlyForecastCard(timeline: List<HourlyTimelineItem>, formats: TimeFormats, modifier: Modifier = Modifier) {
    GlassCard(modifier = modifier, contentPadding = PaddingValues(vertical = Dimens.CardPadding)) {
        CardHeader(
            title = stringResource(R.string.hourly_forecast),
            glyph = CardGlyph.CLOCK,
            modifier = Modifier.padding(horizontal = Dimens.CardPadding),
        )
        CardDivider(Modifier.padding(horizontal = Dimens.CardPadding, vertical = Dimens.SpaceM))
        LazyRow(
            contentPadding = PaddingValues(horizontal = Dimens.SpaceS),
            horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXxs),
        ) {
            items(timeline, key = { "${it::class.java.simpleName}_${it.time}" }) { item ->
                when (item) {
                    is HourlyTimelineItem.Hour -> HourItem(item, formats)
                    is HourlyTimelineItem.Sunrise -> SunEventItem(
                        time = item.time.format(formats.time),
                        label = stringResource(R.string.sunrise),
                        icon = WeatherIconType.SUNRISE,
                    )
                    is HourlyTimelineItem.Sunset -> SunEventItem(
                        time = item.time.format(formats.time),
                        label = stringResource(R.string.sunset),
                        icon = WeatherIconType.SUNSET,
                    )
                }
            }
        }
    }
}

@Composable
private fun HourItem(item: HourlyTimelineItem.Hour, formats: TimeFormats) {
    val forecast = item.forecast
    val label = if (item.isNow) stringResource(R.string.now) else forecast.time.format(formats.hour)
    val condition = stringResource(WeatherVisualMapper.description(forecast.condition, forecast.isDay))
    val temperature = formatTemperature(forecast.temperature)
    val precipitation = forecast.precipitationProbabilityPercent
        ?.takeIf { it >= SHOW_PRECIPITATION_FROM_PERCENT }
    val precipitationDescription = precipitation?.let {
        stringResource(R.string.precipitation_chance_description, it)
    }
    val description = listOfNotNull(
        stringResource(R.string.hour_description, label, condition, temperature),
        precipitationDescription,
    ).joinToString(", ")

    HourlyColumn(
        highlighted = item.isNow,
        description = description,
        top = label,
        icon = WeatherVisualMapper.icon(forecast.condition, forecast.isDay),
        middle = precipitation?.let { stringResource(R.string.precipitation_chance, it) },
        bottom = temperature,
    )
}

@Composable
private fun SunEventItem(time: String, label: String, icon: WeatherIconType) {
    HourlyColumn(
        highlighted = false,
        description = "$label $time",
        top = time,
        icon = icon,
        middle = null,
        bottom = label,
        bottomStyleSmall = true,
    )
}

@Composable
private fun HourlyColumn(
    highlighted: Boolean,
    description: String,
    top: String,
    icon: WeatherIconType,
    middle: String?,
    bottom: String,
    bottomStyleSmall: Boolean = false,
) {
    val shape = RoundedCornerShape(Dimens.CardCorner)
    Column(
        modifier = Modifier
            .widthIn(min = Dimens.HourlyItemMinWidth)
            .clip(shape)
            .background(if (highlighted) OnWeather.copy(alpha = NOW_HIGHLIGHT_ALPHA) else Color.Transparent)
            .padding(horizontal = Dimens.SpaceS, vertical = Dimens.SpaceS)
            .clearAndSetSemantics { contentDescription = description },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXs),
    ) {
        Text(
            text = top,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (highlighted) FontWeight.Bold else FontWeight.Medium,
            color = OnWeather,
            maxLines = 1,
        )
        WeatherIcon(type = icon, contentDescription = null, size = Dimens.IconMedium)
        // Always reserve the line so temperatures stay aligned across the row.
        Text(
            text = middle.orEmpty(),
            style = MaterialTheme.typography.labelMedium,
            color = PrecipitationText,
            maxLines = 1,
        )
        Text(
            text = bottom,
            style = if (bottomStyleSmall) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            color = if (bottomStyleSmall) OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA) else OnWeather,
            maxLines = 1,
        )
    }
}
