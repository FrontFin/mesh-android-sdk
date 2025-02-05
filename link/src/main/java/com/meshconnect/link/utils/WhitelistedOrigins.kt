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
        "https://ramp.revolut.codes",
        "https://sso.revolut.codes",
        "https://ramp.revolut.com",
    )

internal fun isUrlWhitelisted(
    url: String,
    host: String,
): Boolean {
    return whitelistedOrigins.find { url.startsWith(it) || host.endsWith(it) } != null
}
