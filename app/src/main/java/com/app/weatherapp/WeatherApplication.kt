package com.app.weatherapp

import android.app.Application
import com.app.weatherapp.di.AppContainer

class WeatherApplication : Application() {
    val container: AppContainer by lazy { AppContainer(this) }
}
