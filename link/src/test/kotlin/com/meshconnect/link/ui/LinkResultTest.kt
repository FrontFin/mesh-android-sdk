package com.meshconnect.link.ui

import android.content.Intent
import io.mockk.every
import io.mockk.mockk
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldBeNull
import org.junit.Test

class LinkResultTest {
    // region LinkExit

    @Test
    fun `LinkExit errorMessage is null by default`() {
        LinkExit().errorMessage.shouldBeNull()
    }

    @Test
    fun `LinkExit errorMessage returns the provided string`() {
        LinkExit("something went wrong").errorMessage shouldBeEqualTo "something went wrong"
    }

    @Test
    fun `LinkExit errorMessage is null when null is passed`() {
        LinkExit(errorMessage = null).errorMessage.shouldBeNull()
    }

    // region deprecated compat

    @Test
    fun `deprecated Throwable constructor maps message to errorMessage`() {
        @Suppress("DEPRECATION")
        val exit = LinkExit(RuntimeException("legacy error"))
        exit.errorMessage shouldBeEqualTo "legacy error"
    }

    @Test
    fun `deprecated Throwable constructor with null maps to null errorMessage`() {
        @Suppress("DEPRECATION")
        val exit = LinkExit(null as Throwable?)
        exit.errorMessage.shouldBeNull()
    }

    @Test
    fun `deprecated error property is always null`() {
        @Suppress("DEPRECATION")
        LinkExit(RuntimeException("x")).error.shouldBeNull()
    }

    // endregion

    // endregion

    // region getLinkResult

    @Test
    fun `getLinkResult returns LinkExit with null message when intent is null`() {
        val result = LinkActivity.getLinkResult(null)
        result.shouldBeInstanceOf<LinkExit>()
        (result as LinkExit).errorMessage.shouldBeNull()
    }

    @Test
    fun `getLinkResult returns LinkExit with message when getParcelable throws`() {
        val exception = RuntimeException("unparcel failure")
        val intent =
            mockk<Intent> {
                // Stub both overloads so the test is not sensitive to which branch
                // the inline getParcelable extension picks based on Build.VERSION.SDK_INT.
                @Suppress("DEPRECATION")
                every { getParcelableExtra<LinkResult>(any()) } throws exception
                every { getParcelableExtra(any(), LinkResult::class.java) } throws exception
            }
        val result = LinkActivity.getLinkResult(intent)
        result.shouldBeInstanceOf<LinkExit>()
        (result as LinkExit).errorMessage shouldBeEqualTo "unparcel failure"
    }

    // endregion
}
