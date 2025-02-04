package com.meshconnect.link.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner

internal inline fun <reified VM : ViewModel> ViewModelStoreOwner.viewModel(factory: ViewModelProvider.Factory) =
    lazy(LazyThreadSafetyMode.NONE) {
        ViewModelProvider(this, factory)[VM::class.java]
    }
