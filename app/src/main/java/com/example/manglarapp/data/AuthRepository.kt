package com.example.manglarapp.data

import com.example.manglarapp.model.Usuario
import com.example.manglarapp.network.NetworkResult
import com.example.manglarapp.network.RetrofitClient
import com.example.manglarapp.network.dto.LoginRequest
import com.example.manglarapp.network.dto.toUsuario
import com.example.manglarapp.network.safeApiCall

class AuthRepository {

    private val authApi = RetrofitClient.authApi

    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            // ✅ Crear fuera del safeApiCall
            val loginRequest = LoginRequest(email, password)

            val result = safeApiCall(
                apiCall = { authApi.login(loginRequest) },
                transform = { it.toUsuario() }  // ✅ "it" es el LoginResponse
            )

            when (result) {
                is NetworkResult.Success -> Result.success(result.data)
                is NetworkResult.Error -> Result.failure(Exception(result.message))
                is NetworkResult.Loading -> Result.failure(Exception("Operación en progreso"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}