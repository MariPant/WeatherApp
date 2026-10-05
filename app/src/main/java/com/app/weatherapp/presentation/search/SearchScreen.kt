package com.app.weatherapp.presentation.search

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.app.weatherapp.R
import com.app.weatherapp.domain.model.WeatherLocation
import com.app.weatherapp.presentation.common.ObserveAsEvents
import com.app.weatherapp.presentation.common.WeatherIconType
import com.app.weatherapp.presentation.common.countryFlagEmoji
import com.app.weatherapp.presentation.common.toErrorText
import com.app.weatherapp.ui.components.MessageAction
import com.app.weatherapp.ui.components.MessageState
import com.app.weatherapp.ui.components.ShimmerContainer
import com.app.weatherapp.ui.components.SkeletonBlock
import com.app.weatherapp.ui.components.SystemBarIcons
import com.app.weatherapp.ui.components.themedMessageColors
import com.app.weatherapp.ui.theme.Dimens
import kotlinx.coroutines.launch

private const val SKELETON_ROWS = 6
private const val SKELETON_TITLE_FRACTION = 0.5f
private const val SKELETON_SUBTITLE_FRACTION = 0.3f

@Composable
fun SearchRoute(
    onBack: () -> Unit,
    onLocationChosen: () -> Unit,
    viewModel: SearchViewModel = viewModel(factory = SearchViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isCurrentLocationEnabled by viewModel.isCurrentLocationEnabled.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val permissionDeniedMessage = stringResource(R.string.error_location_permission_short)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SearchEvent.LocationChosen -> onLocationChosen()
            SearchEvent.LocationPermissionDenied -> scope.launch {
                snackbarHostState.showSnackbar(permissionDeniedMessage)
            }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        viewModel::onLocationPermissionResult,
    )

    SearchScreen(
        state = state,
        showUseCurrentLocation = !isCurrentLocationEnabled,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onRetry = viewModel::retry,
        onCitySelected = viewModel::onCitySelected,
        onUseCurrentLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
        onBack = onBack,
    )
}

@Composable
fun SearchScreen(
    state: SearchUiState,
    showUseCurrentLocation: Boolean,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onRetry: () -> Unit,
    onCitySelected: (WeatherLocation) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onBack: () -> Unit,
) {
    SystemBarIcons(useDarkIcons = !isSystemInDarkTheme())
    var query by rememberSaveable { mutableStateOf("") }
    // Re-sync after process death: the text survives in saved state, the ViewModel does not.
    LaunchedEffect(Unit) { if (query.isNotEmpty()) onQueryChange(query) }

    Scaffold(
        topBar = {
            SearchField(
                query = query,
                onQueryChange = {
                    query = it
                    onQueryChange(it)
                },
                onBack = onBack,
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            AnimatedVisibility(visible = state is SearchUiState.Loading, enter = fadeIn(), exit = fadeOut()) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
            Box(Modifier.widthIn(max = Dimens.MaxContentWidth).fillMaxSize()) {
                when (state) {
                    SearchUiState.Idle -> IdleContent(showUseCurrentLocation, onUseCurrentLocation)
                    SearchUiState.Loading -> ResultsSkeleton()
                    is SearchUiState.Results -> ResultsList(state.cities, onCitySelected)
                    is SearchUiState.NoResults -> MessageState(
                        icon = WeatherIconType.FOG,
                        title = stringResource(R.string.search_no_results_title, state.query),
                        message = stringResource(R.string.search_no_results_message),
                        colors = themedMessageColors(),
                        modifier = Modifier.align(Alignment.TopCenter),
                    )
                    is SearchUiState.Error -> {
                        val text = state.error.toErrorText()
                        MessageState(
                            icon = WeatherIconType.CLOUD,
                            title = stringResource(text.title),
                            message = stringResource(text.message),
                            colors = themedMessageColors(),
                            modifier = Modifier.align(Alignment.TopCenter),
                            primaryAction = MessageAction(stringResource(R.string.action_retry), onRetry),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit, onBack: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(end = Dimens.ScreenPadding, top = Dimens.SpaceS, bottom = Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, stringResource(R.string.action_back))
        }
        TextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            placeholder = { Text(stringResource(R.string.search_placeholder)) },
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = { onQueryChange("") }) {
                        Icon(Icons.Rounded.Close, stringResource(R.string.action_clear))
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(Dimens.CardCorner),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ),
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Search,
            ),
            keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() }),
        )
    }
}

@Composable
private fun IdleContent(showUseCurrentLocation: Boolean, onUseCurrentLocation: () -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        if (showUseCurrentLocation) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.action_use_current_location)) },
                leadingContent = {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.clickable(onClick = onUseCurrentLocation),
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
            HorizontalDivider(Modifier.padding(horizontal = Dimens.ScreenPadding))
        }
        Text(
            text = stringResource(R.string.search_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(Dimens.ScreenPadding),
        )
    }
}

@Composable
private fun ResultsList(cities: List<WeatherLocation>, onCitySelected: (WeatherLocation) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(cities, key = { it.id }) { city ->
            val subtitle = city.subtitle
            ListItem(
                headlineContent = {
                    Text(city.name, maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                supportingContent = if (subtitle != null) {
                    { Text(subtitle, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                } else {
                    null
                },
                leadingContent = {
                    // The flag is decorative; the country name is in the supporting text.
                    Box(Modifier.size(Dimens.IconLarge), contentAlignment = Alignment.Center) {
                        Text(
                            text = countryFlagEmoji(city.countryCode) ?: "📍",
                            style = MaterialTheme.typography.titleLarge,
                            modifier = Modifier.clearAndSetSemantics { },
                        )
                    }
                },
                modifier = Modifier
                    .animateItem()
                    .clickable { onCitySelected(city) },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

@Composable
private fun ResultsSkeleton() {
    val base = MaterialTheme.colorScheme.surfaceContainerHigh
    val highlight = MaterialTheme.colorScheme.surfaceContainerHighest
    ShimmerContainer {
        Column(Modifier.fillMaxWidth().padding(Dimens.ScreenPadding)) {
            repeat(SKELETON_ROWS) {
                Row(
                    modifier = Modifier.padding(vertical = Dimens.SpaceM),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    SkeletonBlock(Modifier.size(Dimens.IconLarge), CircleShape, base, highlight)
                    Column(Modifier.padding(start = Dimens.SpaceL).weight(1f)) {
                        SkeletonBlock(
                            Modifier.fillMaxWidth(SKELETON_TITLE_FRACTION).height(Dimens.SpaceL),
                            baseColor = base,
                            highlightColor = highlight,
                        )
                        SkeletonBlock(
                            Modifier
                                .padding(top = Dimens.SpaceS)
                                .fillMaxWidth(SKELETON_SUBTITLE_FRACTION)
                                .height(Dimens.SpaceM),
                            baseColor = base,
                            highlightColor = highlight,
                        )
                    }
                }
            }
        }
    }
}
