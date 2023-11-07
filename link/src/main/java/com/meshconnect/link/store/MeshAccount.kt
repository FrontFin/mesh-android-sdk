package com.meshconnect.link.store

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Entity of connected brokerage account.
 */
@Parcelize
data class MeshAccount(
    val accessToken: String,
    val refreshToken: String?,
    val accountId: String,
    val accountName: String,
    val brokerType: String,
    val brokerName: String,
    val id: String = brokerType + accountId
) : Parcelable
