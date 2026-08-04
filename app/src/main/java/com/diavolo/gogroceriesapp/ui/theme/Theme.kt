package com.diavolo.gogroceriesapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GroceryGreenLight,
    onPrimary = GroceryGreenDark,
    primaryContainer = GroceryGreenDark,
    onPrimaryContainer = GroceryGreenLight,
    secondary = GroceryGreenLight,
    background = GroceryOnSurface,
    onBackground = GrocerySurface,
    surface = GroceryOnSurface,
    onSurface = GrocerySurface,
    surfaceVariant = Color(0xFF27352D),
    onSurfaceVariant = Color(0xFFD2E1D7),
    outline = Color(0xFF9AAEA0),
    error = Color(0xFFFFB4AB)
)

private val LightColorScheme = lightColorScheme(
    primary = GroceryGreen,
    onPrimary = Color.White,
    primaryContainer = GroceryGreenContainer,
    onPrimaryContainer = GroceryGreenDark,
    secondary = GroceryGreen,
    onSecondary = Color.White,
    secondaryContainer = GroceryGreenContainer,
    onSecondaryContainer = GroceryGreenDark,
    background = GrocerySurface,
    onBackground = GroceryOnSurface,
    surface = Color.White,
    onSurface = GroceryOnSurface,
    surfaceVariant = GrocerySurfaceVariant,
    onSurfaceVariant = GroceryOnSurfaceVariant,
    outline = GroceryOutline,
    error = GroceryError
)

@Composable
fun GoGroceriesAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
