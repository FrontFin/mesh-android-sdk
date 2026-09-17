package com.meshconnect.link.utils

internal val whitelistedOrigins =
    listOf(
        ".meshconnect.com",
        ".walletconnect.com",
        ".walletconnect.org",
        ".walletlink.org",
        ".coinbase.com",
        ".okx.com",
        ".gemini.com",
        ".hcaptcha.com",
        ".robinhood.com",
        ".google.com",
        "https://robinhood.com",
        "https://m.stripe.network",
        "https://js.stripe.com",
        "https://app.usercentrics.eu",
        "https://api.cb-device-intelligence.com",
        "https://contentmx.okcoin.com",
        "https://www.recaptcha.net",
    )

// Hosts that match a whitelisted origin above by suffix but belong to a
// different product, so they get opened externally instead (expected).
// web3.okx.com is OKX Wallet, not OKX Exchange (why ".okx.com" is whitelisted).
internal val whitelistedOriginExceptions =
    listOf(
        "web3.okx.com",
    )

// A bare `url.startsWith(prefix)` treats "https://robinhood.com.evil.com" as a
// match for "https://robinhood.com" - the string does start with it, nothing
// requires what follows to actually be a URL boundary. Require the next
// character (if any) to be one that only ever starts a path, query, fragment,
// or port, so an attacker-controlled suffix can no longer spoof a whitelisted
// full-URL entry.
private fun matchesUrlPrefix(
    url: String,
    prefix: String,
): Boolean {
    if (!url.startsWith(prefix)) return false
    val boundary = url.getOrNull(prefix.length)
    return boundary == null || boundary in "/?#:"
}

internal fun isUrlWhitelisted(
    url: String,
    host: String,
): Boolean {
    if (whitelistedOriginExceptions.any { it.equals(host, ignoreCase = true) }) return false
    return whitelistedOrigins.find { matchesUrlPrefix(url, it) || host.endsWith(it, ignoreCase = true) } != null
}
