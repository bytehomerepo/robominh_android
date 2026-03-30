package com.danh.main

import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.danh.main.databinding.FragmentMainBinding


class FragmentMain : Fragment() {
    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

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
}
