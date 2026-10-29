package com.meshconnect.link.utils

import android.app.Activity
import android.content.res.Configuration
import android.util.Log
import androidx.annotation.ColorRes
import com.meshconnect.link.R
import com.meshconnect.link.entity.LinkTheme
import org.json.JSONObject

internal const val THEME_DARK = "dark"
internal const val THEME_LIGHT = "light"
internal const val THEME_SYSTEM = "system"

internal fun getThemeName(theme: LinkTheme?): String? =
    when (theme) {
        LinkTheme.DARK -> THEME_DARK
        LinkTheme.LIGHT -> THEME_LIGHT
        LinkTheme.SYSTEM -> THEME_SYSTEM
        else -> null
    }

internal fun resolveTheme(
    theme: String?,
    isSystemThemeDark: () -> Boolean,
): String? =
    when (theme) {
        THEME_DARK -> THEME_DARK
        THEME_LIGHT -> THEME_LIGHT
        THEME_SYSTEM -> if (isSystemThemeDark()) THEME_DARK else THEME_LIGHT
        else -> null
    }

internal fun Activity.isSystemThemeDark(): Boolean {
    val mask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mask == Configuration.UI_MODE_NIGHT_YES
}

private const val BASE64_BLOCK = 4

// Base64 or base64url, padded; a '+' turned into a space by URL decoding is restored.
private fun decodeBase64Param(value: String): String {
    val base64 = value.replace(' ', '+').replace('-', '+').replace('_', '/')
    return decodeBase64(base64 + "=".repeat((BASE64_BLOCK - base64.length % BASE64_BLOCK) % BASE64_BLOCK))
}

private fun getLinkStyleTheme(url: String): String? {
    val linkStyle = getFirstQueryParam(url, "link_style") ?: return null
    return JSONObject(decodeBase64Param(linkStyle)).optString("th").takeIf { it.isNotBlank() }
}

// The latest Link's `token` param, validated the way Link does; null for older links.
private fun getTokenEnvelope(url: String): JSONObject? {
    val token = getFirstQueryParam(url, "token")?.takeIf { it.isNotEmpty() } ?: return null
    return try {
        val envelope = JSONObject(decodeBase64Param(token))
        val client = envelope.opt("c")
        envelope.takeIf { it.opt("v") is Number && client is String && client.isNotEmpty() }
    } catch (expected: Exception) {
        null
    }
}

internal fun isLatestLinkUrl(url: String): Boolean = getTokenEnvelope(url) != null

/**
 * Whether Link will render dark, resolved the way the page does so the native background
 * matches it: the first `th` param wins, then legacy `link_style`, then the latest Link
 * token's theme. Such a token without a theme follows the device; legacy links default to light.
 */
internal fun isDarkLinkTheme(
    url: String,
    isSystemThemeDark: () -> Boolean,
): Boolean {
    val th = getFirstQueryParam(url, "th")?.takeIf { it == THEME_DARK || it == THEME_LIGHT }
    val linkStyleTheme =
        try {
            getLinkStyleTheme(url)
        } catch (expected: Exception) {
            Log.e("MeshSDK", "isDarkLinkTheme", expected)
            null
        }
    return when {
        th != null -> th == THEME_DARK
        linkStyleTheme == THEME_SYSTEM -> isSystemThemeDark()
        linkStyleTheme == THEME_DARK || linkStyleTheme == THEME_LIGHT -> linkStyleTheme == THEME_DARK
        else -> isLatestLinkDark(url, isSystemThemeDark)
    }
}

private fun isLatestLinkDark(
    url: String,
    isSystemThemeDark: () -> Boolean,
): Boolean {
    val envelope = getTokenEnvelope(url) ?: return false
    return when (envelope.optString("th")) {
        "LINK_THEME_DARK" -> true
        "LINK_THEME_LIGHT" -> false
        else -> isSystemThemeDark()
    }
}

// The latest Link uses one canvas color (--color-bg-canvas-dark/light in FrontFin/link
// packages/ui/src/styles/tailwind.css), so both bars match it and there's no seam.
@ColorRes
internal fun themeColor(
    isDarkTheme: Boolean,
    isLatestLink: Boolean,
    top: Boolean,
): Int =
    when {
        isLatestLink -> if (isDarkTheme) R.color.linkCanvasDark else R.color.linkCanvasLight
        top -> if (isDarkTheme) R.color.coldGray70 else R.color.coldGray0
        else -> if (isDarkTheme) R.color.gray80 else R.color.gray0
    }
