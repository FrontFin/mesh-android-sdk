package com.meshconnect.link.utils

import java.util.Locale

internal fun resolveLanguage(language: String?): String? {
    return if (language == "system") systemLanguage else language
}

internal val systemLanguage: String get() = Locale.getDefault().toLanguageTag()
