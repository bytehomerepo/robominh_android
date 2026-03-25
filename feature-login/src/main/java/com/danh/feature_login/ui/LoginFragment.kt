package com.danh.feature_login.ui

import kotlinx.coroutines.delay
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.danh.feature_login.databinding.FragmentLoginBinding
import com.danh.feature_login.viewmodel.LoginViewModel
import androidx.core.net.toUri
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {
    private var loginStartTime = 0L
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
            loginStartTime = System.currentTimeMillis()
            showLoading(true)
            viewModel.login(userName, password)
        }
    }
    private fun showLoading(isLoading: Boolean) {
        binding.loadingOverlay.visibility = if (isLoading) View.VISIBLE else View.GONE

//        binding.btnLogin.isEnabled = !isLoading
//        binding.editUser.isEnabled = !isLoading
//        binding.editPassword.isEnabled = !isLoading

        binding.loginContent.alpha = if (isLoading) 0.7f else 1f
    }
    private fun observeLoginResult() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            viewLifecycleOwner.lifecycleScope.launch {
                when (result) {
                    true -> {
                        val elapsed = System.currentTimeMillis() - loginStartTime
                        val remain = 1500L - elapsed

                        if (remain > 0) {
                            delay(remain)
                            showLoading(true)
                        }

                        showLoading(false)
                        findNavController().navigate("myapp://main".toUri())
                    }

                    false -> {
                        showLoading(false)
                        Toast.makeText(
                            requireContext(),
                            "Đăng nhập thất bại",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    null -> {
                        showLoading(false)
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
}
