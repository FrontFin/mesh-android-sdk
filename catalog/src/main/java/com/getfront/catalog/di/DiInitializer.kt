package com.getfront.catalog.di

import android.content.Context
import androidx.startup.Initializer

internal val di get() = requireNotNull(DiInitializer.diInstanse)

internal class DiInitializer : Initializer<Di> {

    companion object {
        internal var diInstanse: Di? = null
    }

    override fun create(context: Context): Di {
        return DiImpl().also { diInstanse = it }
    }

    override fun dependencies(): List<Class<out Initializer<*>>> {
        return emptyList()
    }
}
