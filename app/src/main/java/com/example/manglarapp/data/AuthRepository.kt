package com.example.manglarapp.data

import com.example.manglarapp.model.Usuario
import com.example.manglarapp.network.NetworkResult
import com.example.manglarapp.network.RetrofitClient
import com.example.manglarapp.network.dto.LoginRequest
import com.example.manglarapp.network.dto.toUsuario
import com.example.manglarapp.network.safeApiCall

class AuthRepository {

    private val authApi = RetrofitClient.authApi
    private val tokenManager = RetrofitClient.getTokenManager()

    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val loginRequest = LoginRequest(email, password)

            val result = safeApiCall(
                apiCall = { authApi.login(loginRequest) },
                transform = { it }  // Devolver LoginResponse completo
            )

            when (result) {
                is NetworkResult.Success -> {
                    // ✅ Guardar el token
                    tokenManager?.saveToken(result.data.token)

                    val tokenGuardado = tokenManager?.getToken()
                    println("🟢 Token guardado verificado: ${if (tokenGuardado != null) "SÍ" else "NO"}")

                    // Convertir a Usuario
                    val usuario = result.data.toUsuario()
                    Result.success(usuario)
                }
                is NetworkResult.Error -> Result.failure(Exception(result.message))
                is NetworkResult.Loading -> Result.failure(Exception("Operación en progreso"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        tokenManager?.clearToken()
    }

    fun isLoggedIn(): Boolean {
        return tokenManager?.isLoggedIn() ?: false
    }
}