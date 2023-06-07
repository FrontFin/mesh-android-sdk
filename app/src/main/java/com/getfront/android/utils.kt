package com.getfront.android

import android.util.Log
import com.getfront.catalog.entity.FrontAccount

fun toString(accounts: List<FrontAccount>): String {
    val builder = StringBuilder()
    for (a in accounts) {
        builder.apply {
            add("brokerName", a.brokerName)
            add("brokerType", a.brokerType)
            add("accountId", a.accountId)
            add("accountName", a.accountName)
            add("accessToken", a.accessToken)
            add("refreshToken", a.refreshToken)
            appendLine()
        }
    }
    return builder.toString()
}

fun StringBuilder.add(key: String, value: String?) {
    if (value != null) appendLine("$key: $value")
}

fun log(obj: Any?, TAG: String = "FrontCatalog") {
    Log.d(TAG, obj.toString())
}
