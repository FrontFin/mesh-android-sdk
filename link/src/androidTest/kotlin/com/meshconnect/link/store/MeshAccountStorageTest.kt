package com.meshconnect.link.store

import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.test.runTest
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContain
import org.amshove.kluent.shouldHaveSize
import org.junit.Test

class MeshAccountStorageTest {
    @Test
    fun test() = runTest {
        val context = InstrumentationRegistry.getInstrumentation().context
        val store = createPreferenceAccountStore(context)
        val account = MeshAccount(
            accountId = "552e9c9f-test-5143-9f72-ab3d396word4",
            brokerType = "coinbase",
            brokerName = "Coinbase",
            accessToken = "<coinbase_access_token>",
            refreshToken = "<coinbase_refresh_token>",
            accountName = "Awesome Mesh Test Account",
        )
        // Test storage should be empty after clear
        store.clear()
        store.getAll().shouldBeEmpty()
        // Test storage should contain account after insert
        store.insert(accounts = listOf(account))
        store.getAll().run {
            shouldHaveSize(1)
            shouldContain(account)
        }
        // Test storage should remove account correctly
        store.remove(id = account.id)
        store.getAll().shouldBeEmpty()
    }
}