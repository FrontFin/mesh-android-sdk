package com.meshconnect.link.utils

import java.net.URL

// Mesh Link domains. Only pages on these domains can use the JS bridge.
internal val linkDomains =
    listOf(
        "meshconnect.com",
        "meshpay.com",
    )

internal fun isLinkOrigin(url: String?): Boolean {
    // Use URL, not URI. URI rejects characters like { } | that Link URLs can contain.
    val parsed = url?.let { runCatching { URL(it) }.getOrNull() }
    val host = parsed?.takeIf { it.protocol.equals("https", ignoreCase = true) }?.host?.lowercase()
    return host != null && linkDomains.any { host == it || host.endsWith(".$it") }
}
