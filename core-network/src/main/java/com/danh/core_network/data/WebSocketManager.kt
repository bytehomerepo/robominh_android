package com.danh.core_network.data

import android.util.Log
import okhttp3.*
import org.json.JSONObject

class WebSocketManager {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .retryOnConnectionFailure(true)
        .build()

    fun connect(
        url: String,
        token: String? = null,
        onConnected: (() -> Unit)? = null,
        onMessage: ((String) -> Unit)? = null,
        onClosed: ((Int, String) -> Unit)? = null,
        onFailure: ((Throwable) -> Unit)? = null
    ) {
        val requestBuilder = Request.Builder()
            .url(url)

        if (!token.isNullOrBlank()) {
            requestBuilder.addHeader("Authorization", "Bearer $token")
        }

        val request = requestBuilder.build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {

            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d("WebSocket", "Connected")
                onConnected?.invoke()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d("WebSocket", "Message: $text")
                onMessage?.invoke(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closing: $code / $reason")
                webSocket.close(code, reason)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d("WebSocket", "Closed: $code / $reason")
                onClosed?.invoke(code, reason)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e("WebSocket", "Error: ${t.message}", t)
                onFailure?.invoke(t)
            }
        })
    }

    fun sendText(
        text: String,
        voice: String,
        timestamp: Long,
        duration: Double
    ) {
        val json = JSONObject().apply {
            put("text", text)
            put("language", "VI")
            put("voice", voice)
            put("timestamp", timestamp)
            put("duration", duration)
        }

        webSocket?.send(json.toString())
    }

    fun disconnect() {
        webSocket?.close(1000, "Client closed")
        webSocket = null
    }
}