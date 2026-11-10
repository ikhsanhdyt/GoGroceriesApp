package com.diavolo.gogroceriesapp.core.ui.util

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import com.diavolo.gogroceriesapp.core.util.Constants.FIVE_HUNDRED_LONG
import com.diavolo.gogroceriesapp.core.util.Constants.ZERO_LONG

@Composable
fun rememberDebounceHandler(interval: Long = FIVE_HUNDRED_LONG) = remember { DebounceHandler(interval) }
fun ignoreInteractiveComponentSize() = LocalMinimumInteractiveComponentSize provides Dp.Unspecified

@OptIn(ExperimentalMaterial3Api::class)
fun disableRipple() = LocalRippleConfiguration provides null

@Stable
class DebounceHandler(private val interval: Long = FIVE_HUNDRED_LONG) {
    private var lastEventTimeMs = ZERO_LONG

    fun processClick(event: (() -> Unit)?) {
        val now = System.currentTimeMillis()
        if (now - lastEventTimeMs >= interval) event?.invoke()
        lastEventTimeMs = now
    }
}