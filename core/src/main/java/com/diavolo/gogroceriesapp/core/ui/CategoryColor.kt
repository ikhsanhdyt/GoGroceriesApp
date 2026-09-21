package com.diavolo.gogroceriesapp.core.ui

import android.graphics.Color.parseColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/**
 * Converts a category hex string (e.g. "#4F8A5B") to a [Color].
 * Falls back to the theme's primary color when the string cannot be parsed.
 */
@Composable
fun categoryColor(colorHex: String): Color =
    runCatching { Color(parseColor(colorHex)) }
        .getOrDefault(MaterialTheme.colorScheme.primary)
