package com.diavolo.gogroceriesapp.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.diavolo.gogroceriesapp.core.util.suspendRunCatching
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {

    /**
     * Runs [block] in [viewModelScope] and reports its result to [onSuccess] or [onError].
     * Cancellation is not reported as an error. [onFinally] runs after either callback.
     */
    protected fun <T> launchSafeJob(
        block: suspend () -> T,
        onSuccess: suspend (T) -> Unit = {},
        onError: suspend (Throwable) -> Unit = {},
        onFinally: suspend () -> Unit = {}
    ): Job = viewModelScope.launch {
        suspendRunCatching { block() }
            .onSuccess { onSuccess(it) }
            .onFailure { onError(it) }
        onFinally()
    }
}
