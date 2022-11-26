package com.getfront.catalog.entity

internal sealed class CatalogResponse {
    object Done : CatalogResponse()
    object Close : CatalogResponse()
    object Loaded : CatalogResponse()

    internal data class Connected(
        val accounts: List<FrontAccount>
    ) : CatalogResponse()

    internal data class Title(
        val title: String?,
        val hideBackButton: Boolean?,
        val hideTitle: Boolean?
    ) : CatalogResponse()
}
