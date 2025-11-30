package com.example.manglarapp.network

import android.content.Context
import com.example.manglarapp.data.TokenManager
import com.example.manglarapp.network.api.AuthApiService
import com.example.manglarapp.network.api.FinanzasApiService
import com.example.manglarapp.network.api.TareasApiService
import com.example.manglarapp.network.api.UsuariosApiService
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    private var tokenManager: TokenManager? = null

    // ✅ Llamar esto desde Application o MainActivity
    fun initialize(context: Context) {
        tokenManager = TokenManager.getInstance(context)
    }

    // Interceptor para logs (opcional pero útil)
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // ✅ Interceptor que agrega el token JWT automáticamente
    private val authInterceptor = Interceptor { chain ->
        val token = tokenManager?.getToken()

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        chain.proceed(request)
    }

    // ✅ OkHttpClient con los interceptores
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)  // ✅ Usar el cliente con interceptores
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    val authApi: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }

    val usuariosApi: UsuariosApiService by lazy {
        retrofit.create(UsuariosApiService::class.java)
    }


    val tareasApi: TareasApiService by lazy {
        retrofit.create(TareasApiService::class.java)
    }

    val finanzasApi: FinanzasApiService by lazy {
        retrofit.create(FinanzasApiService::class.java)
    }

    // ✅ Función helper para acceder al TokenManager
    fun getTokenManager(): TokenManager? = tokenManager
}
