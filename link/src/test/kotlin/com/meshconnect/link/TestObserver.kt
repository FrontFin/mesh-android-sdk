package com.meshconnect.link

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.meshconnect.link.utils.Event
import org.amshove.kluent.shouldContainSame

@Suppress("unused")
class TestObserver<T> : Observer<T> {
    private val observedValues = mutableListOf<T>()

    fun <Event> shouldContainEvents(vararg events: Event) {
        val wrapped = events.map { Event(it) }
        observedValues.shouldContainSame(wrapped)
    }

    fun <T> shouldContainValues(vararg values: T) {
        observedValues.shouldContainSame(values.asList())
    }

    fun shouldBeEmpty() {
        assert(observedValues.size == 0)
    }

    fun clear() {
        observedValues.clear()
    }

    fun printValues() {
        println("Total count = ${observedValues.size}")
        observedValues.forEach { println("item = $it") }
    }

    override fun onChanged(value: T) {
        observedValues.add(value)
    }
}

fun <T> LiveData<T>.testObserver() = TestObserver<T>().also {
    observeForever(it)
}
