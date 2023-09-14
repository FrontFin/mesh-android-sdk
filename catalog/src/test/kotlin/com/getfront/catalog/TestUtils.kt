package com.getfront.catalog

import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.streams.asSequence

fun readFile(fileName: String): String {
    val ins = ClassLoader.getSystemClassLoader().getResourceAsStream(fileName)
    return BufferedReader(InputStreamReader(ins)).use { it.readText() }
}

val randomString: String
    get() {
        val source = "LOREM_IPSUM_DOOR_AMEND"
        return java.util.Random()
            .ints(10, 0, source.length)
            .asSequence()
            .map(source::get)
            .joinToString("")
    }

