package com.getfront.catalog.di

import com.getfront.catalog.converter.JsonConverter
import com.getfront.catalog.usecase.GetLinkEventUseCase
import kotlinx.coroutines.Dispatchers

internal class DiImpl : Di {

    private val dispatcher = Dispatchers.IO

    override val getLinkEventUseCase
        get() = GetLinkEventUseCase(dispatcher, JsonConverter)
}
