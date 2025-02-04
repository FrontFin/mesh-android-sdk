package com.meshconnect.link.utils

internal fun getOnLoadedScript(
    version: String,
    accessTokens: String? = null,
    transferDestinationTokens: String? = null,
): String =
    StringBuilder(
        "window.meshSdkPlatform='android';window.meshSdkVersion='$version'",
    ).apply {
        if (accessTokens != null) {
            append(";window.accessTokens='$accessTokens'")
        }
        if (transferDestinationTokens != null) {
            append(";window.transferDestinationTokens='$transferDestinationTokens'")
        }
    }.toString()
