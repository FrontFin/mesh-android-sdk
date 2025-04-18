package com.meshconnect.link.utils

internal fun decodeToken(token: String?): String {
    return when {
        token.isNullOrEmpty() -> error("Empty source")
        token.startsWith("http") -> token
        else -> decodeBase64(token)
    }
}
