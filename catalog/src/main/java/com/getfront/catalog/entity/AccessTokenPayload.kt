package com.getfront.catalog.entity

data class AccessTokenResponse(
    val payload: AccessTokenPayload
)

data class AccessTokenPayload(
    val accountTokens: List<AccountToken>,
    val brokerType: String,
    val brokerName: String,
    val brokerBrandInfo: BrandInfo,
    val expiresInSeconds: Int?,
    val refreshTokenExpiresInSeconds: Int?,
) : FrontPayload

data class AccountToken(
    val account: Account,
    val accessToken: String,
    val refreshToken: String? = null,
)

data class Account(
    val accountId: String,
    val accountName: String,
    val fund: Double?,
    val cash: Double?,
)

data class BrandInfo(
    val brokerLogo: String? = null,
    val brokerPrimaryColor: String? = null
)
