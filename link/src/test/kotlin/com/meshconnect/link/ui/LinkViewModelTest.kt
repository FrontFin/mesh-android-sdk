package com.meshconnect.link.ui

import com.meshconnect.link.PayloadEmitter
import com.meshconnect.link.ViewModelTest
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.entity.LinkPayload
import com.meshconnect.link.randomString
import com.meshconnect.link.testObserver
import com.meshconnect.link.usecase.BroadcastLinkMessageUseCase
import com.meshconnect.link.usecase.DeserializeLinkMessageUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainSame
import org.junit.Test

class LinkViewModelTest : ViewModelTest() {

    private val deserializeLinkMessageUseCase = mockk<DeserializeLinkMessageUseCase>()
    private val payloadEmitter = mockk<PayloadEmitter>()
    private val broadcastLinkMessageUseCase = mockk<BroadcastLinkMessageUseCase>()

    private val viewModel = LinkViewModel(
        mainCoroutineRule.dispatcher,
        deserializeLinkMessageUseCase,
        payloadEmitter,
        broadcastLinkMessageUseCase
    )

    @Test
    fun `test factory`() {
        LinkViewModel.Factory().create(LinkViewModel::class.java)
    }

    @Test
    fun `verify viewModel emits 'done' event`() {
        val json = randomString
        val event = LinkEvent.Done
        val eventObserver = viewModel.linkEvent.testObserver()

        coEvery { deserializeLinkMessageUseCase.launch(json) } returns event
        coEvery { broadcastLinkMessageUseCase.launch(json) } just Runs
        viewModel.onJsonReceived(json)

        viewModel.payloads.shouldBeEmpty()
        viewModel.error shouldBe null
        eventObserver.shouldContainEvents(event)
    }

    @Test
    fun `verify viewModel emits error`() = runTest {
        val json = randomString
        val th = mockk<Throwable>()
        val errorObserver = viewModel.throwable.testObserver()

        coEvery { deserializeLinkMessageUseCase.launch(json) } throws th
        coEvery { broadcastLinkMessageUseCase.launch(any()) } just Runs
        viewModel.onJsonReceived(json)

        errorObserver.shouldContainEvents(th)
        viewModel.error shouldBe th
    }

    @Test
    fun `verify viewModel emits payload`() = runTest {
        val json = randomString
        val payload: LinkPayload = mockk()
        val event = LinkEvent.Payload(payload)
        val eventObserver = viewModel.linkEvent.testObserver()

        coEvery { deserializeLinkMessageUseCase.launch(json) } returns event
        coEvery { payloadEmitter.emit(payload) } just Runs
        coEvery { broadcastLinkMessageUseCase.launch(json) } just Runs
        viewModel.onJsonReceived(json)

        viewModel.payloads.shouldContainSame(listOf(payload))
        viewModel.error shouldBe null
        eventObserver.shouldContainEvents(event)
    }
}