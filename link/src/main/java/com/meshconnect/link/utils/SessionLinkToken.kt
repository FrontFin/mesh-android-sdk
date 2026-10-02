package com.meshconnect.link.utils

import com.meshconnect.link.entity.MeshLinkEnvironment
import java.net.URLEncoder

/**
 * The Link URL for an MFS session token.
 *
 * Kept separate from [sessionLinkToken] so the URL shape can be asserted in a
 * plain JVM test, without mocking the base64 step.
 *
 * The token is percent-encoded rather than interpolated: one carrying a reserved
 * character (`&`, `#`, `%`) would otherwise truncate the URL and open Link with
 * no token at all. `URLEncoder` is form encoding, so its `+` for space is
 * corrected back to `%20`.
 */
internal fun sessionLinkUrl(
    sessionToken: String,
    environment: MeshLinkEnvironment,
): String {
    val encoded = URLEncoder.encode(sessionToken, "UTF-8").replace("+", "%20")
    return "${environment.linkUrl}/?token=$encoded"
}

/**
 * Builds a link token from an MFS session token.
 *
 * `POST /v2/sessions` returns a bare session token rather than a link token, and
 * a bare token carries no host for the WebView to load. This wraps it into the
 * same base64-of-a-URL shape the SDK already takes, so the MFS-native and legacy
 * paths converge on one code path.
 */
internal fun sessionLinkToken(
    sessionToken: String,
    environment: MeshLinkEnvironment,
): String {
    require(sessionToken.isNotEmpty()) { "A session token is required" }
    return encodeBase64(sessionLinkUrl(sessionToken, environment))
}
