package com.meshconnect.link.entity

data class IntegrationAccessToken(
    val accountId: String,
    val accountName: String,
    val accessToken: String,
    val brokerType: String,
    val brokerName: String,
)
