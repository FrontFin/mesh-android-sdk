package com.getfront.catalog.utils

internal inline fun String?.ifNotBlank(block: (String) -> Unit) {
    if (!isNullOrBlank()) block(this)
}

internal fun <T> lazyNone(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)
