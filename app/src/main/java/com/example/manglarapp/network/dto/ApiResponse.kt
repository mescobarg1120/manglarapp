package com.example.manglarapp.network.dto

/**
 * Respuesta genérica de la API
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val error: String? = null
)

/**
 * Respuesta para login
 */

