package com.getfront.catalog.store

import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.FrontAccount

fun getAccountsFromPayload(payload: AccessTokenPayload): List<FrontAccount> {
    return payload.accountTokens.map {
        FrontAccount(
            accessToken = it.accessToken,
            refreshToken = it.refreshToken,
            accountId = it.account.accountId,
            accountName = it.account.accountName,
            brokerType = payload.brokerType,
            brokerName = payload.brokerName
        )
    }
}
