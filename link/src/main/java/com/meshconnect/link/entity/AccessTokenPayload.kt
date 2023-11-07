package com.meshconnect.link.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class AccessTokenResponse(
    val payload: AccessTokenPayload
)

@Parcelize
data class AccessTokenPayload(
    val accountTokens: List<AccountToken>,
    val brokerType: String,
    val brokerName: String,
    val brokerBrandInfo: BrandInfo,
    val expiresInSeconds: Int?,
    val refreshTokenExpiresInSeconds: Int?,
) : LinkPayload

@Parcelize
data class AccountToken(
    val account: Account,
    val accessToken: String,
    val refreshToken: String?,
) : Parcelable

@Parcelize
data class Account(
    val accountId: String,
    val accountName: String,
    val frontAccountId: String,
    val fund: Double?,
    val cash: Double?,
    val isReconnected: Boolean?
) : Parcelable

@Parcelize
data class BrandInfo(
    val brokerLogo: String?,
    val brokerPrimaryColor: String?
) : Parcelable
