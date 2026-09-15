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

// Hosts that match a whitelisted origin above by suffix but are actually a
// different product than the one that entry is for, so they should NOT be
// treated as whitelisted. web3.okx.com is OKX Wallet's connect/deep-link
// host, not OKX Exchange (why ".okx.com" is whitelisted in the first place).
internal val whitelistedOriginExceptions =
    listOf(
        "web3.okx.com",
    )

internal fun isUrlWhitelisted(
    url: String,
    host: String,
): Boolean {
    if (whitelistedOriginExceptions.contains(host)) return false
    return whitelistedOrigins.find { url.startsWith(it) || host.endsWith(it) } != null
}
