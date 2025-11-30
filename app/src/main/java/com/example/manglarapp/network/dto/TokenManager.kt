package com.example.manglarapp.data

import android.content.Context
import android.content.SharedPreferences

class TokenManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("manglar_auth", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_TOKEN = "jwt_token"

        @Volatile
        private var INSTANCE: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }

    fun saveToken(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()

        // Verificar que se guardó
        val verificado = prefs.getString(KEY_TOKEN, null)
        println("🟢 Token guardado y verificado: ${if (verificado != null) "SÍ" else "NO"}")
    }

    fun getToken(): String? {
        val token = prefs.getString(KEY_TOKEN, null)
        println("🔵 TokenManager.getToken() - Token: ${if (token != null) token.substring(0, 20) + "..." else "NULL"}")
        return token
    }

    fun clearToken() {
        println("🔴 TokenManager.clearToken() - Eliminando token")
        prefs.edit().remove(KEY_TOKEN).apply()
    }

    fun isLoggedIn(): Boolean {
        val loggedIn = getToken() != null
        println("🔵 TokenManager.isLoggedIn() - ¿Logueado?: $loggedIn")
        return loggedIn
    }
}