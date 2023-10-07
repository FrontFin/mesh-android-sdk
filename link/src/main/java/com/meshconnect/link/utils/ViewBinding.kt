package com.meshconnect.link.utils

import android.app.Activity
import android.view.LayoutInflater
import androidx.viewbinding.ViewBinding

internal inline fun <T : ViewBinding> Activity.viewBinding(
    crossinline inflate: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) { inflate(layoutInflater) }
