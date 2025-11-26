package com.example.manglarapp.data

import android.util.Log
import com.example.manglarapp.model.Finanza
import com.example.manglarapp.model.TipoFinanza
import com.example.manglarapp.model.UsuarioFinanza
import com.example.manglarapp.network.ApiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository para gestionar operaciones de finanzas consumiendo API REST
 */
class FinanzasRepository {

    private val api = ApiConfig.finanzaApi
    private val TAG = "FinanzasRepositoryApi"

    /**
     * Obtiene las finanzas de todos los usuarios desde la API
     */
    fun obtenerUsuariosFinanzas(): Flow<List<UsuarioFinanza>> = flow {
        try {
            val response = api.obtenerUsuariosFinanza()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                Log.e(TAG, "Error al obtener usuarios finanza: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener usuarios finanza", e)
            emit(emptyList())
        }
    }

    /**
     * Obtiene todos los gastos desde la API
     */
    fun obtenerGastos(): Flow<List<Finanza>> = flow {
        try {
            val response = api.obtenerGastos()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                Log.e(TAG, "Error al obtener gastos: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener gastos", e)
            emit(emptyList())
        }
    }

    /**
     * Obtiene gastos por mes
     * Nota: Por ahora devuelve todos los gastos
     */
    fun obtenerGastosPorMes(mes: String): Flow<List<Finanza>> = flow {
        try {
            val response = api.obtenerGastos()
            if (response.isSuccessful) {
                // TODO: Filtrar por mes en el futuro
                emit(response.body() ?: emptyList())
            } else {
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener gastos por mes", e)
            emit(emptyList())
        }
    }

    /**
     * Crea un nuevo gasto en la API
     */
    suspend fun crearGasto(gasto: Finanza): Result<Finanza> {
        return try {
            val response = api.crear(gasto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear gasto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al crear gasto", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza un gasto existente en la API
     */
    suspend fun actualizarGasto(gasto: Finanza): Result<Finanza> {
        return try {
            val response = api.actualizar(gasto.id.toLong(), gasto)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar gasto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar gasto", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un gasto desde la API
     */
    suspend fun eliminarGasto(gastoId: Int): Result<Unit> {
        return try {
            val response = api.eliminar(gastoId.toLong())
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar gasto: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al eliminar gasto", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza el abono de un usuario
     */
    suspend fun actualizarAbonoUsuario(
        usuarioId: String,
        nuevoAbono: Double
    ): Result<UsuarioFinanza> {
        return try {
            // Obtener el usuario finanza actual
            val responseGet = api.obtenerUsuarioFinanzaPorId(usuarioId)
            if (responseGet.isSuccessful && responseGet.body() != null) {
                val usuarioFinanza = responseGet.body()!!
                val actualizado = usuarioFinanza.copy(totalAbono = nuevoAbono)

                val responseUpdate = api.actualizarUsuarioFinanza(usuarioId, actualizado)
                if (responseUpdate.isSuccessful && responseUpdate.body() != null) {
                    Result.success(responseUpdate.body()!!)
                } else {
                    Result.failure(Exception("Error al actualizar abono: ${responseUpdate.code()}"))
                }
            } else {
                Result.failure(Exception("Usuario finanza no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar abono", e)
            Result.failure(e)
        }
    }

    /**
     * Calcula el total de ingresos desde la API
     */
    suspend fun calcularTotalIngresos(): Double {
        return try {
            val response = api.obtenerResumen()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.totalIngresos
            } else {
                0.0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al calcular ingresos", e)
            0.0
        }
    }

    /**
     * Calcula el total de gastos desde la API
     */
    suspend fun calcularTotalGastos(): Double {
        return try {
            val response = api.obtenerResumen()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.totalGastos
            } else {
                0.0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al calcular gastos", e)
            0.0
        }
    }

    /**
     * Calcula el saldo (ingresos - gastos) desde la API
     */
    suspend fun calcularSaldo(): Double {
        return try {
            val response = api.obtenerResumen()
            if (response.isSuccessful && response.body() != null) {
                response.body()!!.saldo
            } else {
                0.0
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al calcular saldo", e)
            0.0
        }
    }

    /**
     * Exporta datos a formato CSV (simulado)
     */
    suspend fun exportarAExcel(): Result<String> {
        return try {
            // En producción, aquí generarías el archivo Excel real
            Result.success("finanzas_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}