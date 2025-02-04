package com.meshconnect.link.entity

import org.amshove.kluent.internal.assertEquals
import org.junit.Test

class TransferFinishedErrorPayloadTest {
    @Test
    fun `test TransferFinishedErrorPayload`() {
        val payload = TransferFinishedErrorPayload("err")
        assertEquals(payload.errorMessage, "err")
    }
}
