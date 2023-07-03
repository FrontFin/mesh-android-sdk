package com.getfront.catalog.entity

import kotlinx.parcelize.Parcelize

sealed interface TransferFinishedPayload : FrontPayload

internal data class TransferFinishedResponse(
    val payload: TransferFinishedPayload
)

@Parcelize
data class TransferFinishedSuccessPayload(
    val txId: String,
    val fromAddress: String,
    val toAddress: String,
    val symbol: String,
    val amount: Double,
    val networkId: String,
) : TransferFinishedPayload

@Parcelize
data class TransferFinishedErrorPayload(
    val errorMessage: String
) : TransferFinishedPayload
