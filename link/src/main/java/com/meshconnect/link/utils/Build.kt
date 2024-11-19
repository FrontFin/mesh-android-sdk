package com.meshconnect.link.utils

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

@ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
internal val isAtLeastOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
