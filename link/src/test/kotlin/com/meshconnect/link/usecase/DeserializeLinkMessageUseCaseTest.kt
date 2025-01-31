package com.meshconnect.link.usecase

import com.meshconnect.link.UseCaseTest
import com.meshconnect.link.entity.AccessTokenPayload
import com.meshconnect.link.entity.JsError
import com.meshconnect.link.entity.JsType
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.TransferFinishedPayload
import com.meshconnect.link.entity.Type
import com.meshconnect.link.randomString
import com.meshconnect.link.converter.JsonConverter
import com.meshconnect.link.entity.DelayedAuthPayload
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEqualTo
import org.junit.Assert.assertThrows
import org.junit.Test
import kotlin.IllegalStateException

class DeserializeLinkMessageUseCaseTest : UseCaseTest() {

    private val jsonConverter = mockk<JsonConverter>()
    private val useCase = DeserializeLinkMessageUseCase(jsonConverter)

    private inline fun <reified T> mockkFromJson(classOfT: Class<T>, returnValue: T) {
        every { jsonConverter.fromJson(any(), classOfT) } returns returnValue
    }

    private fun mockkJsType(type: Type?) {
        mockkFromJson(JsType::class.java, JsType(type))
    }

    @Test
    fun `test done`() = runTest {
        mockkJsType(Type.done)
        useCase.launch("") shouldBeEqualTo LinkEvent.Done
    }

    @Test
    fun `test close`() = runTest {
        mockkJsType(Type.close)
        useCase.launch("") shouldBeEqualTo LinkEvent.Close
    }

    @Test
    fun `test showClose`() = runTest {
        mockkJsType(Type.showClose)
        useCase.launch("") shouldBeEqualTo LinkEvent.ShowClose
    }

    @Test
    fun `test Undefined`() = runTest {
        mockkJsType(null)
        useCase.launch("") shouldBeEqualTo LinkEvent.Undefined
    }

    @Test
    fun `test Loaded`() = runTest {
        mockkJsType(Type.loaded)
        useCase.launch("") shouldBeEqualTo LinkEvent.Loaded
    }

    @Test
    fun `test brokerageAccountAccessToken`() = runTest {
        val jsType = JsType(Type.brokerageAccountAccessToken)
        val payload = mockk<AccessTokenPayload>()

        mockkFromJson(JsType::class.java, jsType)
        mockkFromJson(AccessTokenPayload::class.java, payload)

        useCase.launch("") shouldBeEqualTo LinkEvent.Payload(payload)
    }

    @Test
    fun `test transferFinished`() = runTest {
        val jsType = JsType(Type.transferFinished)
        val payload = mockk<TransferFinishedPayload>()

        mockkFromJson(JsType::class.java, jsType)
        mockkFromJson(TransferFinishedPayload::class.java, payload)

        useCase.launch("") shouldBeEqualTo LinkEvent.Payload(payload)
    }

    @Test
    fun `test delayedAuthentication`() = runTest {
        val jsType = JsType(Type.delayedAuthentication)
        val payload = mockk<DelayedAuthPayload>()

        mockkFromJson(JsType::class.java, jsType)
        mockkFromJson(DelayedAuthPayload::class.java, payload)

        useCase.launch("") shouldBeEqualTo LinkEvent.Payload(payload)
    }

    @Test
    fun `test error`() = runTest {
        val msg = randomString
        val jsType = JsType(Type.error)

        mockkFromJson(JsType::class.java, jsType)
        mockkFromJson(JsError::class.java, JsError(msg))

        assertThrows(msg, IllegalStateException::class.java) {
            useCase.launch("")
        }
    }
}