package com.meshconnect.link.entity

data class LinkConfiguration(
    val token: String,
    val accessTokens: List<IntegrationAccessToken>? = null,
    val disableDomainWhiteList: Boolean? = false,
    val language: String? = null,
    val displayFiatCurrency: String? = null,
    val theme: LinkTheme? = null,
)
