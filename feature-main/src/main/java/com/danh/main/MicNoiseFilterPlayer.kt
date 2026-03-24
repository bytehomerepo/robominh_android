package com.danh.main

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.*
import androidx.core.content.ContextCompat
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.concurrent.thread

class MicNoiseFilterPlayer(private val context: Context) {

    private var isRunning = false
    private var recordThread: Thread? = null

    private var audioRecord: AudioRecord? = null
    private var audioTrack: AudioTrack? = null
    private var deepFilterNet: com.rikorose.deepfilternet.NativeDeepFilterNet? = null

    fun start() {
        if (isRunning) return

        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            throw SecurityException("Chưa có quyền RECORD_AUDIO")
        }

        // Khởi tạo bộ lọc
        deepFilterNet = com.rikorose.deepfilternet.NativeDeepFilterNet(context).apply {
            setAttenuationLimit(30f) // mức giảm nhiễu
        }

        val filter = deepFilterNet ?: return
        val frameLength = filter.frameLength.toInt()

        // DeepFilterNet thường chạy tốt với mono 16-bit
        val sampleRate = 48000
        val channelIn = AudioFormat.CHANNEL_IN_MONO
        val channelOut = AudioFormat.CHANNEL_OUT_MONO
        val encoding = AudioFormat.ENCODING_PCM_16BIT

        val minRecordBuffer = AudioRecord.getMinBufferSize(sampleRate, channelIn, encoding)
        val minTrackBuffer = AudioTrack.getMinBufferSize(sampleRate, channelOut, encoding)

        val recordBufferSize = maxOf(minRecordBuffer, frameLength * 4)
        val trackBufferSize = maxOf(minTrackBuffer, frameLength * 4)

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            channelIn,
            encoding,
            recordBufferSize
        )

        audioTrack = AudioTrack(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build(),
            AudioFormat.Builder()
                .setEncoding(encoding)
                .setSampleRate(sampleRate)
                .setChannelMask(channelOut)
                .build(),
            trackBufferSize,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )

        isRunning = true

        recordThread = thread(start = true) {
            val inputBytes = ByteArray(frameLength)
            val outputBytes = ByteArray(frameLength)

            val byteBuffer = ByteBuffer.allocateDirect(frameLength).apply {
                order(ByteOrder.LITTLE_ENDIAN)
            }

            audioRecord?.startRecording()
            audioTrack?.play()

            while (isRunning) {
                val read = audioRecord?.read(
                    inputBytes,
                    0,
                    frameLength,
                    AudioRecord.READ_BLOCKING
                ) ?: 0

                if (read <= 0) continue

                byteBuffer.clear()
                byteBuffer.put(inputBytes, 0, read)

                // Nếu đọc chưa đủ 1 frame thì chèn 0 cho đủ
                if (read < frameLength) {
                    repeat(frameLength - read) { byteBuffer.put(0) }
                }

                byteBuffer.rewind()

                // Lọc nhiễu tại chỗ
                filter.processFrame(byteBuffer)

                // Lấy dữ liệu đã lọc ra
                byteBuffer.rewind()
                byteBuffer.get(outputBytes)

                audioTrack?.write(outputBytes, 0, outputBytes.size)
            }

            stopInternal()
        }
    }

    fun stop() {
        isRunning = false
    }

    private fun stopInternal() {
        try {
            audioRecord?.stop()
        } catch (_: Exception) {
        }
        try {
            audioTrack?.stop()
        } catch (_: Exception) {
        }

        audioRecord?.release()
        audioTrack?.release()
        deepFilterNet?.release()

        audioRecord = null
        audioTrack = null
        deepFilterNet = null
        recordThread = null
    }
}