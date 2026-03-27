package com.danh.main

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.danh.core_network.data.WebSocketManager
import com.danh.main.databinding.FragmentMainBinding
import com.danh.myapplication.data.TokenManager
import kotlinx.coroutines.launch

class FragmentMain : Fragment() {

    private lateinit var binding: FragmentMainBinding
    private lateinit var webSocketManager: WebSocketManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
        setWebSocket()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    private fun setUpViews(){
        binding.btnVoice.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentVoice)

        }
        binding.btnSetting.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentSetting)
        }
    }
    private fun setWebSocket(){
        webSocketManager = WebSocketManager()

        viewLifecycleOwner.lifecycleScope.launch {
            val token = TokenManager(requireContext().applicationContext).getToken()

            Log.d("TOKEN_MAIN", "token = $token")

            webSocketManager.connect(
                url = "ws://192.168.1.12:3000?token=$token",
                token = token,
                onConnected = {
                    requireActivity().runOnUiThread {
                        binding.txtToken?.text = "Đã kết nối WebSocket"
                    }
                },
                onMessage = { message ->
                    requireActivity().runOnUiThread {
                        binding.txtToken?.text = message
                    }
                },
                onClosed = { code, reason ->
                    requireActivity().runOnUiThread {
                        binding.txtToken?.text = "Đóng kết nối: $code - $reason"
                    }
                },
                onFailure = { error ->
                    requireActivity().runOnUiThread {
                        binding.txtToken?.text = "Lỗi: ${error.message}"
                    }
                }
            )
        }

        binding.txtToken?.setOnClickListener {
            webSocketManager.sendText("Hello from ByteHome","Nam",123L,12.12)
        }
    }
}
