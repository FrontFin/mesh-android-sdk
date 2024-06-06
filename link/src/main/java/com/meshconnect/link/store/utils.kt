package com.meshconnect.link.store

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.meshconnect.link.entity.AccessTokenPayload
import java.io.IOException

private const val FILE_NAME = "front_accounts_shared_prefs"

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

internal fun createPreferencesSafe(context: Context): SharedPreferences {
    return try {
        createPreferences(context)
    } catch (_: IOException) {
        clearPreferences(context)
        createPreferences(context)
    }
}

internal fun createPreferences(context: Context): SharedPreferences {
    return EncryptedSharedPreferences.create(
        FILE_NAME,
        MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

@SuppressLint("ApplySharedPref")
private fun clearPreferences(context: Context) {
    context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE).edit().clear().commit()
}
