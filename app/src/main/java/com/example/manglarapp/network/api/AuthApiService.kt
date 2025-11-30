package com.example.manglarapp.network.api

import com.example.manglarapp.network.dto.LoginRequest
import com.example.manglarapp.network.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}