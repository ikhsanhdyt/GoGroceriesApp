package com.diavolo.gogroceriesapp.core.base

import com.diavolo.gogroceriesapp.core.base.BaseState.StateFailed
import com.diavolo.gogroceriesapp.core.base.BaseState.StateLoading
import com.diavolo.gogroceriesapp.core.base.BaseState.StateSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

sealed class BaseState<out T> {

    data object StateInitial : BaseState<Nothing>()

    data object StateLoading : BaseState<Nothing>()

    data class StateSuccess<out T>(val data: T) : BaseState<T>()

    data class StateFailed(val error: Throwable) : BaseState<Nothing>()

    fun data(): T? = (this as? StateSuccess)?.data

    fun error(): Throwable? = (this as? StateFailed)?.error
}

/**
 * Wraps each value in [StateSuccess], starting with [StateLoading]. An upstream exception ends
 * the stream with [StateFailed]; cancellation is not caught.
 */
fun <T> Flow<T>.asBaseState(): Flow<BaseState<T>> =
    map<T, BaseState<T>> { StateSuccess(it) }
        .onStart { emit(StateLoading) }
        .catch { emit(StateFailed(it)) }
