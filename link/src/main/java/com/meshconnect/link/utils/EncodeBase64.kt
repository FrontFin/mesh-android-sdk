package com.meshconnect.link.utils

internal fun encodeBase64(source: String): String {
    return if (isAtLeastOreo) {
        java.util.Base64.getEncoder().encodeToString(source.toByteArray())
    } else {
        // NO_WRAP, not DEFAULT: DEFAULT inserts line breaks every 76 chars,
        // which would corrupt a link token.
        android.util.Base64.encodeToString(source.toByteArray(), android.util.Base64.NO_WRAP)
    }
}
