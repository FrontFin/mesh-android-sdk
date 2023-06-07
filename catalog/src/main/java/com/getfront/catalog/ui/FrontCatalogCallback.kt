package com.getfront.catalog.ui

import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.TransferFinishedPayload

interface FrontCatalogCallback {
    fun onExit() = Unit
    fun onBrokerConnected(payload: AccessTokenPayload) = Unit
    fun onTransferFinished(payload: TransferFinishedPayload) = Unit
}
