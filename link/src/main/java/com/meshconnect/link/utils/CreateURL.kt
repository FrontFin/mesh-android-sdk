package com.meshconnect.link.utils

import java.net.URL

internal fun createURL(
    source: String,
    queryMap: Map<String, String?>? = null,
): URL {
    // Params go before any fragment, so Link still sees them.
    val hash = source.indexOf('#')
    val builder = StringBuilder(if (hash == -1) source else source.substring(0, hash))
    val fragment = if (hash == -1) "" else source.substring(hash)
    queryMap?.forEach { (key, value) ->
        if (key.isNotEmpty() && !value.isNullOrEmpty()) {
            builder.append(if (builder.contains('?')) "&" else "?")
            builder.append("$key=$value")
        }
    }
    return URL(builder.append(fragment).toString())
}

/** The Link URL the SDK opens. [theme] is already resolved to light or dark. */
internal fun createLinkURL(
    token: String,
    language: String?,
    fiatCurrency: String?,
    theme: String?,
): URL {
    // The token can already carry a th (some backends add th=dark); the app's theme replaces it.
    val link = if (theme != null) removeQueryParam(token, "th") else token
    return createURL(link, mapOf("lng" to language, "fiatCur" to fiatCurrency, "th" to theme, "platform" to "android"))
}
