package com.meshconnect.android

import android.app.Activity
import android.content.Intent
import android.os.Bundle

class DeepLinkEntryActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Check if app is already running
        packageManager.getLaunchIntentForPackage(packageName)?.run {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(this)
        }
        finish()
    }
}