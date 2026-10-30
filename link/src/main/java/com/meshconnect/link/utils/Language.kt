package com.meshconnect.link.utils

import java.util.Locale

/** The app's language as a BCP-47 tag, `system` read from the device; null when not set. */
internal fun resolveLanguage(language: String?): String? =
    when (val trimmed = language?.trim()) {
        null, "" -> null
        "system" -> systemLanguage
        else -> trimmed
    }

internal val systemLanguage: String get() = Locale.getDefault().toLanguageTag()
