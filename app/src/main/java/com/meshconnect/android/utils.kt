package com.meshconnect.android

import android.util.Log
import com.meshconnect.link.store.MeshAccount

fun toString(accounts: List<MeshAccount>): String {
    val builder = StringBuilder()
    for (a in accounts) {
        builder.apply {
            add("brokerName", a.brokerName)
            add("brokerType", a.brokerType)
            add("accountId", a.accountId)
            add("accountName", a.accountName)
            add("accessToken", a.accessToken.take(n = 100))
            add("refreshToken", a.refreshToken)
            appendLine()
        }
    }
    return builder.toString()
}

fun StringBuilder.add(key: String, value: String?) {
    if (value != null) appendLine("$key: $value")
}

fun logD(obj: Any?, tag: String = "meshLog") {
    Log.d(tag, obj.toString())
}
