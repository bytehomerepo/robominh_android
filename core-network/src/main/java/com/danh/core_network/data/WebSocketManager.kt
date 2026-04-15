package com.danh.core_network.data

import android.util.Log
import okhttp3.*
import okio.ByteString
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
        onMessage: ((String) -> Unit)? = null, // raw json string
        onReceiveText: ((type: String, text: String, audioUrl: String?) -> Unit)? = null,
        onClosed: ((Int, String) -> Unit)? = null,
        onFailure: ((Throwable) -> Unit)? = null
    ) {
        val requestBuilder = Request.Builder().url(url)

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
                try {
                    val json = JSONObject(text)
                    val type = json.optString("type")
                    val messageText = json.optString("text")
                    val audioUrl = json.optString("audioUrl", null)

                    onReceiveText?.invoke(type, messageText, audioUrl)
                } catch (e: Exception) {
                    Log.e("WebSocket", "Parse message error: ${e.message}", e)
                }
            }

            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d("WebSocket", "Binary message size: ${bytes.size}")
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
        language: String,
        voice: String,
        timestamp: Long,
        duration: Float
    ) {
        val json = JSONObject().apply {
            put("text", text)
            put("language", language)
            put("voice", voice)
            put("timestamp", timestamp)
            put("duration", duration)
        }

        webSocket?.send(json.toString())
        Log.d("lasttext", "da gửi")
    }

    fun disConnectUser(
        language: String,
        voice: String,
        timestamp: Long,
        duration: Float
    ) {
        val json = JSONObject().apply {
            put("text", "disconectuser")
            put("language", language)
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