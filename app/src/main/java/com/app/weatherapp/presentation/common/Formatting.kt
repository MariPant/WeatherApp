package com.app.weatherapp.presentation.common

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.ConfigurationCompat
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/** "18°". Rounds to whole degrees like most weather apps do. */
fun formatTemperature(celsius: Double): String = "${celsius.roundToInt()}°"

/** 🇫🇮 from "FI" using Unicode regional indicator symbols; null for invalid codes. */
fun countryFlagEmoji(countryCode: String?): String? {
    val code = countryCode?.uppercase(Locale.ROOT)?.takeIf { it.length == 2 && it.all { c -> c in 'A'..'Z' } }
        ?: return null
    val regionalIndicatorOffset = 0x1F1E6 - 'A'.code
    return code.map { String(Character.toChars(it.code + regionalIndicatorOffset)) }.joinToString("")
}

/** Date/time patterns that follow the user's locale and 12/24-hour preference. */
class TimeFormats(is24Hour: Boolean, locale: Locale) {
    val time: DateTimeFormatter = DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a", locale)
    val hour: DateTimeFormatter = DateTimeFormatter.ofPattern(if (is24Hour) "HH" else "h a", locale)
    val dayShort: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE", locale)
    val dayLong: DateTimeFormatter = DateTimeFormatter.ofPattern("EEEE", locale)
}

@Composable
fun rememberTimeFormats(): TimeFormats {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val locale = ConfigurationCompat.getLocales(configuration)[0] ?: Locale.getDefault()
    val is24Hour = DateFormat.is24HourFormat(context)
    return remember(locale, is24Hour) { TimeFormats(is24Hour, locale) }
}
