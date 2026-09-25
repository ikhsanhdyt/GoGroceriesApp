package com.diavolo.gogroceriesapp.core.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private const val DISPLAY_DATE_PATTERN = "dd MMM yyyy"

fun Long.toDisplayDate(
    zone: ZoneId = ZoneId.systemDefault(),
    locale: Locale = Locale.getDefault()
): String = Instant
    .ofEpochMilli(this)
    .atZone(zone)
    .format(DateTimeFormatter.ofPattern(DISPLAY_DATE_PATTERN, locale))
