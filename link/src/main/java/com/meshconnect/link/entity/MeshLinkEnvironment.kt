package com.meshconnect.link.entity

/**
 * Mesh environment an MFS session token belongs to.
 *
 * Names match the web SDK's `LinkEnvironment`. There is deliberately no `LOCAL`
 * entry: it points at localhost, which on a device is the device itself, and
 * plain http is not allowlisted. Tunnel a local Link through LocalCan instead.
 */
enum class MeshLinkEnvironment(internal val linkUrl: String) {
    PROD("https://link.meshpay.com"),
    SBX("https://link.sbx.meshpay.com"),
    DEV("https://link.dev.meshpay.com"),
}
