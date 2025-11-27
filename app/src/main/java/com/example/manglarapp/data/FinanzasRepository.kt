package com.example.manglarapp.data

import com.example.manglarapp.model.Finanza
import com.example.manglarapp.model.TipoFinanza
import com.example.manglarapp.model.UsuarioFinanza
import com.example.manglarapp.network.NetworkResult
import com.example.manglarapp.network.RetrofitClient
import com.example.manglarapp.network.dto.*
import com.example.manglarapp.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.collections.filter
import kotlin.collections.indexOfFirst
import kotlin.collections.map
import kotlin.collections.sumOf
import kotlin.collections.toList

/**
 * Repository para gestionar operaciones de finanzas del hogar
 * Conectado al backend de Spring Boot con MySQL
 */
class FinanzasRepository {

    private val apiService = RetrofitClient.finanzasApi

    // Cache local (fallback en caso de error de red)
    private val usuariosFinanzas = mutableListOf(
        UsuarioFinanza(
            usuarioId = "12.633.195-9",
            nombre = "Felipe",
            puntos = 10,
            totalAbono = 20000.0,
            totalDeuda = 14000.0
        ),
        UsuarioFinanza(
            usuarioId = "18.234.567-8",
            nombre = "Kototo",
            puntos = 2,
            totalAbono = 20000.0,
            totalDeuda = 34000.0
        ),
        UsuarioFinanza(
            usuarioId = "15.678.901-2",
            nombre = "Mati",
            puntos = 0,
            totalAbono = 40000.0,
            totalDeuda = 0.0
        ),
        UsuarioFinanza(
            usuarioId = "19.876.543-k",
            nombre = "Santi",
            puntos = 0,
            totalAbono = 10000.0,
            totalDeuda = 0.0
        ),
        UsuarioFinanza(
            usuarioId = "17.345.678-3",
            nombre = "Fran",
            puntos = 0,
            totalAbono = 20000.0,
            totalDeuda = 0.0
        )
    )

    private val gastos = mutableListOf(
        Finanza(
            id = 1,
            usuarioId = "12.633.195-9",
            tipo = TipoFinanza.EGRESO,
            monto = 40000.0,
            descripcion = "Feria",
            fecha = "01/10/25",
            categoria = "Alimentación"
        ),
        Finanza(
            id = 2,
            usuarioId = "18.234.567-8",
            tipo = TipoFinanza.EGRESO,
            monto = 20000.0,
            descripcion = "Productos de Aseo",
            fecha = "04/10/25",
            categoria = "Limpieza"
        ),
        Finanza(
            id = 3,
            usuarioId = "15.678.901-2",
            tipo = TipoFinanza.EGRESO,
            monto = 200000.0,
            descripcion = "Arreglo Techo",
            fecha = "10/10/25",
            categoria = "Mantención"
        )
    )

    /**
     * Obtiene las finanzas de todos los usuarios desde el backend
     */
    fun obtenerUsuariosFinanzas(): Flow<List<UsuarioFinanza>> = flow {
        val result = safeApiCall(
            apiCall = { apiService.obtenerUsuariosFinanzas() },
            transform = { usuarios -> usuarios.map { it.toUsuarioFinanza() } }
        )

        when (result) {
            is NetworkResult.Success -> FlowCollector.emit(result.data)
            is NetworkResult.Error -> {
                println("Error obteniendo usuarios finanzas: ${result.message}")
                FlowCollector.emit(usuariosFinanzas.toList())
            }

            is NetworkResult.Loading -> FlowCollector.emit(emptyList())
        }
    }

    /**
     * Obtiene todos los gastos desde el backend
     */
    fun obtenerGastos(): Flow<List<Finanza>> = flow {
        val result = safeApiCall(
            apiCall = { apiService.obtenerGastos() },
            transform = { gastosDto -> gastosDto.map { it.toFinanza() } }
        )

        when (result) {
            is NetworkResult.Success -> FlowCollector.emit(result.data)
            is NetworkResult.Error -> {
                println("Error obteniendo gastos: ${result.message}")
                FlowCollector.emit(gastos.filter { it.tipo == TipoFinanza.EGRESO }.toList())
            }

            is NetworkResult.Loading -> FlowCollector.emit(emptyList())
        }
    }

    /**
     * Obtiene gastos por mes desde el backend
     */
    fun obtenerGastosPorMes(mes: String): Flow<List<Finanza>> = flow {
        val result = safeApiCall(
            apiCall = { apiService.obtenerGastosPorMes(mes) },
            transform = { gastosDto -> gastosDto.map { it.toFinanza() } }
        )

        when (result) {
            is NetworkResult.Success -> FlowCollector.emit(result.data)
            is NetworkResult.Error -> {
                println("Error obteniendo gastos por mes: ${result.message}")
                FlowCollector.emit(gastos.filter { it.tipo == TipoFinanza.EGRESO }.toList())
            }

            is NetworkResult.Loading -> FlowCollector.emit(emptyList())
        }
    }

    /**
     * Crea un nuevo gasto en el backend
     */
    suspend fun crearGasto(gasto: Finanza): Result<Finanza> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.crearGasto(gasto.toFinanzaRequest()) },
                transform = { it.toFinanza() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    gastos.add(result.data)
                    Result.success(result.data)
                }
                is NetworkResult.Error -> {
                    Result.failure(kotlin.Exception(result.message))
                }
                is NetworkResult.Loading -> {
                    Result.failure(kotlin.Exception("Operación en progreso"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un gasto existente en el backend
     */
    suspend fun actualizarGasto(gasto: Finanza): Result<Finanza> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.actualizarGasto(gasto.id, gasto.toFinanzaRequest()) },
                transform = { it.toFinanza() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    val index = gastos.indexOfFirst { it.id == gasto.id }
                    if (index != -1) {
                        gastos[index] = result.data
                    }
                    Result.success(result.data)
                }
                is NetworkResult.Error -> {
                    Result.failure(kotlin.Exception(result.message))
                }
                is NetworkResult.Loading -> {
                    Result.failure(kotlin.Exception("Operación en progreso"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un gasto en el backend
     */
    suspend fun eliminarGasto(gastoId: Int): Result<Unit> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.eliminarGasto(gastoId) },
                transform = { Unit }
            )

            when (result) {
                is NetworkResult.Success -> {
                    gastos.removeIf { it.id == gastoId }
                    Result.success(Unit)
                }
                is NetworkResult.Error -> {
                    Result.failure(kotlin.Exception(result.message))
                }
                is NetworkResult.Loading -> {
                    Result.failure(kotlin.Exception("Operación en progreso"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza el abono de un usuario en el backend
     */
    suspend fun actualizarAbonoUsuario(
        usuarioId: String,
        nuevoAbono: Double
    ): Result<UsuarioFinanza> {
        return try {
            val result = safeApiCall(
                apiCall = {
                    apiService.actualizarAbonoUsuario(
                        usuarioId,
                        ActualizarAbonoRequest(usuarioId, nuevoAbono)
                    )
                },
                transform = { it.toUsuarioFinanza() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    val index = usuariosFinanzas.indexOfFirst { it.usuarioId == usuarioId }
                    if (index != -1) {
                        usuariosFinanzas[index] = result.data
                    }
                    Result.success(result.data)
                }
                is NetworkResult.Error -> {
                    Result.failure(kotlin.Exception(result.message))
                }
                is NetworkResult.Loading -> {
                    Result.failure(kotlin.Exception("Operación en progreso"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calcula el total de ingresos (se puede obtener del backend)
     */
    fun calcularTotalIngresos(): Double {
        return usuariosFinanzas.sumOf { it.totalAbono }
    }

    /**
     * Calcula el total de gastos (se puede obtener del backend)
     */
    fun calcularTotalGastos(): Double {
        return gastos.filter { it.tipo == TipoFinanza.EGRESO }.sumOf { it.monto }
    }

    /**
     * Calcula el saldo (ingresos - gastos)
     */
    fun calcularSaldo(): Double {
        return calcularTotalIngresos() - calcularTotalGastos()
    }

    /**
     * Exporta datos a formato CSV (simulado)
     */
    suspend fun exportarAExcel(): Result<String> {
        return try {
            // En producción, aquí generarías el archivo Excel real
            // Podrías llamar a un endpoint del backend que genere el reporte
            Result.success("finanzas_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
