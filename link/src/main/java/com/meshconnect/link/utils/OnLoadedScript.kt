package com.meshconnect.link.utils

internal fun getOnLoadedScript(
    version: String,
    accessTokens: String? = null,
): String =
    StringBuilder(
        "window.meshSdkPlatform='android';window.meshSdkVersion='$version'",
    ).apply {
        if (accessTokens != null) {
            append(";window.accessTokens='$accessTokens'")
        }
    }.toString()
