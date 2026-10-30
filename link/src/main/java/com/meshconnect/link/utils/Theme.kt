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

// Link's own default with no theme anywhere.
private const val THEME_DEFAULT = THEME_DARK

private const val BASE64_BLOCK = 4

internal fun getThemeName(theme: LinkTheme?): String? =
    when (theme) {
        LinkTheme.DARK -> THEME_DARK
        LinkTheme.LIGHT -> THEME_LIGHT
        LinkTheme.SYSTEM -> THEME_SYSTEM
        else -> null
    }

/** [theme] as `light` or `dark`, with `system` read from the device; null for anything else. */
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

/**
 * The theme Link opens in, `light` or `dark`. First one set wins:
 * 1. the app's `theme` setting
 * 2. the `th` already in the token URL
 * 3. the `th` inside the token's `link_style`
 * 4. dark, Link's own default
 */
internal fun resolveLinkTheme(
    appTheme: String?,
    urlTheme: String?,
    linkStyle: String?,
    isSystemThemeDark: () -> Boolean,
): String =
    resolveTheme(appTheme, isSystemThemeDark)
        ?: resolveTheme(urlTheme, isSystemThemeDark)
        ?: resolveTheme(linkStyleTheme(linkStyle), isSystemThemeDark)
        ?: THEME_DEFAULT

internal fun Activity.isSystemThemeDark(): Boolean {
    val mask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mask == Configuration.UI_MODE_NIGHT_YES
}

// `link_style` is base64 JSON with an optional `th`. URL decoding turns its '+' into a space,
// so that's restored, and the padding is added back in case it was stripped.
private fun linkStyleTheme(linkStyle: String?): String? {
    if (linkStyle.isNullOrEmpty()) return null
    return try {
        val base64 = linkStyle.replace(' ', '+')
        val padded = base64 + "=".repeat((BASE64_BLOCK - base64.length % BASE64_BLOCK) % BASE64_BLOCK)
        JSONObject(decodeBase64(padded)).optString("th").takeIf { it.isNotEmpty() }
    } catch (expected: Exception) {
        Log.w("MeshSDK", "Couldn't read the theme from link_style", expected)
        null
    }
}

/**
 * The color for the status and navigation bars. One dark and one light color for every Link
 * version and client branding, close enough to all of them that there's no visible seam.
 */
@ColorRes
internal fun systemBarColor(isDarkTheme: Boolean): Int = if (isDarkTheme) R.color.linkBarDark else R.color.linkBarLight
