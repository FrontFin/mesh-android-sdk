package com.meshconnect.link.utils

import android.net.Uri
import android.util.Log

internal fun <T> lazyNone(initializer: () -> T): Lazy<T> =
    lazy(LazyThreadSafetyMode.NONE, initializer)

internal fun getQueryParamFromUrl(url: String, key: String): String? {
    return try {
        Uri.parse(url).getQueryParameter(key)
    } catch (expected: Exception) {
        Log.e("SDK", "getQueryParamFromUrl", expected)
        null
    }
}
