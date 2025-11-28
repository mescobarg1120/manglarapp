package com.example.manglarapp.network.dto

import com.example.manglarapp.model.EstadoUsuario
import com.example.manglarapp.model.RolUsuario
import com.example.manglarapp.model.Usuario

/**
 * DTO para Usuario - usado para comunicación con la API
 */
data class UsuarioDto(
    val rut: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val estado: String,
    val password: String? = null
)

/**
 * Request para crear/actualizar usuario
 */
data class UsuarioRequest(
    val rut: String,
    val nombre: String,
    val email: String,
    val rol: String,
    val estado: String,
    val password: String
)

/**
 * Extensiones para convertir entre DTO y modelo de dominio
 */
fun UsuarioDto.toUsuario(): Usuario {
    return Usuario(
        rut = rut,
        nombre = nombre,
        email = email,
        rol = when (rol.uppercase()) {
            "ADMINISTRADOR" -> RolUsuario.ADMINISTRADOR
            "ARRENDATARIO" -> RolUsuario.ARRENDATARIO
            "SUPERVISOR" -> RolUsuario.SUPERVISOR
            else -> RolUsuario.ARRENDATARIO
        },
        estado = when (estado.uppercase()) {
            "ACTIVO" -> EstadoUsuario.ACTIVO
            "INACTIVO" -> EstadoUsuario.INACTIVO
            "BLOQUEADO" -> EstadoUsuario.BLOQUEADO
            else -> EstadoUsuario.ACTIVO
        },
        password = password ?: ""
    )
}

fun Usuario.toDto(): UsuarioDto {
    return UsuarioDto(
        rut = rut,
        nombre = nombre,
        email = email,
        rol = rol.name,
        estado = estado.name,
        password = password
    )
}

fun Usuario.toUsuarioRequest(): UsuarioRequest {
    return UsuarioRequest(
        rut = rut,
        nombre = nombre,
        email = email,
        rol = rol.name,
        estado = estado.name,
        password = password
    )
}
