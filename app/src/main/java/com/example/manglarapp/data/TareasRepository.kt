package com.example.manglarapp.data.repository

import com.example.manglarapp.domain.model.*
import com.example.manglarapp.model.Usuario
import com.example.manglarapp.network.NetworkResult
import com.example.manglarapp.network.RetrofitClient
import com.example.manglarapp.network.dto.*
import com.example.manglarapp.network.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.collections.forEach
import kotlin.collections.map
import kotlin.collections.minus
import kotlin.collections.plus
import kotlin.text.ifBlank
import kotlin.text.isBlank
import kotlin.to

/**
 * Repository para gestionar operaciones de Tareas
 * Conectado al backend de Spring Boot con MySQL
 */
class TareasRepository {

    private val apiService = RetrofitClient.tareasApi
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _tareas = MutableStateFlow<List<Tarea>>(emptyList())
    val tareas: StateFlow<List<Tarea>> = _tareas

    init {
        // Cargar tareas desde el backend
        cargarTareasDesdeBackend()
    }

    /**
     * Carga las tareas desde el backend
     */
    private fun cargarTareasDesdeBackend() {
        scope.launch {
            val result = safeApiCall(
                apiCall = { apiService.obtenerTareas() },
                transform = { tareasDto -> tareasDto.map { it.toTarea() } }
            )

            when (result) {
                is NetworkResult.Success -> {
                    _tareas.value = result.data
                }
                is NetworkResult.Error -> {
                    println("Error cargando tareas: ${result.message}")
                    // Fallback a datos locales si falla la carga
                    _tareas.value = obtenerTareasLocales()
                }
                is NetworkResult.Loading -> {
                    // No hacer nada
                }
            }
        }
    }

    /**
     * Obtiene tareas locales (fallback)
     */
    private fun obtenerTareasLocales(): List<Tarea> {
        return listOf(
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

    /**
     * Tomar una tarea
     */
    fun tomarTarea(tareaId: String, dia: DiaSemana, usuario: Usuario) {
        if (usuario.rut.isBlank()) {
            println("❌ ERROR: Usuario sin RUT")
            return
        }

        if (usuario.nombre.isBlank()) {
            println("⚠️ ADVERTENCIA: Usuario sin nombre, usando RUT")
        }

        println("🔵 REPOSITORY - TOMANDO TAREA: $tareaId - $dia - ${usuario.nombre}")

        scope.launch {
            val request = TomarTareaRequest(
                tareaId = tareaId,
                dia = dia.name,
                usuarioId = usuario.rut,
                usuarioNombre = usuario.nombre.ifBlank { usuario.rut }
            )

            val result = safeApiCall(
                apiCall = { apiService.tomarTarea(request) },
                transform = { it.toTarea() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    // Actualiza el estado local con la tarea actualizada
                    _tareas.update { tareasList ->
                        tareasList.map { if (it.id == tareaId) result.data else it }
                    }
                }
                is NetworkResult.Error -> {
                    println("Error tomando tarea: ${result.message}")
                    // Actualización local como fallback
                    actualizarTareaLocalmente(tareaId, dia, usuario)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    /**
     * Actualización local de tarea (fallback)
     */
    private fun actualizarTareaLocalmente(tareaId: String, dia: DiaSemana, usuario: Usuario) {
        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    val tareaTomadasCount = tarea.asignaciones.size
                    if (tareaTomadasCount < tarea.disponibilidad) {
                        val nuevaAsignacion = AsignacionTarea(
                            usuarioId = usuario.rut,
                            usuarioNombre = usuario.nombre.ifBlank { usuario.rut },
                            estado = EstadoAsignacion.TOMADA
                        )
                        val nuevasAsignaciones = tarea.asignaciones + (dia to nuevaAsignacion)
                        tarea.copy(asignaciones = nuevasAsignaciones)
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

    /**
     * Completar una tarea
     */
    fun completarTarea(tareaId: String, dia: DiaSemana, fotoUri: String) {
        println("📸 REPOSITORY - COMPLETANDO TAREA: $tareaId - $dia")

        scope.launch {
            val request = CompletarTareaRequest(
                tareaId = tareaId,
                dia = dia.name,
                fotoUri = fotoUri
            )

            val result = safeApiCall(
                apiCall = { apiService.completarTarea(request) },
                transform = { it.toTarea() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    _tareas.update { tareasList ->
                        tareasList.map { if (it.id == tareaId) result.data else it }
                    }
                }
                is NetworkResult.Error -> {
                    println("Error completando tarea: ${result.message}")
                    completarTareaLocalmente(tareaId, dia, fotoUri)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    /**
     * Completar tarea localmente (fallback)
     */
    private fun completarTareaLocalmente(tareaId: String, dia: DiaSemana, fotoUri: String) {
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

    /**
     * Aprobar una tarea
     */
    fun aprobarTarea(tareaId: String, dia: DiaSemana) {
        println("✅ REPOSITORY - APROBANDO TAREA: $tareaId - $dia")

        scope.launch {
            val request = AprobarTareaRequest(
                tareaId = tareaId,
                dia = dia.name
            )

            val result = safeApiCall(
                apiCall = { apiService.aprobarTarea(request) },
                transform = { it.toTarea() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    _tareas.update { tareasList ->
                        tareasList.map { if (it.id == tareaId) result.data else it }
                    }
                }
                is NetworkResult.Error -> {
                    println("Error aprobando tarea: ${result.message}")
                    aprobarTareaLocalmente(tareaId, dia)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    /**
     * Aprobar tarea localmente (fallback)
     */
    private fun aprobarTareaLocalmente(tareaId: String, dia: DiaSemana) {
        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    val asignacion = tarea.asignaciones[dia]
                    if (asignacion != null && asignacion.estado == EstadoAsignacion.PENDIENTE_APROBACION) {
                        val asignacionActualizada = asignacion.copy(
                            estado = EstadoAsignacion.APROBADA,
                            fechaAprobada = System.currentTimeMillis()
                        )
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

    /**
     * Rechazar una tarea
     */
    fun rechazarTarea(tareaId: String, dia: DiaSemana, comentario: String) {
        println("❌ REPOSITORY - RECHAZANDO TAREA: $tareaId - $dia")

        scope.launch {
            val request = RechazarTareaRequest(
                tareaId = tareaId,
                dia = dia.name,
                comentario = comentario
            )

            val result = safeApiCall(
                apiCall = { apiService.rechazarTarea(request) },
                transform = { it.toTarea() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    _tareas.update { tareasList ->
                        tareasList.map { if (it.id == tareaId) result.data else it }
                    }
                }
                is NetworkResult.Error -> {
                    println("Error rechazando tarea: ${result.message}")
                    rechazarTareaLocalmente(tareaId, dia, comentario)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    /**
     * Rechazar tarea localmente (fallback)
     */
    private fun rechazarTareaLocalmente(tareaId: String, dia: DiaSemana, comentario: String) {
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

    /**
     * Liberar una tarea
     */
    fun liberarTarea(tareaId: String, dia: DiaSemana) {
        println("🔓 REPOSITORY - LIBERANDO TAREA: $tareaId - $dia")

        scope.launch {
            val request = LiberarTareaRequest(
                tareaId = tareaId,
                dia = dia.name
            )

            val result = safeApiCall(
                apiCall = { apiService.liberarTarea(request) },
                transform = { it.toTarea() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    _tareas.update { tareasList ->
                        tareasList.map { if (it.id == tareaId) result.data else it }
                    }
                }
                is NetworkResult.Error -> {
                    println("Error liberando tarea: ${result.message}")
                    liberarTareaLocalmente(tareaId, dia)
                }
                is NetworkResult.Loading -> {}
            }
        }
    }

    /**
     * Liberar tarea localmente (fallback)
     */
    private fun liberarTareaLocalmente(tareaId: String, dia: DiaSemana) {
        _tareas.update { tareasList ->
            tareasList.map { tarea ->
                if (tarea.id == tareaId) {
                    val nuevasAsignaciones = tarea.asignaciones - dia
                    tarea.copy(asignaciones = nuevasAsignaciones)
                } else {
                    tarea
                }
            }
        }
    }

    /**
     * Obtiene tareas pendientes de aprobación
     */
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

    /**
     * Refresca las tareas desde el backend
     */
    fun refrescarTareas() {
        cargarTareasDesdeBackend()
    }
}
