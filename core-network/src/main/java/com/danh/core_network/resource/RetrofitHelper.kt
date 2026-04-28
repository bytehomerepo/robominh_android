package com.danh.core_network.resource
import com.danh.core_network.resource.login.LoginApi
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
object RetrofitHelper {

    private val BASE_URL = "http://118.70.187.211:4000/"
//    private val BASE_URL="http://192.168.1.22:4000/"
    private val gson by lazy {
        GsonBuilder().create()
    }
    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
    }
    val useApi: LoginApi by lazy {
        retrofit.build().create(LoginApi::class.java)
    }
}