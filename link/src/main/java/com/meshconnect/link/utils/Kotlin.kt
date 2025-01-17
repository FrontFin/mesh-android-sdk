package com.meshconnect.link.utils

internal fun <T> lazyNone(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)
