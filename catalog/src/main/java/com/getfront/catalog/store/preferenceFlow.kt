package com.getfront.catalog.store

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

internal fun <T> preferenceFlow(
    preferences: SharedPreferences,
    key: String,
    preload: Boolean = true,
    obtain: SharedPreferences.() -> T
) = callbackFlow {

    fun send(prefs: SharedPreferences) = trySend(obtain(prefs))

    val listener = SharedPreferences.OnSharedPreferenceChangeListener { _preferences, _key ->
        if (key == _key) send(_preferences)
    }

    if (preload) send(preferences)

    preferences.registerOnSharedPreferenceChangeListener(listener)
    awaitClose { preferences.unregisterOnSharedPreferenceChangeListener(listener) }
}
