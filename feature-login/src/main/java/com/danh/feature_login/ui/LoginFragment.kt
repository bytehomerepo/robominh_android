package com.danh.feature_login.ui

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.danh.feature_login.databinding.FragmentLoginBinding
import com.danh.feature_login.viewmodel.LoginViewModel
import com.danh.feature_login.R
import com.danh.main.FragmentMain
import androidx.core.net.toUri
import androidx.fragment.app.viewModels

class LoginFragment : Fragment() {
    private val viewModel: LoginViewModel by viewModels()
    private lateinit var binding: FragmentLoginBinding;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupLogin()
        observeLoginResult()
    }

    private fun setupLogin() {
        binding.btnLogin.setOnClickListener {
            val userName = binding.editUser.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            if (userName.isEmpty() || password.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    "Vui lòng nhập đầy đủ tài khoản và mật khẩu",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            viewModel.login(userName, password)
        }
    }

    private fun observeLoginResult() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                true -> {
                    findNavController().navigate("myapp://main".toUri())
                }

                false -> {
                    Toast.makeText(requireContext(), "Đăng nhập thất bại", Toast.LENGTH_SHORT)
                        .show()
                }

                null -> {
                    Toast.makeText(
                        requireContext(),
                        "Tài khoản hoặc mật khẩu không đúng",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
