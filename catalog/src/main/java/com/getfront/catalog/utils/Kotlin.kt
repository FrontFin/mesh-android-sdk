package com.getfront.catalog.utils

internal fun <T> lazyNone(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)
