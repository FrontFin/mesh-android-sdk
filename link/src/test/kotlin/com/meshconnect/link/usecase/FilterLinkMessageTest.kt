package com.meshconnect.link.usecase

import com.meshconnect.link.randomString
import org.amshove.kluent.shouldBeNull
import org.amshove.kluent.shouldContainSame
import org.amshove.kluent.shouldNotBeNull
import org.junit.Test

class FilterLinkMessageTest {

    private val filterLinkMessage = FilterLinkMessageImpl

    @Test
    fun `filter link message`() {
        verifyTypeReplaced("brokerageAccountAccessToken", "integrationConnected")
        verifyTypeReplaced("delayedAuthentication", "integrationConnected")
        verifyTypeReplaced("transferFinished", "transferCompleted")
        verifyTypeReplaced("loaded", "pageLoaded")
        verifyTypeExists("integrationConnectionError")
        verifyTypeExists("integrationSelected")
        verifyTypeExists("credentialsEntered")
        verifyTypeExists("transferStarted")
        verifyTypeExists("transferPreviewed")
        verifyTypeExists("transferPreviewError")
        verifyTypeExists("transferExecutionError")
        verifyTypeExists("walletMessageSigned")
        verifyTypeExists("verifyWalletRejected")
        verifyTypeExists("verifyDonePage")
        verifyTypeNotExist(randomString)
        verifyTypeNotExist("")
    }

    private fun verifyTypeReplaced(original: String, expected: String) {
        filterLinkMessage(mapOfType(original))
            .shouldNotBeNull()
            .shouldContainSame(mapOfType(expected))
    }

    private fun verifyTypeExists(type: String) {
        filterLinkMessage(mapOfType(type))
            .shouldNotBeNull()
            .shouldContainSame(mapOfType(type))
    }

    private fun verifyTypeNotExist(type: String) {
        filterLinkMessage(mapOfType(type))
            .shouldBeNull()
    }

    private fun mapOfType(type: String) = mapOf("type" to type)
}