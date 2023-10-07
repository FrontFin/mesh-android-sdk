package com.meshconnect.link.utils

import android.view.View

internal infix fun View.onClick(listener: (View) -> Unit) {
    setOnClickListener(listener)
}
