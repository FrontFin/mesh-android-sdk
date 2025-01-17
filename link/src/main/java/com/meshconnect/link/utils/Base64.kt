package com.meshconnect.link.utils

import java.net.URL

internal fun decodeBase64(source: String): String {
    return if (isAtLeastOreo) {
        String(java.util.Base64.getDecoder().decode(source))
    } else {
        String(android.util.Base64.decode(source, android.util.Base64.DEFAULT))
    }
}

internal fun decodeToURL(source: String?) = runCatching {
    when {
        source.isNullOrEmpty() -> error("Empty source")
        source.startsWith("http") -> URL(source)
        else -> URL(decodeBase64(source))
    }
}
