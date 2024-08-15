package com.meshconnect.link.utils

import android.app.Activity
import android.content.res.Configuration
import androidx.core.view.WindowInsetsControllerCompat

internal fun Activity.windowInsetsController(
    controller: WindowInsetsControllerCompat.() -> Unit
) {
    controller(WindowInsetsControllerCompat(window, window.decorView))
}

internal fun Activity.isSystemDarkTheme(): Boolean {
    val mask = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    return mask == Configuration.UI_MODE_NIGHT_YES
}