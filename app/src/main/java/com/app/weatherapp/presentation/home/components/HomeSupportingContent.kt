package com.app.weatherapp.presentation.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.List
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import com.app.weatherapp.R
import com.app.weatherapp.domain.model.DataError
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.presentation.common.TimeFormats
import com.app.weatherapp.presentation.common.WeatherIconType
import com.app.weatherapp.presentation.common.displayName
import com.app.weatherapp.presentation.common.shortMessage
import com.app.weatherapp.presentation.common.toErrorText
import com.app.weatherapp.ui.components.GlassCard
import com.app.weatherapp.ui.components.MessageAction
import com.app.weatherapp.ui.components.MessageColors
import com.app.weatherapp.ui.components.MessageState
import com.app.weatherapp.ui.components.ShimmerContainer
import com.app.weatherapp.ui.components.SkeletonBlock
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather
import java.time.Instant
import java.time.ZoneId

private const val SKELETON_BASE_ALPHA = 0.14f
private const val SKELETON_HIGHLIGHT_ALPHA = 0.22f
private const val SKELETON_DAILY_ROWS = 5
private const val SKELETON_HOURS = 6
private const val SKELETON_TITLE_WIDTH_FRACTION = 0.45f

/** Colours for message states drawn on a weather gradient. */
val OnWeatherMessageColors = MessageColors(
    title = OnWeather,
    body = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
    primaryContainer = OnWeather,
    primaryContent = Color(0xFF14325C),
    secondaryContent = OnWeather,
)

@Composable
fun HomeTopBar(
    compactTitle: String?,
    compactTitleAlpha: () -> Float,
    onOpenLocations: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onOpenSearch: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = Dimens.SpaceXs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onOpenLocations) {
            Icon(Icons.AutoMirrored.Rounded.List, stringResource(R.string.action_open_locations), tint = OnWeather)
        }
        Text(
            text = compactTitle.orEmpty(),
            style = MaterialTheme.typography.titleMedium,
            color = OnWeather,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .weight(1f)
                .graphicsLayer { alpha = compactTitleAlpha() }
                // Duplicates the large header, so screen readers skip it.
                .clearAndSetSemantics { },
        )
        IconButton(onClick = onUseCurrentLocation) {
            Icon(Icons.Rounded.LocationOn, stringResource(R.string.action_use_current_location), tint = OnWeather)
        }
        IconButton(onClick = onOpenSearch) {
            Icon(Icons.Rounded.Search, stringResource(R.string.action_search), tint = OnWeather)
        }
    }
}

/** "Updated 08:42 · No internet connection" shown above cached data. */
@Composable
fun OfflineBanner(fetchedAt: Instant, reason: DataError, formats: TimeFormats, modifier: Modifier = Modifier) {
    val time = fetchedAt.atZone(ZoneId.systemDefault()).format(formats.time)
    GlassCard(modifier = modifier) {
        Text(
            text = stringResource(R.string.offline_banner, time, stringResource(reason.shortMessage())),
            style = MaterialTheme.typography.bodyMedium,
            color = OnWeather,
        )
    }
}

/** Open-Meteo data is CC BY 4.0, so attribution is required. Below it: who made the app. */
@Composable
fun DataSourceFooter(modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        FooterLink(
            text = stringResource(R.string.data_source),
            url = stringResource(R.string.data_source_url),
        )
        Text(
            text = stringResource(R.string.made_by),
            style = MaterialTheme.typography.bodyMedium,
            color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
            modifier = Modifier.padding(top = Dimens.SpaceS),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS)) {
            val linkedIn = stringResource(R.string.author_linkedin)
            val gitHub = stringResource(R.string.author_github)
            FooterLink(
                text = linkedIn,
                url = stringResource(R.string.author_linkedin_url),
                description = stringResource(R.string.author_link_description, linkedIn),
                emphasized = true,
            )
            FooterLink(
                text = gitHub,
                url = stringResource(R.string.author_github_url),
                description = stringResource(R.string.author_link_description, gitHub),
                emphasized = true,
            )
        }
    }
}

@Composable
private fun FooterLink(
    text: String,
    url: String,
    description: String? = null,
    emphasized: Boolean = false,
) {
    val uriHandler = LocalUriHandler.current
    Box(
        modifier = Modifier
            .heightIn(min = Dimens.MinTouchTarget)
            .clickable(role = Role.Button, onClickLabel = description) { uriHandler.openUri(url) }
            .padding(horizontal = Dimens.SpaceM),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = if (emphasized) MaterialTheme.typography.labelLarge else MaterialTheme.typography.bodyMedium,
            color = if (emphasized) OnWeather else OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
            textDecoration = if (emphasized) TextDecoration.Underline else null,
        )
    }
}

/** Skeleton that mirrors the real layout, so content does not jump when it arrives. */
@Composable
fun HomeSkeleton(modifier: Modifier = Modifier) {
    val base = OnWeather.copy(alpha = SKELETON_BASE_ALPHA)
    val highlight = OnWeather.copy(alpha = SKELETON_HIGHLIGHT_ALPHA)
    ShimmerContainer {
        Column(
            modifier = modifier
                .widthIn(max = Dimens.MaxContentWidth)
                .padding(horizontal = Dimens.ScreenPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        ) {
            Spacer(Modifier.height(Dimens.SpaceL))
            SkeletonBlock(Modifier.fillMaxWidth(SKELETON_TITLE_WIDTH_FRACTION).height(Dimens.SpaceXxl), baseColor = base, highlightColor = highlight)
            SkeletonBlock(Modifier.size(Dimens.IconHero), CircleShape, base, highlight)
            SkeletonBlock(Modifier.width(Dimens.IconHero * 2).height(Dimens.IconHero), baseColor = base, highlightColor = highlight)
            SkeletonBlock(Modifier.width(Dimens.IconHero * 2).height(Dimens.SpaceL), baseColor = base, highlightColor = highlight)
            Spacer(Modifier.height(Dimens.SpaceL))
            SkeletonCard(base, highlight) {
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceL)) {
                    repeat(SKELETON_HOURS) {
                        SkeletonBlock(Modifier.weight(1f).height(Dimens.IconHero + Dimens.SpaceXl), baseColor = base, highlightColor = highlight)
                    }
                }
            }
            SkeletonCard(base, highlight) {
                repeat(SKELETON_DAILY_ROWS) {
                    SkeletonBlock(
                        Modifier.fillMaxWidth().height(Dimens.SpaceXl).padding(vertical = Dimens.SpaceXxs),
                        baseColor = base,
                        highlightColor = highlight,
                    )
                    Spacer(Modifier.height(Dimens.SpaceM))
                }
            }
        }
    }
}

@Composable
private fun SkeletonCard(base: Color, highlight: Color, content: @Composable () -> Unit) {
    GlassCard(Modifier.fillMaxWidth()) {
        SkeletonBlock(Modifier.fillMaxWidth(SKELETON_TITLE_WIDTH_FRACTION).height(Dimens.SpaceM), baseColor = base, highlightColor = highlight)
        Spacer(Modifier.height(Dimens.SpaceL))
        content()
    }
}

/** First launch: explain the choice instead of asking for a permission out of the blue. */
@Composable
fun WelcomeContent(onUseCurrentLocation: () -> Unit, onOpenSearch: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            MessageState(
                icon = WeatherIconType.PARTLY_CLOUDY_DAY,
                title = stringResource(R.string.welcome_title),
                message = stringResource(R.string.welcome_message),
                colors = OnWeatherMessageColors,
                primaryAction = MessageAction(stringResource(R.string.action_use_current_location), onUseCurrentLocation),
                secondaryAction = MessageAction(stringResource(R.string.action_search), onOpenSearch),
            )
            Text(
                text = stringResource(R.string.welcome_location_note),
                style = MaterialTheme.typography.bodyMedium,
                color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = Dimens.SpaceXxl),
            )
        }
    }
}

@Composable
fun HomeErrorContent(
    location: WeatherLocation?,
    error: DataError,
    onRetry: () -> Unit,
    onUseCurrentLocation: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val text = error.toErrorText()
    val retry = MessageAction(stringResource(R.string.action_retry), onRetry)
    val search = MessageAction(stringResource(R.string.action_search), onOpenSearch)
    val (primary, secondary, tertiary) = when (error) {
        DataError.LocationPermissionDenied -> Triple(
            MessageAction(stringResource(R.string.action_allow_location), onUseCurrentLocation),
            search,
            MessageAction(stringResource(R.string.action_open_settings), onOpenSettings),
        )
        DataError.LocationUnavailable, DataError.CityNotFound -> Triple(retry, search, null)
        else -> Triple(retry, null, null)
    }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        location?.let {
            Text(
                text = it.displayName(),
                style = MaterialTheme.typography.headlineMedium,
                color = OnWeather,
                modifier = Modifier.padding(top = Dimens.SpaceXl),
            )
        }
        MessageState(
            icon = errorIcon(error),
            title = stringResource(text.title),
            message = stringResource(text.message),
            colors = OnWeatherMessageColors,
            primaryAction = primary,
            secondaryAction = secondary,
            tertiaryAction = tertiary,
        )
    }
}

private fun errorIcon(error: DataError): WeatherIconType = when (error) {
    DataError.LocationPermissionDenied, DataError.LocationUnavailable -> WeatherIconType.FOG
    DataError.NoInternet, DataError.Timeout -> WeatherIconType.CLOUD
    else -> WeatherIconType.THUNDERSTORM
}
