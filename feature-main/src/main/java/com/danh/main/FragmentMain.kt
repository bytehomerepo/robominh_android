package com.danh.main

import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.danh.main.databinding.FragmentMainBinding


class FragmentMain : Fragment() {
    private var _binding: FragmentMainBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }
    override fun onResume() {
        super.onResume()
        (activity as AppCompatActivity).supportActionBar?.hide()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View ?{

        _binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
    private fun setUpViews(){
        binding.btnVoice.setOnClickListener {
            val prefs = PreferenceManager.getDefaultSharedPreferences(requireContext())
            val optionMode = prefs.getString("option_mode","Trò chuyện liên tục")
            Log.d("option", optionMode.toString())
            if(optionMode.toString()=="Trò chuyện liên tục"){
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentVoice)
                Log.d("option", optionMode.toString())
            }
            else if(optionMode.toString()=="Tro chuyen si da"){
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentVoice3)
            }
            else{
                findNavController().navigate(R.id.action_fragmentMain_to_fragmentVoice2)
                Log.d("option", "sai")
            }

        }
        binding.btnSetting.setOnClickListener {
            findNavController().navigate(R.id.action_fragmentMain_to_fragmentSetting)
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
