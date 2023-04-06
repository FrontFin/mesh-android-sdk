package com.getfront.catalog.entity

internal sealed interface CatalogEvent {
    object Done : CatalogEvent
    object Close : CatalogEvent
    object ShowClose : CatalogEvent
    object Undefined : CatalogEvent
    data class Connected(val accounts: List<FrontAccount>) : CatalogEvent
}
