package com.example.manglarapp.network

import com.example.manglarapp.network.api.AuthApiService
import com.example.manglarapp.network.api.FinanzasApiService
import com.example.manglarapp.network.api.TareasApiService
import com.example.manglarapp.network.api.UsuariosApiService
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private val gson = GsonBuilder()
        .setLenient()
        .create()

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(ApiConstants.BASE_URL)
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
}
