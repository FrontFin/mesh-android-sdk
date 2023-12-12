package com.meshconnect.link.entity

data class LinkConfiguration(
    val token: String,
    val accessTokens: List<IntegrationAccessToken>? = null,
    val transferDestinationTokens: List<IntegrationAccessToken>? = null
)
