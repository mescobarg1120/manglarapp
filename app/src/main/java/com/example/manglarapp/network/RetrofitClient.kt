package com.example.manglarapp.network

import com.example.manglarapp.network.api.FinanzasApiService
import com.example.manglarapp.network.api.TareasApiService
import com.example.manglarapp.network.api.UsuariosApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente Retrofit singleton para manejar todas las llamadas a la API
 */
object RetrofitClient {

    /**
     * Configuración de Gson para serialización/deserialización JSON
     */
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }

    /**
     * Interceptor para logging de peticiones HTTP
     */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    /**
     * Cliente OkHttp con configuración de timeouts e interceptores
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(ApiConstants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(ApiConstants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(ApiConstants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    /**
     * Instancia de Retrofit
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    /**
     * Servicio API para operaciones de usuarios
     */
    val usuariosApi: UsuariosApiService by lazy {
        retrofit.create(UsuariosApiService::class.java)
    }

    /**
     * Servicio API para operaciones de tareas
     */
    val tareasApi: TareasApiService by lazy {
        retrofit.create(TareasApiService::class.java)
    }

    /**
     * Servicio API para operaciones de finanzas
     */
    val finanzasApi: FinanzasApiService by lazy {
        retrofit.create(FinanzasApiService::class.java)
    }
}
