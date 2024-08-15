package com.meshconnect.link.utils

import android.net.Uri

internal fun <T> lazyNone(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)

internal fun getQueryParamFromUrl(url: String, key: String): String? {
    return try {
        Uri.parse(url).getQueryParameter(key)
    } catch (_: Exception) {
        null
    }
}