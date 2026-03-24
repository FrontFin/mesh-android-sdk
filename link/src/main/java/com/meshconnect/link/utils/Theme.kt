package com.meshconnect.link.utils

import android.app.Activity
import android.content.res.Configuration
import com.meshconnect.link.entity.LinkTheme

internal fun resolveTheme(
    theme: String?,
    isSystemThemeDark: () -> Boolean,
): String? {
    val themeEnum = theme?.let { LinkTheme.entries.find { it.name == theme } }
    return when (themeEnum) {
        LinkTheme.LIGHT -> "light"
        LinkTheme.DARK -> "dark"
        LinkTheme.SYSTEM -> if (isSystemThemeDark()) "dark" else "light"
        else -> null
    }
}

internal fun Activity.isSystemThemeDark(): Boolean {
    val mask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mask == Configuration.UI_MODE_NIGHT_YES
}
