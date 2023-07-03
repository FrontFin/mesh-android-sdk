package com.getfront.catalog.ui

import android.os.Parcelable
import com.getfront.catalog.entity.FrontPayload
import kotlinx.parcelize.Parcelize

sealed interface FrontCatalogResult : Parcelable {
    @Parcelize
    data class Success(val payloads: List<FrontPayload>) : FrontCatalogResult
    @Parcelize
    data class Cancelled(val error: Throwable? = null) : FrontCatalogResult
}
