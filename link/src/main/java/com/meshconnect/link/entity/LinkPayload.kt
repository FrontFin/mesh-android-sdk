package com.meshconnect.link.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed interface LinkPayload : Parcelable

sealed interface TransferFinishedPayload : LinkPayload

/**
 * AccessTokenPayload
 */
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
    val isReconnected: Boolean?,
) : Parcelable

@Parcelize
data class BrandInfo(
    val brokerLogo: String?,
    val brokerPrimaryColor: String?,
) : Parcelable

/**
 * DelayedAuthPayload
 */
@Parcelize
data class DelayedAuthPayload(
    val refreshTokenExpiresInSeconds: Int?,
    val brokerType: String,
    val refreshToken: String,
    val brokerName: String,
    val brokerBrandInfo: BrandInfo,
) : LinkPayload

/**
 * TransferFinishedPayload
 */
@Parcelize
data class TransferFinishedSuccessPayload(
    val txId: String,
    val fromAddress: String,
    val toAddress: String,
    val symbol: String,
    val amount: Double,
    val networkId: String,
    val amountInFiat: Double?,
    val totalAmountInFiat: Double?,
    val networkName: String?,
    val txHash: String?,
    val transferId: String?,
    val refundAddress: String?,
) : TransferFinishedPayload

@Parcelize
data class TransferFinishedErrorPayload(
    val errorMessage: String,
) : TransferFinishedPayload
