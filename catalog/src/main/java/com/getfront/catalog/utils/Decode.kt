package com.getfront.catalog.utils

import java.util.Base64

internal fun decodeBase64(source: String): String {
    return String(Base64.getDecoder().decode(source))
}

internal fun decodeCatching(source: String?) = runCatching {
    when {
        source.isNullOrEmpty() -> error("Empty 'catalogLink' or 'linkToken'.")
        source.startsWith("http") -> source
        else -> decodeBase64(source)
    }
}
