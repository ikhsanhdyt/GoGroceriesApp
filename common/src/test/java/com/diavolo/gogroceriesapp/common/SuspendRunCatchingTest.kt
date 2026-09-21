package com.diavolo.gogroceriesapp.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.coroutines.cancellation.CancellationException

class SuspendRunCatchingTest {

    @Test
    fun returnsSuccessWithTheBlockValue() {
        val result = suspendRunCatching { 42 }

        assertEquals(42, result.getOrNull())
    }

    @Test
    fun returnsFailureForARegularException() {
        val error = IllegalStateException("boom")

        val result = suspendRunCatching { throw error }

        assertTrue(result.isFailure)
        assertSame(error, result.exceptionOrNull())
    }

    @Test
    fun rethrowsCancellationInsteadOfReportingFailure() {
        val cancellation = CancellationException("cancelled")

        val thrown = assertThrows(CancellationException::class.java) {
            suspendRunCatching { throw cancellation }
        }

        assertSame(cancellation, thrown)
    }
}
