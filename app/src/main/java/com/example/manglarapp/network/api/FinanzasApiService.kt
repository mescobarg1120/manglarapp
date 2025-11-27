package com.example.manglarapp.network.api

import com.example.manglarapp.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para operaciones de Finanzas
 */
interface FinanzasApiService {

    /**
     * Obtiene finanzas de todos los usuarios
     */
    @GET("finanzas/usuarios")
    suspend fun obtenerUsuariosFinanzas(): Response<List<UsuarioFinanzaDto>>

    /**
     * Obtiene todos los gastos
     */
    @GET("finanzas/gastos")
    suspend fun obtenerGastos(): Response<List<FinanzaDto>>

    /**
     * Obtiene gastos por mes
     */
    @GET("finanzas/gastos")
    suspend fun obtenerGastosPorMes(@Query("mes") mes: String): Response<List<FinanzaDto>>

    /**
     * Crea un nuevo gasto
     */
    @POST("finanzas/gastos")
    suspend fun crearGasto(@Body request: FinanzaRequest): Response<FinanzaDto>

    /**
     * Actualiza un gasto existente
     */
    @PUT("finanzas/gastos/{id}")
    suspend fun actualizarGasto(
        @Path("id") id: Int,
        @Body request: FinanzaRequest
    ): Response<FinanzaDto>

    /**
     * Elimina un gasto
     */
    @DELETE("finanzas/gastos/{id}")
    suspend fun eliminarGasto(@Path("id") id: Int): Response<Unit>

    /**
     * Actualiza el abono de un usuario
     */
    @PATCH("finanzas/usuarios/{usuarioId}/abono")
    suspend fun actualizarAbonoUsuario(
        @Path("usuarioId") usuarioId: String,
        @Body request: ActualizarAbonoRequest
    ): Response<UsuarioFinanzaDto>

    /**
     * Obtiene el resumen financiero
     */
    @GET("finanzas/resumen")
    suspend fun obtenerResumen(): Response<ResumenFinancieroDto>
}

/**
 * DTO para el resumen financiero
 */
data class ResumenFinancieroDto(
    val totalIngresos: Double,
    val totalGastos: Double,
    val saldo: Double
)
