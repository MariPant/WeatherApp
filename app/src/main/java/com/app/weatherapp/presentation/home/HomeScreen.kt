package com.app.weatherapp.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.weatherapp.R
import com.app.weatherapp.presentation.common.TimeFormats
import com.app.weatherapp.presentation.common.WeatherBackgroundStyle
import com.app.weatherapp.presentation.common.displayName
import com.app.weatherapp.presentation.common.formatTemperature
import com.app.weatherapp.presentation.common.openAppSettings
import com.app.weatherapp.presentation.common.rememberTimeFormats
import com.app.weatherapp.presentation.common.shortMessage
import com.app.weatherapp.presentation.home.components.CurrentWeatherHeader
import com.app.weatherapp.presentation.home.components.DailyForecastCard
import com.app.weatherapp.presentation.home.components.DataSourceFooter
import com.app.weatherapp.presentation.home.components.DetailsGrid
import com.app.weatherapp.presentation.home.components.HomeErrorContent
import com.app.weatherapp.presentation.home.components.HomeSkeleton
import com.app.weatherapp.presentation.home.components.HomeTopBar
import com.app.weatherapp.presentation.home.components.HourlyForecastCard
import com.app.weatherapp.presentation.home.components.OfflineBanner
import com.app.weatherapp.presentation.home.components.SunCard
import com.app.weatherapp.presentation.home.components.WelcomeContent
import com.app.weatherapp.ui.components.LocalCardScrimAlpha
import com.app.weatherapp.ui.components.SystemBarIcons
import com.app.weatherapp.ui.components.WeatherBackground
import com.app.weatherapp.ui.components.appearAnimation
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.OnWeather
import com.app.weatherapp.ui.theme.WeatherGradients

private const val CONTENT_ENTER_MILLIS = 450
private const val CONTENT_EXIT_MILLIS = 250
private const val COMPACT_DETAIL_COLUMNS = 2
private const val MEDIUM_DETAIL_COLUMNS = 3
private val HeaderFadeDistance = 160.dp
private val RefreshIndicatorColor = Color(0xFF14325C)

@Composable
fun HomeRoute(
    onOpenSearch: () -> Unit,
    onOpenLocations: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = HomeViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    // Permission is requested only when the user explicitly asks for their location.
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onLocationPermissionResult,
    )
    HomeScreen(
        state = state,
        actions = HomeActions(
            onRefresh = viewModel::refresh,
            onRetry = viewModel::retry,
            onUseCurrentLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
            onOpenSearch = onOpenSearch,
            onOpenLocations = onOpenLocations,
            onOpenSettings = context::openAppSettings,
            onMessageShown = viewModel::onMessageShown,
        ),
    )
}

/** Groups the screen callbacks so composables below do not need long parameter lists. */
data class HomeActions(
    val onRefresh: () -> Unit,
    val onRetry: () -> Unit,
    val onUseCurrentLocation: () -> Unit,
    val onOpenSearch: () -> Unit,
    val onOpenLocations: () -> Unit,
    val onOpenSettings: () -> Unit,
    val onMessageShown: () -> Unit,
)

private data class BackgroundVisual(val style: WeatherBackgroundStyle, val isNight: Boolean)

@Composable
fun HomeScreen(state: HomeUiState, actions: HomeActions) {
    SystemBarIcons(useDarkIcons = false)
    val formats = rememberTimeFormats()
    val snackbarHostState = remember { SnackbarHostState() }

    // Keep the last weather background while loading the next city, so the screen never flashes.
    val success = state.content as? HomeContent.Success
    var lastVisual by remember { mutableStateOf(BackgroundVisual(WeatherBackgroundStyle.CLEAR_DAY, isNight = false)) }
    val visual = success?.let { BackgroundVisual(it.data.background, it.data.isNight) } ?: lastVisual
    SideEffect { lastVisual = visual }

    val messageText = state.message?.let { stringResource(it.shortMessage()) }
    LaunchedEffect(state.message) {
        if (messageText != null) {
            snackbarHostState.showSnackbar(messageText)
            actions.onMessageShown()
        }
    }

    Box(Modifier.fillMaxSize()) {
        WeatherBackground(style = visual.style, isNight = visual.isNight)
        CompositionLocalProvider(
            LocalCardScrimAlpha provides WeatherGradients.palette(visual.style).cardScrimAlpha,
            LocalContentColor provides OnWeather,
        ) {
            AnimatedContent(
                targetState = state.content,
                contentKey = { content -> content.animationKey() },
                transitionSpec = {
                    fadeIn(tween(CONTENT_ENTER_MILLIS)) togetherWith fadeOut(tween(CONTENT_EXIT_MILLIS))
                },
                label = "homeContent",
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
            ) { content ->
                when (content) {
                    HomeContent.Loading -> StaticPage(actions) {
                        HomeSkeleton(Modifier.align(Alignment.TopCenter))
                    }
                    HomeContent.NoLocation -> StaticPage(actions) {
                        WelcomeContent(actions.onUseCurrentLocation, actions.onOpenSearch)
                    }
                    is HomeContent.Error -> StaticPage(actions) {
                        HomeErrorContent(
                            location = content.location,
                            error = content.error,
                            onRetry = actions.onRetry,
                            onUseCurrentLocation = actions.onUseCurrentLocation,
                            onOpenSearch = actions.onOpenSearch,
                            onOpenSettings = actions.onOpenSettings,
                        )
                    }
                    is HomeContent.Success -> WeatherPage(content.data, state.isRefreshing, formats, actions)
                }
            }
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
            )
        }
    }
}

/** Same location keeps the same key, so a refresh updates in place instead of cross-fading. */
private fun HomeContent.animationKey(): String = when (this) {
    HomeContent.Loading -> "loading"
    HomeContent.NoLocation -> "welcome"
    is HomeContent.Error -> "error"
    is HomeContent.Success -> "weather_${data.location.id}"
}

@Composable
private fun StaticPage(actions: HomeActions, content: @Composable BoxScope.() -> Unit) {
    Column(Modifier.fillMaxSize()) {
        HomeTopBar(
            compactTitle = null,
            compactTitleAlpha = { 0f },
            onOpenLocations = actions.onOpenLocations,
            onUseCurrentLocation = actions.onUseCurrentLocation,
            onOpenSearch = actions.onOpenSearch,
        )
        Box(Modifier.fillMaxSize(), content = content)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeatherPage(data: HomeWeatherUi, isRefreshing: Boolean, formats: TimeFormats, actions: HomeActions) {
    val primaryListState = rememberLazyListState()
    val fadeDistancePx = with(LocalDensity.current) { HeaderFadeDistance.toPx() }
    val collapse: () -> Float = { primaryListState.headerCollapse(fadeDistancePx) }
    val pullState = rememberPullToRefreshState()

    Column(Modifier.fillMaxSize()) {
        HomeTopBar(
            compactTitle = stringResource(
                R.string.compact_title,
                data.location.displayName(),
                formatTemperature(data.weather.current.temperature),
            ),
            compactTitleAlpha = collapse,
            onOpenLocations = actions.onOpenLocations,
            onUseCurrentLocation = actions.onUseCurrentLocation,
            onOpenSearch = actions.onOpenSearch,
        )
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = actions.onRefresh,
            state = pullState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = pullState,
                    isRefreshing = isRefreshing,
                    modifier = Modifier.align(Alignment.TopCenter),
                    containerColor = OnWeather,
                    color = RefreshIndicatorColor,
                )
            },
        ) {
            BoxWithConstraints(Modifier.fillMaxSize()) {
                val primary = primarySections(data, formats, collapse)
                if (maxWidth >= Dimens.TwoPaneBreakpoint) {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = Dimens.SpaceS),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                    ) {
                        SectionList(primary, primaryListState, Modifier.weight(1f))
                        SectionList(
                            secondarySections(data, formats, COMPACT_DETAIL_COLUMNS),
                            rememberLazyListState(),
                            Modifier.weight(1f),
                        )
                    }
                } else {
                    val columns = if (maxWidth >= Dimens.MediumBreakpoint) MEDIUM_DETAIL_COLUMNS else COMPACT_DETAIL_COLUMNS
                    SectionList(
                        sections = primary + secondarySections(data, formats, columns),
                        state = primaryListState,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }
}

/** One block of the weather page; [content] receives the modifier with width and animation applied. */
private class Section(val key: String, val content: @Composable (Modifier) -> Unit)

private fun primarySections(data: HomeWeatherUi, formats: TimeFormats, collapse: () -> Float): List<Section> =
    buildList {
        add(
            Section("header") { modifier ->
                CurrentWeatherHeader(
                    data = data,
                    modifier = modifier.graphicsLayer { alpha = 1f - collapse() },
                )
            },
        )
        data.staleReason?.let { reason ->
            add(Section("offline") { OfflineBanner(data.weather.fetchedAt, reason, formats, it) })
        }
        if (data.hourly.isNotEmpty()) add(Section("hourly") { HourlyForecastCard(data.hourly, formats, it) })
        if (data.daily.isNotEmpty()) add(Section("daily") { DailyForecastCard(data.daily, formats, it) })
    }

private fun secondarySections(data: HomeWeatherUi, formats: TimeFormats, detailColumns: Int): List<Section> =
    buildList {
        add(Section("details") { DetailsGrid(data.details, detailColumns, it) })
        data.sun?.let { sun -> add(Section("sun") { SunCard(sun, formats, it) }) }
        add(Section("footer") { DataSourceFooter(it) })
    }

@Composable
private fun SectionList(sections: List<Section>, state: LazyListState, modifier: Modifier = Modifier) {
    val bottomInset: Dp = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    LazyColumn(
        state = state,
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
        contentPadding = PaddingValues(
            start = Dimens.ScreenPadding,
            end = Dimens.ScreenPadding,
            bottom = bottomInset + Dimens.SpaceL,
        ),
    ) {
        itemsIndexed(sections, key = { _, section -> section.key }) { index, section ->
            section.content(
                Modifier
                    .widthIn(max = Dimens.MaxContentWidth)
                    .fillMaxWidth()
                    .appearAnimation(index),
            )
        }
    }
}

/** 0 when the big header is fully visible, 1 once it has scrolled away. */
private fun LazyListState.headerCollapse(fadeDistancePx: Float): Float =
    if (firstVisibleItemIndex > 0) 1f else (firstVisibleItemScrollOffset / fadeDistancePx).coerceIn(0f, 1f)
