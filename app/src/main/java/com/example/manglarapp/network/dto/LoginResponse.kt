package com.example.manglarapp.network.dto

import com.example.manglarapp.model.EstadoUsuario
import com.example.manglarapp.model.RolUsuario
import com.example.manglarapp.model.Usuario

data class LoginResponse(
    val token: String,
    val rut: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val estado: String
)
    // ✅ Función de extensión para convertir LoginResponse a Usuario
    fun LoginResponse.toUsuario(): Usuario {
        return Usuario(
            rut = rut,
            nombre = nombre,
            email = email,
            rol = when (rol) {
                "ADMINISTRADOR" -> RolUsuario.ADMINISTRADOR
                "ARRENDATARIO" -> RolUsuario.ARRENDATARIO
                "SUPERVISOR" -> RolUsuario.SUPERVISOR
                else -> RolUsuario.ARRENDATARIO
            },
            estado = when (estado) {
                "ACTIVO" -> EstadoUsuario.ACTIVO
                "INACTIVO" -> EstadoUsuario.INACTIVO
                "BLOQUEADO" -> EstadoUsuario.BLOQUEADO
                else -> EstadoUsuario.ACTIVO
            },
            password = "" // No se devuelve el password desde el login
        )
    }
