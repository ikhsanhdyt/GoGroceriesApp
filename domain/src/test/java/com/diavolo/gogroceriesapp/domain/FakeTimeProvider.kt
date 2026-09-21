package com.diavolo.gogroceriesapp.domain

import com.diavolo.gogroceriesapp.common.TimeProvider

class FakeTimeProvider(var now: Long = 1_000L) : TimeProvider {
    override fun nowMillis(): Long = now
}
