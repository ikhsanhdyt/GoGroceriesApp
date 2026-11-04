package com.diavolo.gogroceriesapp.core.util

import kotlin.coroutines.cancellation.CancellationException

/**
 * Like [runCatching], but safe to use around suspend calls: a [CancellationException] is rethrown
 * instead of being reported as a failure, so coroutine cancellation keeps propagating.
 */
inline fun <T> suspendRunCatching(block: () -> T): Result<T> =
    try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
