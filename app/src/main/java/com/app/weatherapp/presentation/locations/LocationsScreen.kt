package com.app.weatherapp.presentation.locations

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.weatherapp.R
import com.app.weatherapp.presentation.common.ObserveAsEvents
import com.app.weatherapp.presentation.common.TimeFormats
import com.app.weatherapp.presentation.common.WeatherBackgroundStyle
import com.app.weatherapp.presentation.common.WeatherIconType
import com.app.weatherapp.presentation.common.displayName
import com.app.weatherapp.presentation.common.formatTemperature
import com.app.weatherapp.presentation.common.rememberTimeFormats
import com.app.weatherapp.ui.components.MessageAction
import com.app.weatherapp.ui.components.MessageState
import com.app.weatherapp.ui.components.ShimmerContainer
import com.app.weatherapp.ui.components.SkeletonBlock
import com.app.weatherapp.ui.components.SystemBarIcons
import com.app.weatherapp.ui.components.WeatherIcon
import com.app.weatherapp.ui.components.themedMessageColors
import com.app.weatherapp.ui.components.weatherGradientStops
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather
import kotlinx.coroutines.launch
import java.time.LocalTime

private const val SELECTED_BORDER_ALPHA = 0.85f
private const val SKELETON_BASE_ALPHA = 0.2f
private const val SKELETON_HIGHLIGHT_ALPHA = 0.32f
private val SelectedBorderWidth = 2.dp
private val TemperatureSkeletonWidth = 56.dp

@Composable
fun LocationsRoute(
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onNavigateHome: () -> Unit,
    viewModel: LocationsViewModel = viewModel(factory = LocationsViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val permissionDeniedMessage = stringResource(R.string.error_location_permission_short)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LocationsEvent.NavigateHome -> onNavigateHome()
            LocationsEvent.LocationPermissionDenied -> scope.launch {
                snackbarHostState.showSnackbar(permissionDeniedMessage)
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onLocationPermissionResult,
    )

    LocationsScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onBack = onBack,
        onOpenSearch = onOpenSearch,
        onRefresh = viewModel::refresh,
        onSelect = viewModel::onLocationSelected,
        onRemove = viewModel::onLocationRemoved,
        onUseCurrentLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationsScreen(
    state: LocationsUiState,
    snackbarHostState: SnackbarHostState,
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onRefresh: () -> Unit,
    onSelect: (String) -> Unit,
    onRemove: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
) {
    SystemBarIcons(useDarkIcons = !isSystemInDarkTheme())
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val formats = rememberTimeFormats()
    val hasCurrentLocation = state.items.any { it.isCurrentLocation }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.locations_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back))
                    }
                },
                actions = {
                    IconButton(onClick = onOpenSearch) {
                        Icon(Icons.Rounded.Add, stringResource(R.string.action_add_city))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = onRefresh,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding, vertical = Dimens.SpaceS),
                verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                item(key = "search") {
                    SearchShortcut(onOpenSearch, Modifier.widthIn(max = Dimens.MaxContentWidth))
                }
                if (!state.isLoading && !hasCurrentLocation) {
                    item(key = "use_current") {
                        UseCurrentLocationRow(onUseCurrentLocation, Modifier.widthIn(max = Dimens.MaxContentWidth).animateItem())
                    }
                }
                if (!state.isLoading && state.items.isEmpty()) {
                    item(key = "empty") {
                        MessageState(
                            icon = WeatherIconType.PARTLY_CLOUDY_DAY,
                            title = stringResource(R.string.locations_empty_title),
                            message = stringResource(R.string.locations_empty_message),
                            colors = themedMessageColors(),
                            primaryAction = MessageAction(stringResource(R.string.action_add_city), onOpenSearch),
                        )
                    }
                }
                items(state.items, key = { it.id }) { item ->
                    SwipeToRemove(
                        onRemove = { onRemove(item.id) },
                        modifier = Modifier
                            .widthIn(max = Dimens.MaxContentWidth)
                            .animateItem(),
                    ) {
                        LocationCard(
                            item = item,
                            formats = formats,
                            onClick = { onSelect(item.id) },
                            onRemove = { onRemove(item.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchShortcut(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouchTarget)
            .clip(RoundedCornerShape(Dimens.CardCorner))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(Dimens.SpaceM))
        Text(
            text = stringResource(R.string.search_placeholder),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun UseCurrentLocationRow(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.MinTouchTarget)
            .clip(RoundedCornerShape(Dimens.CardCorner))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceM),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(Dimens.SpaceM))
        Text(
            text = stringResource(R.string.action_use_current_location),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeToRemove(
    onRemove: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val currentOnRemove by rememberUpdatedState(onRemove)
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                currentOnRemove()
                true
            } else {
                false
            }
        },
    )
    val shape = RoundedCornerShape(Dimens.LocationCardCorner)
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shape)
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = Dimens.SpaceXl),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(Icons.Rounded.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
            }
        },
    ) {
        content()
    }
}

/** Card with the city's own weather gradient, like a miniature of its home screen. */
@Composable
private fun LocationCard(
    item: LocationItemUi,
    formats: TimeFormats,
    onClick: () -> Unit,
    onRemove: () -> Unit,
) {
    val weather = item.weather as? LocationWeatherUi.Loaded
    val style = weather?.background ?: WeatherBackgroundStyle.CLOUDY_DAY
    val stops = weatherGradientStops(style, isNight = weather?.isDay == false, darkTheme = isSystemInDarkTheme())
    val shape = RoundedCornerShape(Dimens.LocationCardCorner)
    val removeLabel = stringResource(R.string.action_remove)
    val selectedLabel = stringResource(R.string.location_selected)
    val title = item.location?.displayName() ?: stringResource(R.string.my_location)
    val secondLine = when {
        item.isCurrentLocation -> stringResource(R.string.my_location)
        weather != null -> LocalTime.now(weather.zoneId).format(formats.time)
        else -> item.location?.subtitle.orEmpty()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Dimens.LocationCardMinHeight)
            .clip(shape)
            .background(Brush.verticalGradient(stops))
            .then(
                if (item.isSelected) {
                    Modifier.border(SelectedBorderWidth, OnWeather.copy(alpha = SELECTED_BORDER_ALPHA), shape)
                } else {
                    Modifier
                },
            )
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                if (item.isSelected) stateDescription = selectedLabel
                customActions = listOf(CustomAccessibilityAction(removeLabel) { onRemove(); true })
            }
            .padding(Dimens.CardPadding),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (item.isCurrentLocation) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            tint = OnWeather,
                            modifier = Modifier.size(Dimens.IconSmall),
                        )
                        Spacer(Modifier.width(Dimens.SpaceXs))
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        color = OnWeather,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = secondLine,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            CardTemperature(item.weather)
        }
        Spacer(Modifier.heightIn(min = Dimens.SpaceM))
        CardFooter(item.weather)
    }
}

@Composable
private fun CardTemperature(weather: LocationWeatherUi) {
    when (weather) {
        is LocationWeatherUi.Loaded -> Text(
            text = formatTemperature(weather.temperature),
            style = MaterialTheme.typography.displaySmall,
            color = OnWeather,
        )
        LocationWeatherUi.Loading -> ShimmerContainer {
            SkeletonBlock(
                modifier = Modifier.size(TemperatureSkeletonWidth, Dimens.SpaceXxl + Dimens.SpaceS),
                baseColor = OnWeather.copy(alpha = SKELETON_BASE_ALPHA),
                highlightColor = OnWeather.copy(alpha = SKELETON_HIGHLIGHT_ALPHA),
            )
        }
        LocationWeatherUi.Unavailable -> Text(
            text = stringResource(R.string.not_available),
            style = MaterialTheme.typography.displaySmall,
            color = OnWeather,
        )
    }
}

@Composable
private fun CardFooter(weather: LocationWeatherUi) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        when (weather) {
            is LocationWeatherUi.Loaded -> {
                val description = stringResource(weather.description)
                WeatherIcon(weather.icon, contentDescription = null, size = Dimens.IconSmall + Dimens.SpaceXs)
                Spacer(Modifier.width(Dimens.SpaceXs))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnWeather,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                if (weather.maxTemperature != null && weather.minTemperature != null) {
                    Text(
                        text = stringResource(
                            R.string.high_low,
                            formatTemperature(weather.maxTemperature),
                            formatTemperature(weather.minTemperature),
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnWeather,
                    )
                }
            }
            LocationWeatherUi.Unavailable -> Text(
                text = stringResource(R.string.location_unavailable),
                style = MaterialTheme.typography.bodyMedium,
                color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA),
            )
            LocationWeatherUi.Loading -> Spacer(Modifier.heightIn(min = Dimens.SpaceL))
        }
    }
}

