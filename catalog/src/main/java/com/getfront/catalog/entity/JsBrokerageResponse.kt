package com.getfront.catalog.entity

internal data class JsBrokerageResponse(
    val type: Type?,
    val errorMessage: String?,
    val expiresInSeconds: Int,
    val refreshTokenExpiresInSeconds: Int?,
    val title: String?,
    val hideBackButton: Boolean?,
    val hideTitle: Boolean?,
    val payload: Payload?,
) {
    @Suppress("EnumEntryName")
    internal enum class Type {
        brokerageAccountAccessToken,
        delayedAuthentication,
        loaded,
        error,
        close,
        done,
        setTitle,
        tradingBrokerConnected,
    }

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
}
