package com.meshconnect.link.utils

import android.app.Activity
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import com.meshconnect.link.entity.LinkTheme
import org.json.JSONObject

internal const val THEME_DARK = "dark"
internal const val THEME_LIGHT = "light"
internal const val THEME_SYSTEM = "system"

private const val SESSION_THEME_DARK = "LINK_THEME_DARK"
private const val SESSION_THEME_LIGHT = "LINK_THEME_LIGHT"
private const val BASE64_BLOCK = 4

internal fun getThemeName(theme: LinkTheme?): String? =
    when (theme) {
        LinkTheme.DARK -> THEME_DARK
        LinkTheme.LIGHT -> THEME_LIGHT
        LinkTheme.SYSTEM -> THEME_SYSTEM
        else -> null
    }

// Keeps the system bars in step with Link: an explicit light/dark wins, then the
// session token's theme, then the device setting.
internal fun resolveSystemBarsTheme(
    urlTheme: String?,
    sessionTokenTheme: () -> String?,
    isSystemThemeDark: () -> Boolean,
): String =
    when (urlTheme) {
        THEME_DARK, THEME_LIGHT -> urlTheme
        else -> sessionTokenTheme() ?: if (isSystemThemeDark()) THEME_DARK else THEME_LIGHT
    }

// Reads the theme Link will use from the session token, if present.
// Returns null for tokens without a theme. Never logs the token.
internal fun getSessionTokenTheme(token: String?): String? {
    if (token.isNullOrBlank()) return null
    return try {
        val base64 = token.replace('-', '+').replace('_', '/')
        val padding = (BASE64_BLOCK - base64.length % BASE64_BLOCK) % BASE64_BLOCK
        val envelope = JSONObject(decodeBase64(base64 + "=".repeat(padding)))
        val isEnvelope = envelope.opt("v") is Number && (envelope.opt("c") as? String).orEmpty().isNotEmpty()
        when {
            !isEnvelope -> null
            envelope.opt("th") == SESSION_THEME_DARK -> THEME_DARK
            envelope.opt("th") == SESSION_THEME_LIGHT -> THEME_LIGHT
            else -> null
        }
    } catch (expected: Exception) {
        null
    }
}

internal fun Activity.isSystemThemeDark(): Boolean {
    val mask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mask == Configuration.UI_MODE_NIGHT_YES
}

// Uses the last `th` param, since the SDK's value is appended after any already in the URL.
internal fun getThemeFromUrl(url: String): String? {
    return try {
        val uri = Uri.parse(url)
        val th = uri.getQueryParameters("th")?.lastOrNull()
        if (!th.isNullOrBlank()) return th

        val linkStyle = uri.getQueryParameter("link_style")
        linkStyle?.let {
            JSONObject(decodeBase64(it)).optString("th").takeIf { s -> s.isNotBlank() }
        }
    } catch (expected: Exception) {
        Log.e("MeshSDK", "getThemeFromUrl", expected)
        null
    }
}

// Never logs the token.
internal fun getSessionTokenFromUrl(url: String): String? =
    try {
        Uri.parse(url).getQueryParameter("token")
    } catch (expected: Exception) {
        null
    }
