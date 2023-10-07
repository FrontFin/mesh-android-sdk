package com.meshconnect.link.utils

import java.net.URL
import java.util.Base64

internal fun decodeBase64(source: String): String {
    return String(Base64.getDecoder().decode(source))
}

internal fun decodeCatching(source: String?) = runCatching {
    when {
        source.isNullOrEmpty() -> error("Empty 'catalogLink' or 'linkToken'")
        source.startsWith("http") -> source
        else -> URL(decodeBase64(source)).toString()
    }
}
