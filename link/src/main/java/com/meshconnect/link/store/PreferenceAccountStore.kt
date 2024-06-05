package com.meshconnect.link.store

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlin.LazyThreadSafetyMode.NONE

internal class PreferenceAccountStore(context: Context) : MeshAccountStore {
    private val gson by lazy(NONE) { Gson() }
    private val preferences by lazy(NONE) { createPreferencesSafe(context) }
    private val key = "accounts"

    @SuppressLint("ApplySharedPref")
    override suspend fun insert(accounts: List<MeshAccount>) {
        preferences.edit()
            .putString(key, gson.toJson(getAll() + accounts))
            .commit()
    }

    override suspend fun getAll(): List<MeshAccount> {
        return preferences.accounts
    }

    private val SharedPreferences.accounts: List<MeshAccount>
        get() {
            val json = getString(key, "")
            return if (json.isNullOrEmpty()) emptyList()
            else gson.fromJson(json, object : TypeToken<List<MeshAccount>>() {}.type)
        }

    override suspend fun getById(id: String): MeshAccount? {
        return getAll().find { it.id == id }
    }

    override fun accounts(): Flow<List<MeshAccount>> {
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
