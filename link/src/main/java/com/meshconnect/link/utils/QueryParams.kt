package com.meshconnect.link.utils

import java.net.URLDecoder

// Plain string helpers instead of android.net.Uri, so the token URL is never re-encoded.

private fun splitFragment(url: String): Pair<String, String> {
    val hash = url.indexOf('#')
    return if (hash == -1) url to "" else url.substring(0, hash) to url.substring(hash)
}

private fun queryPairs(url: String): List<String> {
    val beforeFragment = splitFragment(url).first
    val queryStart = beforeFragment.indexOf('?')
    return if (queryStart == -1) emptyList() else beforeFragment.substring(queryStart + 1).split('&')
}

private fun decodeQueryComponent(value: String): String =
    try {
        URLDecoder.decode(value, "UTF-8")
    } catch (expected: IllegalArgumentException) {
        value.replace('+', ' ')
    }

private fun pairKey(pair: String): String = decodeQueryComponent(pair.substringBefore('='))

/** First value of [name], or null when it's missing. */
internal fun getFirstQueryParam(
    url: String,
    name: String,
): String? {
    val pair = queryPairs(url).firstOrNull { it.isNotEmpty() && pairKey(it) == name } ?: return null
    return if (pair.contains('=')) decodeQueryComponent(pair.substringAfter('=')) else ""
}

/** Removes every [name], leaving the rest of the URL byte for byte. */
internal fun removeQueryParam(
    url: String,
    name: String,
): String {
    val (beforeFragment, fragment) = splitFragment(url)
    val queryStart = beforeFragment.indexOf('?')
    if (queryStart == -1) return url
    val kept = queryPairs(url).filter { it.isEmpty() || pairKey(it) != name }
    val base = beforeFragment.substring(0, queryStart)
    return base + (if (kept.isEmpty()) "" else "?" + kept.joinToString("&")) + fragment
}
