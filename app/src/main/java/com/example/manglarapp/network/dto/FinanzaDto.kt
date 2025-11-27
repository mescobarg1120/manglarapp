package com.example.manglarapp.network.dto

import com.example.manglarapp.model.Finanza
import com.example.manglarapp.model.TipoFinanza
import com.example.manglarapp.model.UsuarioFinanza

/**
 * DTO para Finanza
 */
data class FinanzaDto(
    val id: Int,
    val usuarioId: String,
    val tipo: String,
    val monto: Double,
    val descripcion: String,
    val fecha: String,
    val categoria: String
)

/**
 * DTO para UsuarioFinanza
 */
data class UsuarioFinanzaDto(
    val usuarioId: String,
    val nombre: String,
    val puntos: Int,
    val totalAbono: Double,
    val totalDeuda: Double
)

/**
 * Request para crear/actualizar finanza
 */
data class FinanzaRequest(
    val usuarioId: String,
    val tipo: String,
    val monto: Double,
    val descripcion: String,
    val fecha: String,
    val categoria: String
)

/**
 * Request para actualizar abono de usuario
 */
data class ActualizarAbonoRequest(
    val usuarioId: String,
    val nuevoAbono: Double
)

/**
 * Extensiones para convertir entre DTO y modelo de dominio
 */
fun FinanzaDto.toFinanza(): Finanza {
    return Finanza(
        id = id,
        usuarioId = usuarioId,
        tipo = when (tipo.uppercase()) {
            "INGRESO" -> TipoFinanza.INGRESO
            "EGRESO" -> TipoFinanza.EGRESO
            else -> TipoFinanza.INGRESO
        },
        monto = monto,
        descripcion = descripcion,
        fecha = fecha,
        categoria = categoria
    )
}

fun Finanza.toFinanzaDto(): FinanzaDto {
    return FinanzaDto(
        id = id,
        usuarioId = usuarioId,
        tipo = tipo.name,
        monto = monto,
        descripcion = descripcion,
        fecha = fecha,
        categoria = categoria
    )
}

fun Finanza.toFinanzaRequest(): FinanzaRequest {
    return FinanzaRequest(
        usuarioId = usuarioId,
        tipo = tipo.name,
        monto = monto,
        descripcion = descripcion,
        fecha = fecha,
        categoria = categoria
    )
}

fun UsuarioFinanzaDto.toUsuarioFinanza(): UsuarioFinanza {
    return UsuarioFinanza(
        usuarioId = usuarioId,
        nombre = nombre,
        puntos = puntos,
        totalAbono = totalAbono,
        totalDeuda = totalDeuda
    )
}

fun UsuarioFinanza.toUsuarioFinanzaDto(): UsuarioFinanzaDto {
    return UsuarioFinanzaDto(
        usuarioId = usuarioId,
        nombre = nombre,
        puntos = puntos,
        totalAbono = totalAbono,
        totalDeuda = totalDeuda
    )
}
