package com.example.manglarapp

import android.app.Application
import com.example.manglarapp.network.RetrofitClient

class ManglarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        println("🟢 ManglarApp - Inicializando RetrofitClient...")
        RetrofitClient.initialize(this)
        println("🟢 ManglarApp - RetrofitClient inicializado")
    }
}