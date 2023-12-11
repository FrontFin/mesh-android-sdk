package com.meshconnect.link.utils

@Suppress("MaxLineLength")
internal class OnPageFinishedScriptBuilder(
    private val version: String,
    private val accessTokens: String? = null,
    private val transferDestinationTokens: String? = null
) {
    fun build(): String {
        val builder = StringBuilder(
            "window.meshSdkPlatform='android';window.meshSdkVersion='$version';if(window.parent) {window.parent.meshSdkPlatform='android';window.parent.meshSdkVersion='$version';}"
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
