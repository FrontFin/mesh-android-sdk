package com.meshconnect.link.utils

/** The app's display currency, or null when it's not set. */
internal fun resolveCurrency(currency: String?): String? = currency?.trim()?.takeIf { it.isNotEmpty() }
