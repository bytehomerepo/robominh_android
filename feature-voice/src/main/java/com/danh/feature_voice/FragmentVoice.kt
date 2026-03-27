package com.danh.feature_voice

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.danh.feature_voice.databinding.FragmentVoiceBinding
import androidx.core.net.toUri

class FragmentVoice : Fragment() {
    private lateinit var binding: FragmentVoiceBinding
    private var player: ExoPlayer?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=FragmentVoiceBinding.inflate(inflater,container,false)
        return binding.root
    }
    private lateinit var videoUrl: Uri
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        binding.playerView.setOnClickListener {
            if(videoUrl=="android.resource://${requireContext().packageName}/${R.raw.listen}".toUri()){
                videoUrl = "android.resource://${requireContext().packageName}/${R.raw.voice}".toUri()
                player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
                    binding.playerView.player = exoPlayer
                    exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ALL   // lặp lại
                    exoPlayer.playWhenReady = true                  // vào app là phát
                    exoPlayer.prepare()
                }
            }else{
                videoUrl = "android.resource://${requireContext().packageName}/${R.raw.listen}".toUri()
                player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
                    binding.playerView.player = exoPlayer
                    exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
                    exoPlayer.repeatMode = Player.REPEAT_MODE_ALL   // lặp lại
                    exoPlayer.playWhenReady = true                  // vào app là phát
                    exoPlayer.prepare()
                }
            }

        }
    }
    private fun setUpViews(){
        videoUrl = "android.resource://${requireContext().packageName}/${R.raw.listen}".toUri()
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            binding.playerView.player = exoPlayer
            exoPlayer.setMediaItem(MediaItem.fromUri(videoUrl))
            exoPlayer.repeatMode = Player.REPEAT_MODE_ALL   // lặp lại
            exoPlayer.playWhenReady = true                  // vào app là phát
            exoPlayer.prepare()
        }
    }
}