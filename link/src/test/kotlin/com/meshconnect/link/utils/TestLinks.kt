package com.meshconnect.link.utils

import java.net.URLEncoder
import java.util.Base64

// Links the way Link gets them, shared by the URL and theme tests.

internal fun base64Url(json: String): String = Base64.getUrlEncoder().withoutPadding().encodeToString(json.toByteArray())

/** A link with a session token. [envelope] is the token's JSON. */
internal fun sessionUrl(
    envelope: String,
    extra: String = "",
): String = "https://link.meshpay.com/?token=${base64Url(envelope)}&flow=%7B%7D$extra"

/** The `link_style` param for a client style JSON (standard base64, URL encoded). */
internal fun linkStyleParam(style: String): String =
    "&link_style=" + URLEncoder.encode(Base64.getEncoder().encodeToString(style.toByteArray()), "UTF-8")

/** A legacy link, with the client's style in link_style. */
internal fun legacyUrl(
    style: String? = null,
    extra: String = "",
): String = "https://link.meshconnect.com/?auth_code=x${style?.let(::linkStyleParam).orEmpty()}$extra"
