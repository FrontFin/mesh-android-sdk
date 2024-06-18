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

    private fun toJson(obj: Any) = gson.toJson(obj)

    private fun getAccounts(preferences: SharedPreferences): List<MeshAccount> {
        val json = preferences.getString(key, "")
        return if (json.isNullOrEmpty()) emptyList()
        else gson.fromJson(json, object : TypeToken<List<MeshAccount>>() {}.type)
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun insert(account: MeshAccount) {
        preferences.edit()
            .putString(key, toJson(getAll() + account))
            .commit()
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun insert(accounts: List<MeshAccount>) {
        preferences.edit()
            .putString(key, toJson(getAll() + accounts))
            .commit()
    }

    override fun accounts(): Flow<List<MeshAccount>> {
        return preferenceFlow(preferences, key) { getAccounts(this) }
    }

    override suspend fun getAll(): List<MeshAccount> {
        return getAccounts(preferences)
    }

    override suspend fun getById(id: String): MeshAccount? {
        return getAll().find { it.id == id }
    }

    override suspend fun count(): Int {
        return getAll().size
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun remove(id: String) {
        preferences.edit()
            .putString(key, toJson(getAll().filter { it.id != id }))
            .commit()
    }

    @SuppressLint("ApplySharedPref")
    override suspend fun clear() {
        preferences.edit().clear().commit()
    }
}
