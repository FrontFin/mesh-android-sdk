@file:Suppress("unused")

package com.meshconnect.android

import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.Account
import com.meshconnect.link.entity.AccountToken
import com.meshconnect.link.entity.BrandInfo
import com.meshconnect.link.store.MeshAccount

val debugAccessTokenPayload get() = AccessTokenPayload(
    accountTokens = listOf(
        AccountToken(
            account = Account(
                accountId = "utG3yLzgq8njpYdM5EeeQXn",
                accountName = "awesome-account",
                frontAccountId = "yLzgq8YdM5EeeQXn",
                cash = 100.0,
                fund = 1000.0,
                isReconnected = false
            ),
            accessToken = "G3jpYdM5EeeQXn",
            refreshToken = "utQXn",
        )
    ),
    brokerType = "test-type",
    brokerName = "Test name",
    brokerBrandInfo = BrandInfo("", ""),
    expiresInSeconds = null,
    refreshTokenExpiresInSeconds = null
)

val debugMeshAccount get() = MeshAccount(
    accountId = "552e9c9f-test-5143-9f72-ab3d396word4",
    brokerType = "coinbase",
    brokerName = "Coinbase",
    accessToken = "<coinbase_access_token>",
    refreshToken = "<coinbase_refresh_token>",
    accountName = "Awesome Mesh Test Account",
)
