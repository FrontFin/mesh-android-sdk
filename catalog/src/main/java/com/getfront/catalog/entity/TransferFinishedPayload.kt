package com.getfront.catalog.entity

sealed interface TransferFinishedPayload : FrontPayload

internal data class TransferFinishedResponse(
    val payload: TransferFinishedPayload
)

data class TransferFinishedSuccessPayload(
    val txId: String,
    val fromAddress: String,
    val toAddress: String,
    val symbol: String,
    val amount: Double,
    val networkId: String,
) : TransferFinishedPayload

data class TransferFinishedErrorPayload(
    val errorMessage: String
) : TransferFinishedPayload
