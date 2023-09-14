package com.getfront.catalog.usecase

import com.getfront.catalog.UseCaseTest
import com.getfront.catalog.converter.fromJson
import com.getfront.catalog.entity.AccessTokenPayload
import com.getfront.catalog.entity.AccessTokenResponse
import com.getfront.catalog.entity.JsError
import com.getfront.catalog.entity.JsType
import com.getfront.catalog.entity.LinkEvent
import com.getfront.catalog.entity.TransferFinishedPayload
import com.getfront.catalog.entity.TransferFinishedResponse
import com.getfront.catalog.entity.Type
import com.getfront.catalog.randomString
import com.google.gson.Gson
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.IllegalStateException

class GetLinkEventUseCaseTest : UseCaseTest() {

    private val gson = mockk<Gson>()

    private val useCase = GetLinkEventUseCase(
        mainCoroutineRule.dispatcher,
        gson
    )

    @Test
    fun `test done`() = runTest {
        every { gson.fromJson<JsType>("") } returns JsType(Type.done)
        val result = useCase.launch("")
        assert(result.isSuccess)
        assert(result.getOrNull() == LinkEvent.Done)
    }

    @Test
    fun `test close`() = runTest {
        every { gson.fromJson<JsType>("") } returns JsType(Type.close)
        val result = useCase.launch("")
        assert(result.isSuccess)
        assert(result.getOrNull() == LinkEvent.Close)
    }

    @Test
    fun `test showClose`() = runTest {
        every { gson.fromJson<JsType>("") } returns JsType(Type.showClose)
        val result = useCase.launch("")
        assert(result.isSuccess)
        assert(result.getOrNull() == LinkEvent.ShowClose)
    }

    @Test
    fun `test Undefined`() = runTest {
        every { gson.fromJson<JsType>("") } returns JsType(null)
        val result = useCase.launch("")
        assert(result.isSuccess)
        assert(result.getOrNull() == LinkEvent.Undefined)
    }

    @Test
    fun `test brokerageAccountAccessToken`() = runTest {
        val jsType = JsType(Type.brokerageAccountAccessToken)
        val payload = mockk<AccessTokenPayload>()
        val response = AccessTokenResponse(payload)

        every { gson.fromJson<JsType>("") } returns jsType
        every { gson.fromJson<AccessTokenResponse>("") } returns response

        val result = useCase.launch("")
        assert(result.isSuccess)
        assert((result.getOrNull() as LinkEvent.Payload).payload == payload)
    }

    @Test
    fun `test transferFinished`() = runTest {
        val jsType = JsType(Type.transferFinished)
        val payload = mockk<TransferFinishedPayload>()
        val response = TransferFinishedResponse(payload)

        every { gson.fromJson<JsType>("") } returns jsType
        every { gson.fromJson<TransferFinishedResponse>("") } returns response

        val result = useCase.launch("")
        assert(result.isSuccess)
        assert((result.getOrNull() as LinkEvent.Payload).payload == payload)
    }

    @Test
    fun `test error`() = runTest {
        val msg = randomString
        val jsType = JsType(Type.error)

        every { gson.fromJson<JsType>("") } returns jsType
        every { gson.fromJson<JsError>("") } returns JsError(msg)

        val result = useCase.launch("")
        assert(result.isFailure)
        val ex = result.exceptionOrNull()
        assert(ex?.message == msg)
        assert(ex is java.lang.IllegalStateException)
    }

    @Test
    fun `test exception catch for onAccessToken function`() {
        every { gson.fromJson<AccessTokenResponse>("") } throws Exception()
        val ex = assertThrows(IllegalStateException::class.java) {
            useCase.onAccessToken("")
        }
        assert(ex.message!!.startsWith("Faced an error while parsing access token payload"))
    }

    @Test
    fun `test exception catch for onTransferFinished function`() {
        every { gson.fromJson<TransferFinishedResponse>("") } throws Exception()
        val ex = assertThrows(IllegalStateException::class.java) {
            useCase.onTransferFinished("")
        }
        assert(ex.message!!.startsWith("Faced an error while parsing transfer finished payload"))
    }
}