package com.getfront.catalog.converter

import com.getfront.catalog.entity.TransferFinishedErrorPayload
import com.getfront.catalog.entity.TransferFinishedPayload
import com.getfront.catalog.entity.TransferFinishedSuccessPayload
import com.google.gson.GsonBuilder
import org.junit.Test

class TestTransferFinishedPayloadDeserializer {

    private val gson = GsonBuilder().registerTypeAdapter(
        TransferFinishedPayload::class.java,
        TransferFinishedPayloadDeserializer()
    ).create()

    @Test
    fun testSuccess() {
        val payload = gson.fromJson<TransferFinishedPayload>("{'status':'success'}")
        assert(payload is TransferFinishedSuccessPayload)
    }

    @Test
    fun testError() {
        val payload = gson.fromJson<TransferFinishedPayload>("{'status':'error'}")
        assert(payload is TransferFinishedErrorPayload)
    }
}
