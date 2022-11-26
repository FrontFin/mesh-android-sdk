package com.getfront.catalog.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext

internal suspend inline fun <R : Any> runCatching(
    dispatcher: CoroutineDispatcher,
    noinline block: suspend CoroutineScope.() -> R
) = runCatching { withContext(dispatcher, block) }
