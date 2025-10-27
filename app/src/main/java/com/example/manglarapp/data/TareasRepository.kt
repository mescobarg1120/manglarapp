package com.example.manglarapp.data.repository

import com.example.manglarapp.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class TareasRepository {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas

    init {
        _tareas.value = listOf(
            Tarea("1", "Cocina Principal", 3, 3),
            Tarea("2", "Comedor", 3, 3),
            Tarea("3", "Patio", 5, 5),
            Tarea("4", "Baño 1", 3, 3),
            Tarea("5", "Baño 2", 5, 5),
            Tarea("6", "Teatrito", 3, 3),
            Tarea("7", "Pasillo", 5, 5),
            Tarea("8", "Feria", 6, 6)
        )
    }

    fun tomarTarea(tareaId: String, dia: DiaSemana, usuario: Usuario) {
        val tareaIndex = _tareas.value.indexOfFirst { it.id == tareaId }
        if (tareaIndex != -1) {
            val tarea = _tareas.value[tareaIndex]
            val tareasTomadasCount = tarea.asignaciones.values.filterNotNull().size

            if (tareasTomadasCount < tarea.disponibilidad) {
                val asignacion = AsignacionTarea(
                    usuarioId = usuario.id,
                    usuarioNombre = usuario.nombre,
                    estado = EstadoAsignacion.TOMADA
                )
                tarea.asignaciones[dia] = asignacion
                _tareas.value = _tareas.value.toMutableList().apply {
                    this[tareaIndex] = tarea
                }
            }
        }
    }

    fun completarTarea(tareaId: String, dia: DiaSemana, fotoUri: String) {
        val tareaIndex = _tareas.value.indexOfFirst { it.id == tareaId }
        if (tareaIndex != -1) {
            val tarea = _tareas.value[tareaIndex]
            val asignacion = tarea.asignaciones[dia]

            if (asignacion != null) {
                tarea.asignaciones[dia] = asignacion.copy(
                    estado = EstadoAsignacion.PENDIENTE_APROBACION,
                    fotoConfirmacion = fotoUri,
                    fechaCompletada = System.currentTimeMillis()
                )
                _tareas.value = _tareas.value.toMutableList().apply {
                    this[tareaIndex] = tarea
                }
            }
        }
    }

    fun aprobarTarea(tareaId: String, dia: DiaSemana) {
        val tareaIndex = _tareas.value.indexOfFirst { it.id == tareaId }
        if (tareaIndex != -1) {
            val tarea = _tareas.value[tareaIndex]
            val asignacion = tarea.asignaciones[dia]

            if (asignacion != null && asignacion.estado == EstadoAsignacion.PENDIENTE_APROBACION) {
                tarea.asignaciones[dia] = asignacion.copy(
                    estado = EstadoAsignacion.APROBADA,
                    fechaAprobada = System.currentTimeMillis()
                )
                _tareas.value = _tareas.value.toMutableList().apply {
                    this[tareaIndex] = tarea
                }
            }
        }
    }

    fun rechazarTarea(tareaId: String, dia: DiaSemana, comentario: String) {
        val tareaIndex = _tareas.value.indexOfFirst { it.id == tareaId }
        if (tareaIndex != -1) {
            val tarea = _tareas.value[tareaIndex]
            val asignacion = tarea.asignaciones[dia]

            if (asignacion != null && asignacion.estado == EstadoAsignacion.PENDIENTE_APROBACION) {
                tarea.asignaciones[dia] = asignacion.copy(
                    estado = EstadoAsignacion.RECHAZADA,
                    comentarioRechazo = comentario,
                    fotoConfirmacion = null
                )
                _tareas.value = _tareas.value.toMutableList().apply {
                    this[tareaIndex] = tarea
                }
            }
        }
    }

    fun liberarTarea(tareaId: String, dia: DiaSemana) {
        val tareaIndex = _tareas.value.indexOfFirst { it.id == tareaId }
        if (tareaIndex != -1) {
            val tarea = _tareas.value[tareaIndex]
            tarea.asignaciones.remove(dia)
            _tareas.value = _tareas.value.toMutableList().apply {
                this[tareaIndex] = tarea
            }
        }
    }

    fun obtenerTareasPendientesAprobacion(): List<Pair<Tarea, Pair<DiaSemana, AsignacionTarea>>> {
        val pendientes = mutableListOf<Pair<Tarea, Pair<DiaSemana, AsignacionTarea>>>()

        _tareas.value.forEach { tarea ->
            tarea.asignaciones.forEach { (dia, asignacion) ->
                if (asignacion?.estado == EstadoAsignacion.PENDIENTE_APROBACION) {
                    pendientes.add(Pair(tarea, Pair(dia, asignacion)))
                }
            }
        }

        return pendientes
    }
}