package com.getfront.catalog.entity

internal data class JsType(
    val type: Type?
)

@Suppress("EnumEntryName")
internal enum class Type {
    brokerageAccountAccessToken,
    error,
    close,
    done,
    showClose,
}

internal data class JsError(
    val errorMessage: String?
)

internal data class JsAccessTokens(
    val payload: Payload?
)

internal data class Payload(
    val accountTokens: List<AccountToken>,
    val brokerType: String,
    val brokerName: String,
    val brokerBrandInfo: BrandInfo?,
    val expiresInSeconds: Int,
    val refreshTokenExpiresInSeconds: Int?,
)

internal data class AccountToken(
    val account: Account,
    val accessToken: String,
    val refreshToken: String? = null,
)

internal data class Account(
    val accountId: String,
    val accountName: String,
    val fund: Double?,
    val cash: Double?,
)

internal data class BrandInfo(
    val brokerLogo: String? = null,
    val brokerPrimaryColor: String? = null
)
