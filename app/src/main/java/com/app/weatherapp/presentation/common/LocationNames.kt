package com.app.weatherapp.presentation.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.app.weatherapp.R
import com.app.weatherapp.domain.model.WeatherLocation

/** Device location without a resolved place name reads "My Location". */
@Composable
fun WeatherLocation.displayName(): String =
    name.ifBlank { stringResource(R.string.my_location) }
