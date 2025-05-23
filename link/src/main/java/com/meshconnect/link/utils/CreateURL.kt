package com.meshconnect.link.utils

import java.net.URL

internal fun createURL(
    source: String,
    queryMap: Map<String, String?>? = null,
): URL {
    val builder = StringBuilder(source)
    queryMap?.forEach { (key, value) ->
        if (key.isNotEmpty() && !value.isNullOrEmpty()) {
            builder.append(if (builder.toString().contains("?")) "&" else "?")
            builder.append("$key=$value")
        }
    }
    return URL(builder.toString())
}
