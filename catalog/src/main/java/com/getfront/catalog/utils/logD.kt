package com.getfront.catalog.utils

import android.util.Log
import com.getfront.catalog.BuildConfig

internal fun logD(any: Any?, tag: String = "frontLog") {
    if (BuildConfig.DEBUG) Log.d(tag, any.toString())
}
