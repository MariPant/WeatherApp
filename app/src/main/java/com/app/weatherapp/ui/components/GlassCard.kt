package com.app.weatherapp.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.app.weatherapp.ui.theme.Dimens
import com.app.weatherapp.ui.theme.ON_WEATHER_SECONDARY_ALPHA
import com.app.weatherapp.ui.theme.OnWeather

/** Darkness of card fills; provided per weather palette so light gradients get stronger scrims. */
val LocalCardScrimAlpha = staticCompositionLocalOf { DEFAULT_SCRIM_ALPHA }

private const val DEFAULT_SCRIM_ALPHA = 0.18f
private const val CARD_BORDER_ALPHA = 0.12f
private const val DIVIDER_ALPHA = 0.18f

/** Translucent rounded card used for every block on a weather background. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(Dimens.CardPadding),
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(Dimens.CardCorner)
    Column(
        modifier = modifier
            .clip(shape)
            .background(Color.Black.copy(alpha = LocalCardScrimAlpha.current))
            .border(Dimens.CardBorder, OnWeather.copy(alpha = CARD_BORDER_ALPHA), shape)
            .padding(contentPadding),
        content = content,
    )
}

/** Small uppercase header with a glyph, e.g. "☼ UV INDEX". Marked as a heading for TalkBack. */
@Composable
fun CardHeader(title: String, glyph: CardGlyph, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceXs + Dimens.SpaceXxs),
    ) {
        val color = OnWeather.copy(alpha = ON_WEATHER_SECONDARY_ALPHA)
        Canvas(Modifier.size(Dimens.IconTiny)) { drawCardGlyph(glyph, color) }
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = color,
        )
    }
}

@Composable
fun CardDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = Dimens.CardBorder, color = OnWeather.copy(alpha = DIVIDER_ALPHA))
}
