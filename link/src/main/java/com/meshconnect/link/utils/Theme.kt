package com.meshconnect.link.utils

import android.app.Activity
import android.content.res.Configuration
import androidx.annotation.ColorInt
import com.meshconnect.link.entity.LinkTheme
import org.json.JSONObject

internal const val THEME_DARK = "dark"
internal const val THEME_LIGHT = "light"
internal const val THEME_SYSTEM = "system"

// What link-v2 shows when nothing sets a theme.
private const val THEME_DEFAULT = THEME_DARK

private const val BASE64_BLOCK = 4

// The short hex forms (#RGB, #RGBA) repeat each digit; the long ones start with #RRGGBB.
private const val SHORT_HEX_MAX = 4
private const val RGB_DIGITS = 6
private const val HEX_RADIX = 16

// Perceived brightness of a color: weighted RGB, 0 (black) to 1 (white).
private const val RED_SHIFT = 16
private const val GREEN_SHIFT = 8
private const val BYTE_MASK = 0xFF
private const val LUMA_RED = 0.299
private const val LUMA_GREEN = 0.587
private const val LUMA_BLUE = 0.114
private const val HALF = 0.5

// Same shape Link accepts for a page color: #RGB, #RGBA, #RRGGBB or #RRGGBBAA.
private val HEX_COLOR = Regex("^#(?:[0-9a-fA-F]{3,4}|[0-9a-fA-F]{6}|[0-9a-fA-F]{8})$")

private const val OPAQUE = 0xFF000000.toInt()

// The color around Link when the page has no valid color of its own, per Link version.
private const val SESSION_DARK = 0xFF1C1C1C.toInt()
private const val SESSION_LIGHT = 0xFFF9F9F9.toInt()
private const val LEGACY_DARK = 0xFF0E0D0D.toInt()
private const val LEGACY_LIGHT = 0xFFFBFBFB.toInt()

/** The theme Link opens in and the color around it. */
internal data class LinkAppearance(
    val isDarkTheme: Boolean,
    @ColorInt val backgroundColor: Int,
    /** Set when a legacy link leaves the theme to the device; see [resolveLinkAppearance]. */
    val themeToWrite: String?,
)

internal fun getThemeName(theme: LinkTheme?): String? =
    when (theme) {
        LinkTheme.DARK -> THEME_DARK
        LinkTheme.LIGHT -> THEME_LIGHT
        LinkTheme.SYSTEM -> THEME_SYSTEM
        else -> null
    }

/**
 * The theme Link opens in and the color around it, read from the URL the way the page reads it,
 * so the status bar, navigation bar and background match with no visible seam. [appTheme] is the
 * app's theme, which replaces the `th` in the URL.
 *
 * Theme, first one set wins:
 * 1. `th=light` or `th=dark` (the app's choice, or the one the token came with)
 * 2. the theme in the session token, for a link that has one
 * 3. `th=system` means the device on any link
 * 4. `link_style.th` (legacy links only), `system` meaning the device
 * 5. the device for a link with a session token, otherwise the default (dark, like link-v2)
 *
 * Color: the page's own `bgd` (dark) or `bgl` (light) from the session token or `link_style`,
 * otherwise the fallback pair of that Link version (a link with a session token, or a legacy link).
 *
 * [LinkAppearance.themeToWrite] is set for a legacy link whose theme comes from the device. The
 * WebView can read the device differently from the app (a host app with a dark-only theme), so the
 * caller writes this resolved theme into the URL and Link follows the same reading as the bars.
 */
internal fun resolveLinkAppearance(
    query: QueryString,
    appTheme: String?,
    isSystemThemeDark: Boolean,
): LinkAppearance {
    val sessionToken = readSessionToken(query)
    val linkStyle = if (sessionToken == null) decodeJsonParam(query.first("link_style")) else null
    val choice = resolveChoice(appTheme ?: query.first("th"), sessionToken, linkStyle)
    val theme = if (choice == DEVICE) (if (isSystemThemeDark) THEME_DARK else THEME_LIGHT) else choice
    val isDarkTheme = theme == THEME_DARK

    val pageStyle = sessionToken ?: linkStyle
    return LinkAppearance(
        isDarkTheme = isDarkTheme,
        backgroundColor =
            toOpaqueColor(pageStyle?.opt(if (isDarkTheme) "bgd" else "bgl"))
                ?: fallbackColor(isDarkTheme, isSessionLink = sessionToken != null),
        themeToWrite = theme.takeIf { choice == DEVICE && sessionToken == null },
    )
}

/** `light`, `dark` or [DEVICE], in the order described at [resolveLinkAppearance]. */
private fun resolveChoice(
    urlTheme: String?,
    sessionToken: JSONObject?,
    linkStyle: JSONObject?,
): String =
    toFixedTheme(urlTheme)
        ?: toSessionTheme(sessionToken?.opt("th"))
        ?: DEVICE.takeIf { urlTheme == THEME_SYSTEM }
        ?: toFixedTheme(linkStyle?.opt("th"))
        ?: DEVICE.takeIf { linkStyle?.opt("th") == THEME_SYSTEM }
        ?: if (sessionToken != null) DEVICE else THEME_DEFAULT

@ColorInt
private fun fallbackColor(
    isDarkTheme: Boolean,
    isSessionLink: Boolean,
): Int =
    when {
        isSessionLink -> if (isDarkTheme) SESSION_DARK else SESSION_LIGHT
        else -> if (isDarkTheme) LEGACY_DARK else LEGACY_LIGHT
    }

// Marks a choice that is left to the device.
private const val DEVICE = "device"

/** `light` and `dark` as they are, null for anything else (`system` included). */
private fun toFixedTheme(value: Any?): String? = (value as? String)?.takeIf { it == THEME_LIGHT || it == THEME_DARK }

/** The theme a session token names; null for "match the device" or anything unknown. */
private fun toSessionTheme(value: Any?): String? =
    when (value) {
        "LINK_THEME_DARK" -> THEME_DARK
        "LINK_THEME_LIGHT" -> THEME_LIGHT
        else -> null
    }

/**
 * The session token's content (`token` param), checked the way Link checks it: a numeric `v` and
 * a non-empty string `c`. Null for a legacy link, which has no such token. The token is not
 * signed, so nothing in it is trusted.
 */
private fun readSessionToken(query: QueryString): JSONObject? {
    val token = decodeJsonParam(query.first("token")) ?: return null
    val client = token.opt("c")
    return token.takeIf { it.opt("v") is Number && client is String && client.isNotEmpty() }
}

/** An opaque color from a valid hex color, alpha dropped; null for anything else. */
@ColorInt
private fun toOpaqueColor(value: Any?): Int? {
    if (value !is String || !HEX_COLOR.matches(value)) return null
    val digits = value.drop(1)
    val full = if (digits.length <= SHORT_HEX_MAX) digits.map { "$it$it" }.joinToString("") else digits
    return OPAQUE or full.take(RGB_DIGITS).toInt(HEX_RADIX)
}

// A param holding base64 JSON (`token`, `link_style`). URL decoding turns its '+' into a space, so
// that's restored; the URL-safe alphabet and stripped padding are accepted too.
private fun decodeJsonParam(value: String?): JSONObject? {
    if (value.isNullOrEmpty()) return null
    return try {
        val base64 = value.replace(' ', '+').replace('-', '+').replace('_', '/')
        JSONObject(decodeBase64(base64 + "=".repeat((BASE64_BLOCK - base64.length % BASE64_BLOCK) % BASE64_BLOCK)))
    } catch (expected: Exception) {
        null
    }
}

/** Whether [color] is dark, so light system bar icons stay readable on it. */
internal fun isDarkColor(
    @ColorInt color: Int,
): Boolean {
    val red = (color shr RED_SHIFT) and BYTE_MASK
    val green = (color shr GREEN_SHIFT) and BYTE_MASK
    val blue = color and BYTE_MASK
    return (LUMA_RED * red + LUMA_GREEN * green + LUMA_BLUE * blue) / BYTE_MASK < HALF
}

internal fun Activity.isSystemThemeDark(): Boolean {
    val mask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mask == Configuration.UI_MODE_NIGHT_YES
}
