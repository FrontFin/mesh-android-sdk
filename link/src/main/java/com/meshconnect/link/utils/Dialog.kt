package com.meshconnect.link.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal fun Context.alertDialog(block: MaterialAlertDialogBuilder.() -> Unit) {
    block(MaterialAlertDialogBuilder(this))
}
