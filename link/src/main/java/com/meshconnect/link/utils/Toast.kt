package com.meshconnect.link.utils

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

internal fun Context.showToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

internal fun Context.showToast(@StringRes resId: Int) {
    Toast.makeText(this, resId, Toast.LENGTH_SHORT).show()
}
