package com.app.weatherapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import com.app.weatherapp.presentation.common.WeatherIconType
import com.app.weatherapp.ui.theme.Dimens

private val ThemedIconBadge = Color(0xFF3A84DB)

data class MessageAction(val label: String, val onClick: () -> Unit)

/** Colours for [MessageState]; weather screens use white on gradient, list screens use the theme. */
data class MessageColors(
    val title: Color,
    val body: Color,
    val primaryContainer: Color,
    val primaryContent: Color,
    val secondaryContent: Color,
    /** Badge behind the glyph; needed on light surfaces where white clouds would disappear. */
    val iconBackground: Color = Color.Transparent,
)

/**
 * Centered icon + title + explanation + actions. Used for errors, empty states and onboarding,
 * so all "nothing to show" situations look and behave consistently.
 */
@Composable
fun MessageState(
    icon: WeatherIconType,
    title: String,
    message: String,
    colors: MessageColors,
    modifier: Modifier = Modifier,
    primaryAction: MessageAction? = null,
    secondaryAction: MessageAction? = null,
    tertiaryAction: MessageAction? = null,
) {
    Column(
        modifier = modifier
            .widthIn(max = Dimens.MaxContentWidth)
            .padding(horizontal = Dimens.SpaceXl, vertical = Dimens.SpaceXxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
    ) {
        Box(
            modifier = Modifier
                .size(Dimens.IconHero + Dimens.SpaceXl)
                .background(colors.iconBackground, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            WeatherIcon(type = icon, contentDescription = null, size = Dimens.IconHero)
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            color = colors.title,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics { heading() },
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.body,
            textAlign = TextAlign.Center,
        )
        Column(
            modifier = Modifier.padding(top = Dimens.SpaceS),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        ) {
            primaryAction?.let { action ->
                Button(
                    onClick = action.onClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.MinTouchTarget),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primaryContainer,
                        contentColor = colors.primaryContent,
                    ),
                ) { Text(action.label) }
            }
            secondaryAction?.let { action ->
                OutlinedButton(
                    onClick = action.onClick,
                    modifier = Modifier.fillMaxWidth().heightIn(min = Dimens.MinTouchTarget),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colors.secondaryContent),
                ) { Text(action.label) }
            }
            tertiaryAction?.let { action ->
                TextButton(
                    onClick = action.onClick,
                    modifier = Modifier.heightIn(min = Dimens.MinTouchTarget),
                    colors = ButtonDefaults.textButtonColors(contentColor = colors.secondaryContent),
                ) { Text(action.label) }
            }
        }
    }
}

/** Message colours for screens that use the regular app theme (search, locations). */
@Composable
fun themedMessageColors(): MessageColors {
    val colors = MaterialTheme.colorScheme
    return MessageColors(
        title = colors.onSurface,
        body = colors.onSurfaceVariant,
        primaryContainer = colors.primary,
        primaryContent = colors.onPrimary,
        secondaryContent = colors.primary,
        iconBackground = ThemedIconBadge,
    )
}
