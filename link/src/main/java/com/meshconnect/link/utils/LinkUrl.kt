package com.meshconnect.link.utils

import androidx.annotation.ColorInt
import java.net.URL

private const val PLATFORM = "android"
private val APP_THEMES = setOf(THEME_LIGHT, THEME_DARK, THEME_SYSTEM)

/** The app's raw settings for the Link URL; null means not set. */
internal class LinkUrlOptions(
    val theme: String?,
    val language: String?,
    val fiatCurrency: String?,
)

/** The URL to open, whether it opens dark, and the color around it. */
internal data class LinkUrl(
    val url: URL,
    val isDarkTheme: Boolean,
    @ColorInt val backgroundColor: Int,
)

/**
 * Builds the Link URL in one pass, the same way as the other mobile SDKs: the params the app set
 * (`th`, `lng`, `fiatCur`, plus `platform`) replace any already in the token URL, and the rest of
 * the URL stays byte for byte. `th` is written when the app sets a theme (`system` is sent as
 * `system`, so Link decides), and, on a legacy link, when the device decides: then the resolved
 * theme is written, so Link and the bars read the device the same way. Also returns the theme and
 * color Link opens with; see [resolveLinkAppearance].
 */
internal fun buildLinkURL(
    linkUrl: String,
    options: LinkUrlOptions,
    isSystemThemeDark: Boolean,
): LinkUrl {
    val query = QueryString.parse(linkUrl)
    val appTheme = options.theme?.takeIf { it in APP_THEMES }
    val appearance = resolveLinkAppearance(query, appTheme, isSystemThemeDark)
    val params =
        buildMap {
            (appearance.themeToWrite ?: appTheme)?.let { put("th", it) }
            resolveLanguage(options.language)?.let { put("lng", it) }
            resolveCurrency(options.fiatCurrency)?.let { put("fiatCur", it) }
            put("platform", PLATFORM)
        }
    return LinkUrl(URL(query.withParams(params)), appearance.isDarkTheme, appearance.backgroundColor)
}
