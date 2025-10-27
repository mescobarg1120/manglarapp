package com.example.manglarapp.domain.model

import java.util.UUID

data class Tarea(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val disponibilidad: Int,
    val puntos: Int,
    val reglas: String = "",
    val asignaciones: MutableMap<DiaSemana, AsignacionTarea?> = mutableMapOf()
)

data class AsignacionTarea(
    val usuarioId: String,
    val usuarioNombre: String,
    val estado: EstadoAsignacion = EstadoAsignacion.TOMADA,
    val fotoConfirmacion: String? = null,
    val fechaAsignacion: Long = System.currentTimeMillis(),
    val fechaCompletada: Long? = null,
    val fechaAprobada: Long? = null,
    val comentarioRechazo: String? = null
)

enum class EstadoAsignacion {
    TOMADA,              // Usuario tomó la tarea
    PENDIENTE_APROBACION, // Usuario subió foto, esperando admin
    APROBADA,            // Admin aprobó la tarea
    RECHAZADA            // Admin rechazó la tarea
}

enum class DiaSemana {
    LUNES, MARTES, MIERCOLES, JUEVES, VIERNES, SABADO, DOMINGO
}