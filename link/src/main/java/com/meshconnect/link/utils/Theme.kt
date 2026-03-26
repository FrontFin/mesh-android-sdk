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

internal fun getThemeFromUrl(url: String): String? {
    return try {
        val uri = Uri.parse(url)
        val th = uri.getQueryParameter("th")
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
