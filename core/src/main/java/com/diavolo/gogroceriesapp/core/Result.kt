package com.diavolo.gogroceriesapp.core

sealed interface Result<out T> {
    data class Success<T>(val data: T) : Result<T>
    data class Failure(val cause: Throwable) : Result<Nothing>
}

inline fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

inline fun <T> Result<T>.onFailure(action: (Throwable) -> Unit): Result<T> {
    if (this is Result.Failure) action(cause)
    return this
}

inline fun <T, R> Result<T>.map(transform: (T) -> R): Result<R> {
    return when (this) {
        is Result.Success -> Result.Success(transform(data))
        is Result.Failure -> this
    }
}

inline fun <T, R> Result<T>.fold(
    onSuccess: (T) -> R,
    onFailure: (Throwable) -> R
): R {
    return when (this) {
        is Result.Success -> onSuccess(data)
        is Result.Failure -> onFailure(cause)
    }
}