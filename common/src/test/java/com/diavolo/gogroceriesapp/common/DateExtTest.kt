package com.diavolo.gogroceriesapp.common

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.util.Locale

class DateExtTest {

    // 2024-03-05T23:30:00Z
    private val timestamp = 1_709_681_400_000L

    @Test
    fun toDisplayDate_formatsInTheGivenZone() {
        assertEquals(
            "05 Mar 2024",
            timestamp.toDisplayDate(ZoneId.of("UTC"), Locale.ENGLISH)
        )
    }

    @Test
    fun toDisplayDate_movesToNextDayInEarlierTimeZone() {
        assertEquals(
            "06 Mar 2024",
            timestamp.toDisplayDate(ZoneId.of("Asia/Jakarta"), Locale.ENGLISH)
        )
    }
}
