package com.meshconnect.link.entity

internal data class AccessTokenResponse(
    val payload: AccessTokenPayload
)

internal data class DelayedAuthResponse(
    val payload: DelayedAuthPayload
)

internal data class TransferFinishedResponse(
    val payload: TransferFinishedPayload
)
