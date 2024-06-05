package com.meshconnect.link.store

import android.content.Context
import com.meshconnect.link.entity.AccessTokenPayload

fun createPreferenceAccountStore(context: Context): MeshAccountStore =
    PreferenceAccountStore(context)

fun getAccountsFromPayload(payload: AccessTokenPayload): List<MeshAccount> {
    return payload.accountTokens.map {
        MeshAccount(
            accessToken = it.accessToken,
            refreshToken = it.refreshToken,
            accountId = it.account.accountId,
            accountName = it.account.accountName,
            brokerType = payload.brokerType,
            brokerName = payload.brokerName
        )
    }
}
