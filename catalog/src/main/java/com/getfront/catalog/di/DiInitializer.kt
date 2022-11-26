package com.getfront.catalog.di

import android.content.Context
import androidx.startup.Initializer

internal object DiInitializer : Initializer<Di> {

    lateinit var instance: Di; private set

    override fun create(context: Context): Di {
        return DiImpl().also { instance = it }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
