@file:Suppress("unused")

package com.getfront.utils.log

import android.util.Log

const val TAG = "frontLog"

fun log(msg: Any?, tag: String = TAG) = Log.d(tag, msg.toString())
