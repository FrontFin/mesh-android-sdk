package com.meshconnect.link.utils

import java.net.URL

internal fun decodeBase64(source: String): String {
    return if (isAtLeastOreo) {
        String(java.util.Base64.getDecoder().decode(source))
    } else {
        String(android.util.Base64.decode(source, android.util.Base64.DEFAULT))
    }
}

internal fun decodeCatching(source: String?) = runCatching {
    when {
        source.isNullOrEmpty() -> error("Empty 'catalogLink' or 'linkToken'")
        source.startsWith("http") -> source
        else -> URL(decodeBase64(source)).toString()
    }
}
