package com.meshconnect.link.utils

internal fun decodeBase64(source: String): String {
    return if (isAtLeastOreo) {
        String(java.util.Base64.getDecoder().decode(source))
    } else {
        String(android.util.Base64.decode(source, android.util.Base64.DEFAULT))
    }
}
