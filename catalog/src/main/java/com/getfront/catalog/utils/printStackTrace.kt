package com.getfront.catalog.utils

import com.getfront.catalog.BuildConfig

internal fun printStackTrace(e: Throwable) {
    if (BuildConfig.DEBUG) e.printStackTrace()
}
