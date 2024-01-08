package com.meshconnect.link.utils

internal class OnLoadedScriptBuilder(
    private val version: String,
    private val accessTokens: String? = null,
    private val transferDestinationTokens: String? = null
) {
    fun build(): String {
        val builder = StringBuilder(
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version'"
        )
        if (accessTokens != null) {
            builder.append(";window.accessTokens='$accessTokens'")
        }
        if (transferDestinationTokens != null) {
            builder.append(";window.transferDestinationTokens='$transferDestinationTokens'")
        }
        return builder.toString()
    }
}
