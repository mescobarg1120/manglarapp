package com.example.manglarapp.network.dto

import com.example.manglarapp.domain.model.*

/**
 * DTO para Tarea
 */
data class TareaDto(
    val id: String,
    val nombre: String,
    val disponibilidad: Int,
    val puntos: Int,
    val reglas: String? = null,
    val asignaciones: Map<String, AsignacionTareaDto>? = null
)

/**
 * DTO para AsignacionTarea
 */
data class AsignacionTareaDto(
    val usuarioId: String,
    val usuarioNombre: String,
    val estado: String,
    val fotoConfirmacion: String? = null,
    val fechaAsignacion: Long,
    val fechaCompletada: Long? = null,
    val fechaAprobada: Long? = null,
    val comentarioRechazo: String? = null
)

/**
 * Request para tomar una tarea
 */
data class TomarTareaRequest(
    val tareaId: String,
    val dia: String,
    val usuarioId: String,
    val usuarioNombre: String
)

/**
 * Request para completar una tarea
 */
data class CompletarTareaRequest(
    val tareaId: String,
    val dia: String,
    val fotoUri: String
)

/**
 * Request para aprobar una tarea
 */
data class AprobarTareaRequest(
    val tareaId: String,
    val dia: String
)

/**
 * Request para rechazar una tarea
 */
data class RechazarTareaRequest(
    val tareaId: String,
    val dia: String,
    val comentario: String
)

/**
 * Request para liberar una tarea
 */
data class LiberarTareaRequest(
    val tareaId: String,
    val dia: String
)

/**
 * Extensiones para convertir entre DTO y modelo de dominio
 */
fun TareaDto.toTarea(): Tarea {
    return Tarea(
        id = id,
        nombre = nombre,
        disponibilidad = disponibilidad,
        puntos = puntos,
        reglas = reglas ?: "",
        asignaciones = asignaciones?.mapKeys { entry ->
            DiaSemana.valueOf(entry.key.uppercase())
        }?.mapValues { entry ->
            entry.value.toAsignacionTarea()
        } ?: emptyMap()
    )
}

fun AsignacionTareaDto.toAsignacionTarea(): AsignacionTarea {
    return AsignacionTarea(
        usuarioId = usuarioId,
        usuarioNombre = usuarioNombre,
        estado = when (estado.uppercase()) {
            "TOMADA" -> EstadoAsignacion.TOMADA
            "PENDIENTE_APROBACION" -> EstadoAsignacion.PENDIENTE_APROBACION
            "APROBADA" -> EstadoAsignacion.APROBADA
            "RECHAZADA" -> EstadoAsignacion.RECHAZADA
            else -> EstadoAsignacion.TOMADA
        },
        fotoConfirmacion = fotoConfirmacion,
        fechaAsignacion = fechaAsignacion,
        fechaCompletada = fechaCompletada,
        fechaAprobada = fechaAprobada,
        comentarioRechazo = comentarioRechazo
    )
}

fun Tarea.toTareaDto(): TareaDto {
    return TareaDto(
        id = id,
        nombre = nombre,
        disponibilidad = disponibilidad,
        puntos = puntos,
        reglas = reglas,
        asignaciones = asignaciones.mapKeys { it.key.name }
            .mapValues { it.value.toAsignacionTareaDto() }
    )
}

fun AsignacionTarea.toAsignacionTareaDto(): AsignacionTareaDto {
    return AsignacionTareaDto(
        usuarioId = usuarioId,
        usuarioNombre = usuarioNombre,
        estado = estado.name,
        fotoConfirmacion = fotoConfirmacion,
        fechaAsignacion = fechaAsignacion,
        fechaCompletada = fechaCompletada,
        fechaAprobada = fechaAprobada,
        comentarioRechazo = comentarioRechazo
    )
}
