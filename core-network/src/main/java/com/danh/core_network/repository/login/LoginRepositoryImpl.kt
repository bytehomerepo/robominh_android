package com.danh.core_network.repository.login

import android.util.Log
import com.danh.core_network.model.Login
import com.danh.core_network.model.pagingRequest.LoginRequest
import com.danh.core_network.resource.Result
import com.danh.core_network.resource.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepositoryImpl : LoginRepository {
    override suspend fun authLogin(
        userName: String,
        password: String
    ): Result<Login> {
        return withContext(Dispatchers.IO) {
            val request = LoginRequest(userName, password)
            val response = RetrofitHelper.useApi.login(request)
            if (response.isSuccessful) {
                val data = response.body()
                Log.d("LOGIN", "success body = $data")
                Result.Success(response.body()!!)
            } else {
                val errorText = response.errorBody()?.string()
                Log.d("LOGIN", "code = ${response.code()}, error = $errorText")
                Result.Error(Exception(response.errorBody().toString()))
            }
        }
    }

}