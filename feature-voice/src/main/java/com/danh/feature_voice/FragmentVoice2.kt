package com.danh.feature_voice

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
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
import androidx.annotation.OptIn
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
import com.danh.myapplication.data.TokenManager

class FragmentVoice2 : Fragment() {
    private lateinit var binding: FragmentVoiceBinding
    private var videoPlayer: ExoPlayer? = null
    private var audioPlayer: ExoPlayer? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var lastText = ""
    private val handler = Handler(Looper.getMainLooper())
    private var mediaPlayer: MediaPlayer? = null
    private var audioPlayerListener: Player.Listener? = null
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
        binding = FragmentVoiceBinding.inflate(inflater, container, false)
        return binding.root
    }

    @UnstableApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initVideoPlayer()
        setWebSocket()
        initSpeechRecognizer()
        setUpViewHelloOrBye("aihi")
    }
    private fun setUpViewHelloOrBye(fileName:String){

        val resId = resources.getIdentifier(fileName, "raw", requireContext().packageName)
        setUpIconVoice()
        binding.viewBlockTouch.visibility = View.VISIBLE
        mediaPlayer= MediaPlayer.create(requireContext(),resId)
        handler.postDelayed({
            if (!isAdded) return@postDelayed
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener {
                it.release()
                mediaPlayer = null
                binding.viewBlockTouch.visibility = View.GONE
                if (isAdded) {
                    isListening=false
                    setUpViewWait()
                }
            }
        }, 900)
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

    @UnstableApi
    private fun initAudioAi() {
        if (audioPlayer == null) {
            val loadControl = DefaultLoadControl.Builder()
                .setBufferDurationsMs(
                    1000,   // minBufferMs — chỉ cần 1 giây là phát
                    10000,  // maxBufferMs
                    500,    // bufferForPlaybackMs — chỉ cần 500ms để bắt đầu
                    500     // bufferForPlaybackAfterRebufferMs
                )
                .build()
            audioPlayer = ExoPlayer.Builder(requireContext()).setLoadControl(loadControl).build()
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
                    activity?.runOnUiThread {
                        if (!isAdded || view == null) return@runOnUiThread
                    }
                },
                onMessage = { message ->
                    activity?.runOnUiThread {
                        if (!isAdded || view == null) return@runOnUiThread
                    }
                },
                onReceiveText = { type, text, audioUrl ->
                    activity?.runOnUiThread {
                        if (!isAdded || view == null) return@runOnUiThread
                        receiveText(type, text, audioUrl)
                    }
                },
                onClosed = { code, reason ->
                    activity?.runOnUiThread {
                        if (!isAdded || view == null) return@runOnUiThread
                    }
                },
                onFailure = { error ->
                    activity?.runOnUiThread {
                        if (!isAdded || view == null) return@runOnUiThread
                    }
                }
            )
        }
        binding.playerView.setOnClickListener {
            if (audioPlayer?.isPlaying == true) {
                stopAudioStream()
                startListening()
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

    private fun stopAudioStream() {
        audioPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
        }
        audioPlayer = null
    }

    @UnstableApi
    private fun receiveText(type: String?, text: String?, audioUrl: String?) {
        if (!audioUrl.isNullOrEmpty()) {
            playAudioStream(audioUrl)
        }
    }

    @UnstableApi
    private fun playAudioStream(url: String) {
        initAudioAi()
        audioPlayerListener?.let { audioPlayer?.removeListener(it) }
        audioPlayerListener = object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> {
                        Log.d("AudioStream", "STATE_BUFFERING")
                    }
                    Player.STATE_READY -> {
                        Log.d("AudioStream", "STATE_READY")
                    }
                    Player.STATE_ENDED -> {
                        stopAudioStream()
                        setUpViewWait()
                    }
                    Player.STATE_IDLE -> {
                        Log.d("AudioStream", "STATE_IDLE")
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                if (isPlaying) {
                    isListening=false
                    setUpIconVoice()
                }
            }

            override fun onPlayerError(error: PlaybackException) {
                Log.e("AudioStream", "Lỗi phát audio stream", error)
                Toast.makeText(
                    requireContext(),
                    "Không phát được audio",
                    Toast.LENGTH_SHORT
                ).show()
                stopAudioStream()
            }
        }

        audioPlayer?.also { exoPlayer ->
            exoPlayer.addListener(audioPlayerListener!!)
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
        }
    }

    private fun initSpeechRecognizer() {
        if (speechRecognizer == null) {
            speechRecognizer =
                SpeechRecognizer.createSpeechRecognizer(requireContext()).also { recognizer ->
                    recognizer.setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                        }

                        override fun onBeginningOfSpeech() {
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                        }

                        override fun onError(error: Int) {
                            webSocketManager.disConnectUser( "VI", "giongnuhanoi", 112233, 2.5f)
                            stopAudioStream()
                            stopListening()
                            setUpViewHelloOrBye("byeai")
                        }

                        override fun onResults(results: Bundle?) {
                            val matches =
                                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            Log.d("Speech", "onResults: $matches")
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val texts =
                                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val newText = texts?.firstOrNull()?.trim().orEmpty()
                            if (newText.isNotEmpty() && newText != lastText) {
                                lastText = newText
                                val numberText=lastText.length.toString()
                                Log.d("result", lastText.length.toString())
                                if(numberText.toInt()<30){
                                    resetNoNewTextTimer(1000)
                                }else{
                                    resetNoNewTextTimer(2000)
                                }
                            }
                            Log.d("result",lastText)
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }


        }
    }

    private fun startListening() {
        if (isListening) return
        initSpeechRecognizer()
        setUpIconListen()
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "vi-VN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        isListening = true
        speechRecognizer?.startListening(intent)

    }

    private fun resetNoNewTextTimer(time:Long) {
        handler.removeCallbacks(stopBecauseNoNewText)
        handler.postDelayed(stopBecauseNoNewText, time)
    }

    private fun stopListening() {
        if (isListening && speechRecognizer != null) {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        }
      //  isListening = false
        handler.removeCallbacks(stopBecauseNoNewText)
        Log.d("lasttext", lastText)

        if (lastText.isNotEmpty()) {
            val textToSend = lastText
            lastText = ""
            webSocketManager.sendText(textToSend, "VI", "giongnuhanoi", 112233, 2.5f)
            Log.d("result", "Dữ liệu được gửi lên server: " + textToSend)
            Log.d("result", "lasttext ${lastText}")
        }
    }

    override fun onDestroy() {
        isListening = false;
        if (speechRecognizer != null) {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
            speechRecognizer?.destroy()
            speechRecognizer = null
        }

        super.onDestroy()
    }

    override fun onDestroyView() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null

        audioPlayer?.release()
        audioPlayer = null

        videoPlayer?.release()
        videoPlayer = null
        mediaPlayer?.release()
        mediaPlayer=null
        webSocketManager.disconnect()
        handler.removeCallbacksAndMessages(null)
        binding.playerView.player = null
        super.onDestroyView()
    }
}