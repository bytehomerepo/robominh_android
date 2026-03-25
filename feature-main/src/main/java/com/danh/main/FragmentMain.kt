package com.danh.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.danh.main.databinding.FragmentMainBinding

class FragmentMain : Fragment() {
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false

    private val handler = Handler(Looper.getMainLooper())
    private val speechThresholdDb = 28f
    private lateinit var binding: FragmentMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startListening()
            } else {
                Toast.makeText(requireContext(), "Chưa cấp quyền micro", Toast.LENGTH_SHORT).show()
            }
        }

    private fun setupViews() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                binding.txtResult.text = "Đang nghe..."
                resetSilenceTimer()
            }

            override fun onBeginningOfSpeech() {
                binding.txtResult.text = "Đã phát hiện bắt đầu nói..."
                resetSilenceTimer()
            }

            override fun onRmsChanged(rmsdB: Float) {
                if (!isListening) return

                // Chỉ reset timer khi âm lượng đủ lớn để coi là giọng nói
                if (rmsdB >= speechThresholdDb) {
                    binding.txtResult.text = "Đang nghe... rms=$rmsdB"
                    resetSilenceTimer()
                }
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                binding.stop.append("\nKết thúc lời nói, đang xử lý...")
            }

            override fun onError(error: Int) {
                isListening = false
                handler.removeCallbacks(stopBecauseSilence)
                binding.txtResult.text = "Lỗi speech: $error"
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                handler.removeCallbacks(stopBecauseSilence)

                val texts = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                binding.txtResult.text = texts?.firstOrNull() ?: "Không có kết quả"
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val texts = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val text = texts?.firstOrNull()
                if (!text.isNullOrBlank()) {
                    binding.txtResult.text = text
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}

        })
        binding.btnStart.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            } else {
                requestPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.btnStop.setOnClickListener {
            stopListening()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
    }

    private val stopBecauseSilence = Runnable {
        if (isListening) {
            stopListening()
        }
    }

    private fun startListening() {
        if (isListening) return

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }

        isListening = true
        speechRecognizer?.startListening(intent)
        resetSilenceTimer()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private fun stopListening() {
        isListening = false

        handler.removeCallbacks(stopBecauseSilence)
        speechRecognizer?.stopListening()
    }

    private fun resetSilenceTimer() {
        handler.removeCallbacks(stopBecauseSilence)
        handler.postDelayed(stopBecauseSilence, 200)
        handler.postDelayed(stopBecauseSilence, 200)
    }

    override fun onDestroy() {
        handler.removeCallbacks(stopBecauseSilence)
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        super.onDestroy()
    }
}
