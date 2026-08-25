package com.meshconnect.link.utils

internal val whitelistedOrigins =
    listOf(
        ".meshconnect.com",
        // MFS / Link v3. A link token is base64 of a URL, so a migrated client
        // is handed a link.meshpay.com URL by the same v1 endpoint and the
        // WebView must be allowed to load it.
        ".meshpay.com",
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

internal fun isUrlWhitelisted(
    url: String,
    host: String,
): Boolean {
    return whitelistedOrigins.find { url.startsWith(it) || host.endsWith(it) } != null
}
