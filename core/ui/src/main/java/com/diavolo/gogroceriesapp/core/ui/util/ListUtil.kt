package com.diavolo.gogroceriesapp.core.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import com.diavolo.gogroceriesapp.core.util.Constants.ZERO

object ListUtil {
    val VerticalLastItemAlwaysBottom: Arrangement.Vertical
        @Composable
        get() = object : Arrangement.Vertical {
            override fun Density.arrange(
                totalSize: Int,
                sizes: IntArray,
                outPositions: IntArray
            ) {
                var currentOffset = ZERO
                sizes.forEachIndexed { index, size ->
                    if (index == sizes.lastIndex) {
                        outPositions[index] = totalSize - size
                    } else {
                        outPositions[index] = currentOffset
                        currentOffset += size
                    }
                }
            }
        }
}