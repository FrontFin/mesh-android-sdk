package com.meshconnect.link.utils

import com.meshconnect.link.BuildConfig

internal fun printStackTrace(e: Throwable) {
    if (BuildConfig.DEBUG) e.printStackTrace()
}
