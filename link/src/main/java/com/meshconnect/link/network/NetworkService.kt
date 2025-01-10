package com.meshconnect.link.network

import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import java.net.HttpURLConnection
import java.net.URL
import kotlin.coroutines.resume

class NetworkService {

    suspend fun doPost(url: String, body: String) = suspendCancellableCoroutine { continuation ->
        val connection = URL(url).openConnection() as HttpURLConnection

        continuation.invokeOnCancellation { connection.disconnect() }

        try {
            connection.apply {
                doOutput = true
                setChunkedStreamingMode(0)
                setRequestProperty("Content-Type", "application/json")
                // write body
                connection.outputStream.bufferedWriter().use { it.write(body) }
                connection.responseCode
            }
        } finally {
            connection.disconnect()
            continuation.resume(Unit)
        }
    }

    suspend fun doGet(url: String) = suspendCancellableCoroutine { continuation ->
        val connection = URL(url).openConnection() as HttpURLConnection

        continuation.invokeOnCancellation { connection.disconnect() }

        try {
            connection.apply {
                setChunkedStreamingMode(0)
                val responseCode = connection.responseCode
                Log.d("3qq", "NetworkService doGet: $responseCode")
            }
        } finally {
            connection.disconnect()
            continuation.resume(Unit)
        }
    }
}
