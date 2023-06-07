package com.getfront.catalog.utils

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder

internal fun Context.alertDialog(block: MaterialAlertDialogBuilder.() -> Unit) {
    block(MaterialAlertDialogBuilder(this))
}
