package com.danh.main


import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.danh.main.databinding.FragmentMainBinding
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.ByteOrder

class FragmentMain : Fragment() {
    private lateinit var micNoiseFilterPlayer: MicNoiseFilterPlayer
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!

    private var mediaRecorder: MediaRecorder? = null
    private var mediaPlayer: MediaPlayer? = null
    private var audioFilePath: String = ""

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                startRecording()
            } else {
                Toast.makeText(requireContext(), "Bạn đã từ chối quyền ghi âm", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        micNoiseFilterPlayer = MicNoiseFilterPlayer(requireContext())
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        audioFilePath = "${requireContext().externalCacheDir?.absolutePath}/voice_record.3gp"

        binding.btnStartRecord.setOnClickListener {
            if (hasRecordPermission()) {
                startRecording()
            } else {
                requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }

        binding.btnStopRecord.setOnClickListener {
            stopRecording()
        }

        binding.btnPlay.setOnClickListener {
            playRecording()
        }
    }

    private fun hasRecordPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startRecording() {
        try {
            mediaRecorder?.release()
            mediaRecorder = MediaRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(audioFilePath)
                prepare()
                start()
            }

            binding.txtStatus.text = "Đang ghi âm..."
            Toast.makeText(requireContext(), "Bắt đầu ghi âm", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi khi ghi âm", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Không thể bắt đầu ghi âm", Toast.LENGTH_SHORT).show()
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            binding.txtStatus.text = "Đã dừng ghi âm"
            Toast.makeText(requireContext(), "Đã lưu file ghi âm", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Chưa ghi âm hoặc lỗi dừng ghi âm", Toast.LENGTH_SHORT).show()
        }
    }
    val deepFilterNet = com.rikorose.deepfilternet.NativeDeepFilterNet(requireContext())
    private fun playRecording() {
        deepFilterNet.setAttenuationLimit(30f)
        val frameLength = deepFilterNet.frameLength.toInt()
// Allocate a new direct ByteBuffer with the given frame length to interact with the native code.
        val byteBuffer = ByteBuffer.allocateDirect(frameLength).apply {
            order(ByteOrder.LITTLE_ENDIAN) // Set byte order to match DeepFilterNet's expectation
        }

        deepFilterNet.processFrame(byteBuffer)
        try {
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioFilePath)
                prepare()
                start()
            }
            binding.txtStatus.text = "Đang phát lại..."
            Toast.makeText(requireContext(), "Đang phát", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Không phát được file", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Lỗi phát âm thanh", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        mediaRecorder?.release()
        mediaRecorder = null

        mediaPlayer?.release()
        mediaPlayer = null

        _binding = null
    }
}


