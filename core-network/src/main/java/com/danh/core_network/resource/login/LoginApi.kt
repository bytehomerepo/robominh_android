package com.danh.core_network.resource.login

import com.danh.core_network.model.Login
import com.danh.core_network.model.pagingRequest.LoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface LoginApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<Login>
}