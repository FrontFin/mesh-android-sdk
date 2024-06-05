package com.meshconnect.link.store

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

private const val FILE_NAME = "front_accounts_shared_prefs"

internal fun createPreferencesSafe(context: Context): SharedPreferences {
    return try {
        createPreferences(context)
    } catch (e: Exception) {
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
