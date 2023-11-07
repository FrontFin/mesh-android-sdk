package com.meshconnect.link.ui

import com.meshconnect.link.ViewModelTest
import com.meshconnect.link.entity.LinkEvent
import com.meshconnect.link.store.PayloadReceiver
import com.meshconnect.link.testObserver
import com.meshconnect.link.usecase.GetLinkEventUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainSame
import org.junit.Test

class LinkViewModelTest : ViewModelTest() {

    private val getLinkEventUseCase = mockk<GetLinkEventUseCase>()
    private val payloadReceiver = mockk<PayloadReceiver>()
    private val viewModel = LinkViewModel(getLinkEventUseCase, payloadReceiver)

    @Test
    fun `verify viewModel emits error`() = runTest {
        val th = mockk<Throwable>()
        val errorObserver = viewModel.throwable.testObserver()
        coEvery { getLinkEventUseCase.launch("") } returns Result.failure(th)
        viewModel.onJsonReceived("")

        errorObserver.shouldContainEvents(th)
        assert(viewModel.error == th)
    }

    @Test
    fun `verify viewModel emits payload`() = runTest {
        val payload = mockk<LinkEvent.Payload> {
            every { payload } returns mockk()
        }
        val eventObserver = viewModel.linkEvent.testObserver()
        coEvery { getLinkEventUseCase.launch("") } returns Result.success(payload)
        coEvery { payloadReceiver.emit(payload.payload) } just Runs
        viewModel.onJsonReceived("")

        viewModel.payloads.shouldContainSame(listOf(payload.payload))
        assert(viewModel.error == null)
        eventObserver.shouldContainEvents(payload)
    }

    @Test
    fun `verify viewModel emits 'done' event`() {
        val event = LinkEvent.Done
        val eventObserver = viewModel.linkEvent.testObserver()
        coEvery { getLinkEventUseCase.launch("") } returns Result.success(event)
        viewModel.onJsonReceived("")

        viewModel.payloads.shouldBeEmpty()
        assert(viewModel.error == null)
        eventObserver.shouldContainEvents(event)
    }

    @Test
    fun `test factory`() {
        LinkViewModel.Factory().create(LinkViewModel::class.java)
    }
}