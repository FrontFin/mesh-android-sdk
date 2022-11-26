package com.getfront.catalog.store

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.getfront.catalog.entity.FrontAccount
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow

@Suppress("unused")
fun createPreferenceAccountStore(context: Context): FrontAccountStore =
    PreferenceAccountStore(context)

internal class PreferenceAccountStore(context: Context) : FrontAccountStore {

    private val gson by lazy { Gson() }
    private val preferences by lazy {
        EncryptedSharedPreferences.create(
            "front_accounts_shared_prefs",
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    private val key = "accounts"

    @SuppressLint("ApplySharedPref")
    override suspend fun insert(accounts: List<FrontAccount>) {
        preferences.edit()
            .putString(key, gson.toJson(getAll() + accounts))
            .commit()
    }

    override suspend fun getAll(): List<FrontAccount> {
        return preferences.accounts
    }

    private val SharedPreferences.accounts: List<FrontAccount>
        get() {
            val json = getString(key, "")
            return if (json.isNullOrEmpty()) emptyList()
            else gson.fromJson(json, object : TypeToken<List<FrontAccount>>() {}.type)
        }

    override suspend fun getById(id: String): FrontAccount? {
        return getAll().find { it.id == id }
    }

    override suspend fun accounts(): Flow<List<FrontAccount>> {
        return preferenceFlow(preferences, key) { accounts }
    }

    override suspend fun count(): Int {
        return getAll().size
    }

    override suspend fun remove(id: String) {
        insert(getAll().filter { it.id != id })
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun clear() {
        preferences.edit().putString(key, "").commit()
    }
}
