package com.example.manglarapp.data.repository

import com.example.manglarapp.domain.model.*
import com.example.manglarapp.viewmodel.UsuariosViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import com.example.manglarapp.model.Usuario
class TareasRepository {

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas

    init {
        _tareas.value = listOf(
            Tarea(
                id = "1",
                nombre = "Cocina Principal",
                disponibilidad = 3,
                puntos = 3,
                asignaciones = mapOf()
            ),
            Tarea(
                id = "2",
                nombre = "Comedor",
                disponibilidad = 3,
                puntos = 3,
                asignaciones = mapOf()
            ),
            Tarea(
                id = "3",
                nombre = "Patio",
                disponibilidad = 5,
                puntos = 5,
                asignaciones = mapOf()
            ),
            Tarea(
                id = "4",
                nombre = "Baño 1",
                disponibilidad = 3,
                puntos = 3,
                asignaciones = mapOf()
            ),
            Tarea(
                id = "5",
                nombre = "Baño 2",
                disponibilidad = 5,
                puntos = 5,
                asignaciones = mapOf()
            ),
            Tarea(
                id = "6",
                nombre = "Teatrito",
                disponibilidad = 3,
                puntos = 3,
                asignaciones = mapOf()
            ),
            Tarea(
                id = "7",
                nombre = "Pasillo",
                disponibilidad = 5,
                puntos = 5,
                asignaciones = mapOf()
            ),
            Tarea(
                id = "8",
                nombre = "Feria",
                disponibilidad = 6,
                puntos = 6,
                asignaciones = mapOf()
            )
        )
    }

    fun tomarTarea(tareaId: String, dia: DiaSemana, usuario: Usuario) {
        // Verificar que el usuario tenga datos válidos
        if (usuario.rut.isBlank()) {
            println("❌ ERROR: Usuario sin RUT")
            return
        }

        if (usuario.nombre.isBlank()) {
            println("⚠️ ADVERTENCIA: Usuario sin nombre, usando RUT")
        }

        println("🔵 REPOSITORY - TOMANDO TAREA: $tareaId - $dia - ${usuario.nombre}")

        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    val tareaTomadasCount = tarea.asignaciones.size

                    if (tareaTomadasCount < tarea.disponibilidad) {
                        val nuevaAsignacion = AsignacionTarea(
                            usuarioId = usuario.rut,
                            usuarioNombre = usuario.nombre.ifBlank { usuario.rut }, // ✅ Fallback
                            estado = EstadoAsignacion.TOMADA
                        )

                        val nuevasAsignaciones = tarea.asignaciones + (dia to nuevaAsignacion)
                        val nuevaTarea = tarea.copy(asignaciones = nuevasAsignaciones)
                        println("🟢 REPOSITORY - TAREA ACTUALIZADA: ${nuevaTarea.asignaciones[dia]}")
                        nuevaTarea
                    } else {
                        println("⚠️ REPOSITORY - Tarea completa, no se puede tomar")
                        tarea
                    }
                } else {
                    tarea
                }
            }
        }
    }

    fun completarTarea(tareaId: String, dia: DiaSemana, fotoUri: String) {
        println("📸 REPOSITORY - COMPLETANDO TAREA: $tareaId - $dia")

        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    val asignacion = tarea.asignaciones[dia]

                    if (asignacion != null) {
                        val asignacionActualizada = asignacion.copy(
                            estado = EstadoAsignacion.PENDIENTE_APROBACION,
                            fotoConfirmacion = fotoUri,
                            fechaCompletada = System.currentTimeMillis()
                        )

                        // ⭐ Crear nuevo Map
                        val nuevasAsignaciones = tarea.asignaciones + (dia to asignacionActualizada)

                        tarea.copy(asignaciones = nuevasAsignaciones)
                    } else {
                        tarea
                    }
                } else {
                    tarea
                }
            }
        }
    }

    fun aprobarTarea(tareaId: String, dia: DiaSemana) {
        println("✅ REPOSITORY - APROBANDO TAREA: $tareaId - $dia")

        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    val asignacion = tarea.asignaciones[dia]

                    if (asignacion != null && asignacion.estado == EstadoAsignacion.PENDIENTE_APROBACION) {
                        val asignacionActualizada = asignacion.copy(
                            estado = EstadoAsignacion.APROBADA,
                            fechaAprobada = System.currentTimeMillis()
                        )

                        // ⭐ Crear nuevo Map
                        val nuevasAsignaciones = tarea.asignaciones + (dia to asignacionActualizada)

                        tarea.copy(asignaciones = nuevasAsignaciones)
                    } else {
                        tarea
                    }
                } else {
                    tarea
                }
            }
        }
    }

    fun rechazarTarea(tareaId: String, dia: DiaSemana, comentario: String) {
        println("❌ REPOSITORY - RECHAZANDO TAREA: $tareaId - $dia")

        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    val asignacion = tarea.asignaciones[dia]

                    if (asignacion != null && asignacion.estado == EstadoAsignacion.PENDIENTE_APROBACION) {
                        val asignacionActualizada = asignacion.copy(
                            estado = EstadoAsignacion.RECHAZADA,
                            comentarioRechazo = comentario,
                            fotoConfirmacion = null
                        )

                        // ⭐ Crear nuevo Map
                        val nuevasAsignaciones = tarea.asignaciones + (dia to asignacionActualizada)

                        tarea.copy(asignaciones = nuevasAsignaciones)
                    } else {
                        tarea
                    }
                } else {
                    tarea
                }
            }
        }
    }

    fun liberarTarea(tareaId: String, dia: DiaSemana) {
        println("🔓 REPOSITORY - LIBERANDO TAREA: $tareaId - $dia")

        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    // ⭐ Eliminar del Map inmutable
                    val nuevasAsignaciones = tarea.asignaciones - dia
                    tarea.copy(asignaciones = nuevasAsignaciones)
                } else {
                    tarea
                }
            }
        }
    }

    fun obtenerTareasPendientesAprobacion(): List<Triple<Tarea, DiaSemana, AsignacionTarea>> {
        val pendientes = mutableListOf<Triple<Tarea, DiaSemana, AsignacionTarea>>()

        _tareas.value.forEach { tarea ->
            tarea.asignaciones.forEach { (dia, asignacion) ->
                if (asignacion.estado == EstadoAsignacion.PENDIENTE_APROBACION) {
                    pendientes.add(Triple(tarea, dia, asignacion))
                }
            }
        }

        return pendientes
    }
}