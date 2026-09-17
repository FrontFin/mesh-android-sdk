package com.meshconnect.android

import android.app.Activity
import android.app.ActivityManager
import android.os.Bundle

/**
 * Trampoline that brings the app's existing task back to the foreground and
 * resumes whatever was on top — typically LinkActivity, which sits above
 * LinkExampleActivity in the task — without recreating any activity, then
 * finishes so the resumed activity shows through. Starting LinkExampleActivity
 * via a launcher intent is deliberately avoided: that intent carries
 * FLAG_ACTIVITY_RESET_TASK_IF_NEEDED, which resets the task to its root and
 * destroys LinkActivity.
 */
class DeepLinkActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveAppTaskToFront()
        finish()
    }

    private fun moveAppTaskToFront() {
        val am = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        am.appTasks.forEach {
            if (it.taskInfo.baseActivity?.className == LinkExampleActivity::class.java.name) {
                it.moveToFront()
                return
            }
        }
    }
}
