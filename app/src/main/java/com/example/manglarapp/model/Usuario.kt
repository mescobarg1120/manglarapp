package com.example.manglarapp.domain.model

data class Usuario(
    val id: String,
    val nombre: String,
    val rol: RolUsuario,
    val password: String = "12345" // Solo para prototipo
)

enum class RolUsuario {
    ADMIN, ARRENDATARIO
}