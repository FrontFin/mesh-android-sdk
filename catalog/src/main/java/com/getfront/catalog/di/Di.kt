package com.getfront.catalog.di

import com.getfront.catalog.usecase.ParseCatalogJsonUseCase

internal interface Di {

    val parseCatalogJsonUseCase: ParseCatalogJsonUseCase
}
