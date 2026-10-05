package com.app.weatherapp.presentation.home.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.app.weatherapp.R
import com.app.weatherapp.presentation.common.displayName
import com.app.weatherapp.presentation.common.formatTemperature
import com.app.weatherapp.presentation.home.HomeWeatherUi
import com.app.weatherapp.ui.components.WeatherIcon
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather
import kotlin.math.roundToInt

private const val TEMPERATURE_MAX_LINES = 1

/** The big centred block: place, icon, temperature, condition, feels-like and today's range. */
@Composable
fun CurrentWeatherHeader(data: HomeWeatherUi, modifier: Modifier = Modifier) {
    val current = data.weather.current
    val conditionText = stringResource(data.description)
    Column(
        modifier = modifier.padding(vertical = Dimens.SpaceL),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (data.location.isCurrentLocation) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
                    modifier = Modifier.size(Dimens.IconTiny),
                )
                Text(
                    text = stringResource(R.string.my_location_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
                )
            }
        }
        Text(
            text = data.location.displayName(),
            style = MaterialTheme.typography.headlineMedium,
            color = OnWeather,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(Dimens.SpaceS))
        WeatherIcon(type = data.icon, contentDescription = conditionText, size = Dimens.IconHero)
        AnimatedTemperature(current.temperature)
        Text(
            text = conditionText,
            style = MaterialTheme.typography.titleLarge,
            color = OnWeather,
        )
        Column(
            modifier = Modifier.padding(top = Dimens.SpaceXs),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceXxs),
        ) {
            Text(
                text = stringResource(R.string.feels_like_short, formatTemperature(current.apparentTemperature)),
                style = MaterialTheme.typography.bodyLarge,
                color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
            )
            data.weather.today?.let { today ->
                Text(
                    text = stringResource(
                        R.string.high_low,
                        formatTemperature(today.maxTemperature),
                        formatTemperature(today.minTemperature),
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = OnWeather,
                )
            }
        }
    }
}

/** Rolls the number up or down when the temperature changes (refresh, city switch). */
@Composable
private fun AnimatedTemperature(temperature: Double) {
    AnimatedContent(
        targetState = temperature.roundToInt(),
        transitionSpec = {
            val direction = if (targetState > initialState) 1 else -1
            (slideInVertically { height -> height * direction } + fadeIn()) togetherWith
                (slideOutVertically { height -> -height * direction } + fadeOut()) using
                SizeTransform(clip = false)
        },
        label = "temperature",
    ) { value ->
        Text(
            text = formatTemperature(value.toDouble()),
            style = MaterialTheme.typography.displayLarge,
            color = OnWeather,
            maxLines = TEMPERATURE_MAX_LINES,
        )
    }
}
