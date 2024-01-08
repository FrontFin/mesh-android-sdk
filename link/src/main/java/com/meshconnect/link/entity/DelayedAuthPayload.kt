package com.meshconnect.link.entity

import kotlinx.parcelize.Parcelize

@Parcelize
data class DelayedAuthPayload(
    val refreshTokenExpiresInSeconds: Int?,
    val brokerType: String,
    val refreshToken: String,
    val brokerName: String,
    val brokerBrandInfo: BrandInfo
) : LinkPayload

internal data class DelayedAuthResponse(
    val payload: DelayedAuthPayload
)
