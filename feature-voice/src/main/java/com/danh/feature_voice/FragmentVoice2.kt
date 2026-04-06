package com.danh.feature_voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.danh.feature_voice.databinding.FragmentVoiceBinding
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import kotlinx.coroutines.launch
import com.danh.core_network.data.WebSocketManager
import com.danh.feature_voice.databinding.FragmentVoice2Binding
import com.danh.myapplication.data.TokenManager

class FragmentVoice2 : Fragment() {
    private lateinit var binding: FragmentVoice2Binding
    private var videoPlayer: ExoPlayer? = null
    private var audioPlayer: ExoPlayer? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private val handler = Handler(Looper.getMainLooper())
    private var lastText = ""
    private var hasAnyText = false
    private val stopBecauseNoNewText = Runnable {
        if (isListening) {
            stopListening()
        }
    }

    private val requestPermission: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) startListening()
            else Toast.makeText(requireContext(), "Chưa cấp quyền micro", Toast.LENGTH_SHORT).show()
        }
    private lateinit var webSocketManager: WebSocketManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentVoice2Binding.inflate(inflater, container, false)
        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVideoPlayer()
        setUpViewWait()
        setWebSocket()
        setUpData()
    }
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.show()
    }
    private fun initVideoPlayer() {
        if (videoPlayer == null) {
            videoPlayer = ExoPlayer.Builder(requireContext()).build().also {
                binding.playerView.player = it
                it.repeatMode = Player.REPEAT_MODE_ALL
                it.playWhenReady = true
            }
        }
    }
    private fun showVideo(rawRes: Int) {
        val uri = "android.resource://${requireContext().packageName}/$rawRes".toUri()
        videoPlayer?.apply {
            setMediaItem(MediaItem.fromUri(uri))
            prepare()
            play()
        }
    }

    private fun setUpViewWait() = showVideo(R.raw.awit)
    private fun setUpIconVoice() = showVideo(R.raw.voice)
    private fun setUpIconListen() = showVideo(R.raw.listen)

    @UnstableApi
    private fun setWebSocket() {
        webSocketManager = WebSocketManager()
        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager(requireContext().applicationContext).getToken()
            Log.d("TOKEN_MAIN", "token = $token")
            webSocketManager.connect(
                url = "ws://118.70.187.211:4000?token=$token",
                token = token,
                onConnected = {
                    requireActivity().runOnUiThread {

                    }
                },
                onMessage = { message ->
                    requireActivity().runOnUiThread {

                    }
                },
                onReceiveText = { type, text, audioUrl ->
                    requireActivity().runOnUiThread {
                        receiveText(type, text, audioUrl)
                    }
                },
                onClosed = { code, reason ->
                    requireActivity().runOnUiThread {

                    }
                },
                onFailure = { error ->
                    requireActivity().runOnUiThread {

                    }
                }
            )
        }
        binding.playerView.setOnClickListener {
            if (audioPlayer?.isPlaying == true) {
                stopAudioStream()
                return@setOnClickListener
            }

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                startListening()
            } else {
                requestPermission.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    private fun stopAudioStream(resetIcon: Boolean = true) {
        audioPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        audioPlayer = null

        if (resetIcon && isAdded) {
            startListening()
        }
    }

    @UnstableApi
    private fun receiveText(type: String?, text: String?, audioUrl: String?) {
        if (!audioUrl.isNullOrEmpty()) {
            Log.d("WS", "type=$type")
            Log.d("WS", "text=$text")
            Log.d("Đã nhận", "audioUrl=$audioUrl")
            playAudioStream(audioUrl)
        }
    }

    @UnstableApi
    private fun playAudioStream(url: String) {
        stopAudioStream(resetIcon = false)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1000,   // minBufferMs — chỉ cần 1 giây là phát
                10000,  // maxBufferMs
                500,    // bufferForPlaybackMs — chỉ cần 500ms để bắt đầu
                500     // bufferForPlaybackAfterRebufferMs
            )
            .build()
        audioPlayer = ExoPlayer.Builder(requireContext()).setLoadControl(loadControl).build().also { exoPlayer ->
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)

            exoPlayer.addListener(object : Player.Listener {

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when (playbackState) {
                        Player.STATE_BUFFERING -> {
                            Log.d("AudioStream", "STATE_BUFFERING")
                        }

                        Player.STATE_READY -> {
                            Log.d("AudioStream", "STATE_READY")
                        }

                        Player.STATE_ENDED -> {
                            Log.d("AudioStream", "STATE_ENDED")
                            stopAudioStream(false)
                            startListening()
                            setUpIconListen()
                        }

                        Player.STATE_IDLE -> {
                            Log.d("AudioStream", "STATE_IDLE")
                        }
                    }
                }

                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Log.d("AudioStream", "isPlaying=$isPlaying")

                    if (isPlaying) {
                        setUpIconVoice()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    Log.e("AudioStream", "Lỗi phát audio stream", error)
                    Toast.makeText(requireContext(), "Không phát được audio", Toast.LENGTH_SHORT)
                        .show()
                    stopAudioStream()
                }
            })

            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun setUpData() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onReadyForSpeech(params: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {

            }

            override fun onError(error: Int) {
                    isListening = false
                    handler.removeCallbacks(stopBecauseNoNewText)
                    lastText = ""
                    recreateSpeechRecognizer()
                    Log.d("result", "error+${speechRecognizer}")
                    recreateSpeechRecognizer()
                    setUpViewWait()
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                handler.removeCallbacks(stopBecauseNoNewText)
                val textToSend = lastText
                lastText = ""
                Log.d("lastText", "onPartialResults: $textToSend")
                Log.d("result", "result")
                recreateSpeechRecognizer()
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val texts =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val newText = texts?.firstOrNull()?.trim().orEmpty()

                if (newText.isNotEmpty() && newText != lastText) {
                    lastText = newText
                    hasAnyText = true
                    resetNoNewTextTimer()
                }
                Log.d("result",lastText)
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun startListening() {
        if (isListening) return

        setUpIconListen()

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        isListening = true
        lastText = ""
        hasAnyText = false
        handler.removeCallbacks(stopBecauseNoNewText)
        speechRecognizer?.startListening(intent)
    }

    private fun resetNoNewTextTimer() {
        handler.removeCallbacks(stopBecauseNoNewText)
        handler.postDelayed(stopBecauseNoNewText, 1500)
    }

    private fun stopListening() {
        isListening = false
        handler.removeCallbacks(stopBecauseNoNewText)
        Log.d("lasttext",lastText)
        speechRecognizer?.stopListening()
        if (lastText.isNotEmpty()) {
            val textToSend = lastText
            lastText = ""
            webSocketManager.sendText(textToSend, "VI", "giongnuhanoi", 112233, 2.5f)
            Log.d("result","Dữ liệu được gửi lên server: "+textToSend)
            Log.d("result","lasttext ${lastText}")
        }

    }

    override fun onDestroy() {
        handler.removeCallbacks(stopBecauseNoNewText)
        speechRecognizer = null
        super.onDestroy()
    }
    override fun onDestroyView() {
        handler.removeCallbacks(stopBecauseNoNewText)

        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null

        audioPlayer?.release()
        audioPlayer = null

        videoPlayer?.release()
        videoPlayer = null

        binding.playerView.player = null
        super.onDestroyView()
    }
    private fun recreateSpeechRecognizer() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        setUpData() // tạo lại SpeechRecognizer mới
    }
}