package com.getfront.catalog.utils

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.os.BuildCompat

@Suppress("DEPRECATION", "UnsafeOptInUsageError")
internal inline fun <reified T : Parcelable> Intent.getParcelableExtraCompat(
    key: String
): T? {
    return if (BuildCompat.isAtLeastT()) {
        getParcelableExtra(key, T::class.java)
    } else {
        getParcelableExtra(key)
    }
}

internal inline fun <reified T> intent(context: Context): Intent {
    return Intent(context, T::class.java)
}
