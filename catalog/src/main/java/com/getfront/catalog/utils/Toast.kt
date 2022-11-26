package com.getfront.catalog.utils

import android.content.Context
import android.widget.Toast

internal fun Context.showToast(string: String) =
    Toast.makeText(this, string, Toast.LENGTH_SHORT).show()
