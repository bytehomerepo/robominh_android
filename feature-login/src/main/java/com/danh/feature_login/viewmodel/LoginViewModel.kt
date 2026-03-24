package com.danh.feature_login.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.danh.core_network.model.Login
import com.danh.core_network.model.pagingRequest.LoginRequest
import com.danh.core_network.repository.login.LoginRepository
import com.danh.core_network.repository.login.LoginRepositoryImpl
import com.danh.core_network.resource.Result
import com.danh.core_network.resource.login.LoginApi
import kotlinx.coroutines.launch

class LoginViewModel(private val loginRepository: LoginRepository = LoginRepositoryImpl()) :
    ViewModel() {
    private val _loginResult = MutableLiveData<Boolean?>()
    val loginResult: MutableLiveData<Boolean?> = _loginResult
     fun login(userName: String, password: String) {
        viewModelScope.launch {
            val response = loginRepository.authLogin(userName, password)
            if (response is Result.Success) {
                _loginResult.value = response.data.success
            } else {
                _loginResult.value = null;
            }
        }
    }
}