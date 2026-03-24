package com.danh.core_network.repository.login

import com.danh.core_network.model.Login
import com.danh.core_network.resource.Result

interface LoginRepository {
    suspend fun authLogin( userName:String, password:String): Result<Login>
}