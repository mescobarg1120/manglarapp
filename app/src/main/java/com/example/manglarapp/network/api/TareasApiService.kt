package com.example.manglarapp.network.api

import com.example.manglarapp.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para operaciones de Tareas
 */
interface TareasApiService {

    /**
     * Obtiene todas las tareas
     */
    @GET("tareas")
    suspend fun obtenerTareas(): Response<List<TareaDto>>

    /**
     * Obtiene una tarea por ID
     */
    @GET("tareas/{id}")
    suspend fun obtenerTareaPorId(@Path("id") id: String): Response<TareaDto>

    /**
     * Tomar una tarea
     */
    @POST("tareas/tomar")
    suspend fun tomarTarea(@Body request: TomarTareaRequest): Response<TareaDto>

    /**
     * Completar una tarea (subir foto)
     */
    @POST("tareas/completar")
    suspend fun completarTarea(@Body request: CompletarTareaRequest): Response<TareaDto>

    /**
     * Aprobar una tarea
     */
    @POST("tareas/aprobar")
    suspend fun aprobarTarea(@Body request: AprobarTareaRequest): Response<TareaDto>

    /**
     * Rechazar una tarea
     */
    @POST("tareas/rechazar")
    suspend fun rechazarTarea(@Body request: RechazarTareaRequest): Response<TareaDto>

    /**
     * Liberar una tarea
     */
    @POST("tareas/liberar")
    suspend fun liberarTarea(@Body request: LiberarTareaRequest): Response<TareaDto>

    /**
     * Obtiene tareas pendientes de aprobación
     */
    @GET("tareas/pendientes-aprobacion")
    suspend fun obtenerTareasPendientesAprobacion(): Response<List<TareaDto>>
}
