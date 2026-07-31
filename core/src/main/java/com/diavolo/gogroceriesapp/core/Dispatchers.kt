package com.diavolo.gogroceriesapp.core

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import javax.inject.Inject

interface AppDispatchers {
    val io: CoroutineDispatcher
    val default: CoroutineDispatcher
    val main: CoroutineDispatcher
}

class DefaultDispatchers @Inject constructor() : AppDispatchers {
    override val io: CoroutineDispatcher = IO
    override val default: CoroutineDispatcher = Default
    override val main: CoroutineDispatcher = Main
}
