package com.meshconnect.link.utils

import java.net.URL
import java.net.URLDecoder
import java.net.URLEncoder

private const val PLATFORM = "android"

/** The app's raw settings for the Link URL; null means not set. */
internal class LinkUrlOptions(
    val theme: String?,
    val language: String?,
    val fiatCurrency: String?,
)

/** The URL to open and the theme it opens in, so the native bars can match it. */
internal data class LinkUrl(
    val url: URL,
    val isDarkTheme: Boolean,
)

/**
 * Builds the Link URL in one pass: parses the token URL once, replaces the params the app set
 * (`th`, `lng`, `fiatCur`, plus `platform`) and keeps every other byte of the URL as is. Also
 * returns the theme Link opens in, so the native bars match it; see [resolveLinkTheme].
 */
internal fun buildLinkURL(
    linkUrl: String,
    options: LinkUrlOptions,
    isSystemThemeDark: () -> Boolean,
): LinkUrl {
    val query = QueryString.parse(linkUrl)
    val theme = resolveLinkTheme(options.theme, query.first("th"), query.first("link_style"), isSystemThemeDark)
    val params =
        buildMap {
            // Only the app's theme is written; otherwise Link reads the token's th or link_style itself.
            resolveTheme(options.theme, isSystemThemeDark)?.let { put("th", it) }
            resolveLanguage(options.language)?.let { put("lng", it) }
            resolveCurrency(options.fiatCurrency)?.let { put("fiatCur", it) }
            put("platform", PLATFORM)
        }
    return LinkUrl(URL(query.withParams(params)), theme == THEME_DARK)
}

/**
 * A URL split into base, query pairs and fragment, without re-encoding anything, so the
 * token and the other params survive byte for byte.
 */
private class QueryString(
    private val base: String,
    private val pairs: List<Pair<String, String>>,
    private val fragment: String,
) {
    /** First value of [name] (Link reads the first one), or null when it's missing. */
    fun first(name: String): String? {
        val raw = pairs.firstOrNull { it.second == name }?.first ?: return null
        return if (raw.contains('=')) decode(raw.substringAfter('=')) else ""
    }

    /** The URL with [params] replacing any existing values, added before the fragment. */
    fun withParams(params: Map<String, String>): String {
        val kept = pairs.filter { it.second !in params }.map { it.first }
        val added = params.map { (key, value) -> "$key=${URLEncoder.encode(value, "UTF-8")}" }
        return base + "?" + (kept + added).joinToString("&") + fragment
    }

    companion object {
        fun parse(url: String): QueryString {
            val hash = url.indexOf('#')
            val beforeFragment = if (hash == -1) url else url.substring(0, hash)
            val fragment = if (hash == -1) "" else url.substring(hash)
            val queryStart = beforeFragment.indexOf('?')
            if (queryStart == -1) return QueryString(beforeFragment, emptyList(), fragment)
            // Each raw pair with its decoded key, decoded once. Empty parts are kept.
            val pairs =
                beforeFragment.substring(queryStart + 1).split('&').map { raw ->
                    raw to if (raw.isEmpty()) "" else decode(raw.substringBefore('='))
                }
            return QueryString(beforeFragment.substring(0, queryStart), pairs, fragment)
        }

        private fun decode(value: String): String =
            try {
                URLDecoder.decode(value, "UTF-8")
            } catch (expected: IllegalArgumentException) {
                // A malformed escape like %zz is kept as is.
                value.replace('+', ' ')
            }
    }
}
