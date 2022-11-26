package com.getfront.catalog.di

import com.getfront.catalog.converter.JsonConverter
import com.getfront.catalog.usecase.ParseCatalogJsonUseCase
import kotlinx.coroutines.Dispatchers

internal class DiImpl : Di {

    private val dispatcher = Dispatchers.IO

    override val parseCatalogJsonUseCase
        get() = ParseCatalogJsonUseCase(dispatcher, JsonConverter)
}
