package com.meshconnect.link.utils

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Parcelable

internal inline fun <reified T : Parcelable> Intent.getParcelable(key: String): T? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(key, T::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(key)
    }

internal inline fun <reified T> intent(context: Context): Intent {
    return Intent(context, T::class.java)
}
