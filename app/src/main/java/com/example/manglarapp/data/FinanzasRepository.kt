package com.example.manglarapp.data

import com.example.manglarapp.model.Finanza
import com.example.manglarapp.model.TipoFinanza
import com.example.manglarapp.model.UsuarioFinanza
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository para gestionar operaciones de finanzas del hogar
 */
class FinanzasRepository {

    // Datos en memoria para usuarios y sus finanzas
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

    // Gastos registrados
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
     * Obtiene las finanzas de todos los usuarios
     */
    fun obtenerUsuariosFinanzas(): Flow<List<UsuarioFinanza>> = flow {
        delay(100) // Simula latencia
        emit(usuariosFinanzas.toList())
    }

    /**
     * Obtiene todos los gastos
     */
    fun obtenerGastos(): Flow<List<Finanza>> = flow {
        delay(100)
        emit(gastos.filter { it.tipo == TipoFinanza.EGRESO }.toList())
    }

    /**
     * Obtiene gastos por mes
     */
    fun obtenerGastosPorMes(mes: String): Flow<List<Finanza>> = flow {
        delay(100)
        // En una implementación real, filtrarías por mes
        emit(gastos.filter { it.tipo == TipoFinanza.EGRESO }.toList())
    }

    /**
     * Crea un nuevo gasto
     */
    suspend fun crearGasto(gasto: Finanza): Result<Finanza> {
        return try {
            delay(200)

            val nuevoId = (gastos.maxOfOrNull { it.id } ?: 0) + 1
            val nuevoGasto = gasto.copy(id = nuevoId, tipo = TipoFinanza.EGRESO)

            gastos.add(nuevoGasto)
            Result.success(nuevoGasto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un gasto existente
     */
    suspend fun actualizarGasto(gasto: Finanza): Result<Finanza> {
        return try {
            delay(200)

            val index = gastos.indexOfFirst { it.id == gasto.id }
            if (index == -1) {
                return Result.failure(Exception("Gasto no encontrado"))
            }

            gastos[index] = gasto
            Result.success(gasto)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un gasto
     */
    suspend fun eliminarGasto(gastoId: Int): Result<Unit> {
        return try {
            delay(200)

            val removed = gastos.removeIf { it.id == gastoId }
            if (!removed) {
                return Result.failure(Exception("Gasto no encontrado"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
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
            delay(150)

            val index = usuariosFinanzas.indexOfFirst { it.usuarioId == usuarioId }
            if (index == -1) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val usuarioActualizado = usuariosFinanzas[index].copy(
                totalAbono = nuevoAbono
            )
            usuariosFinanzas[index] = usuarioActualizado

            Result.success(usuarioActualizado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Calcula el total de ingresos
     */
    fun calcularTotalIngresos(): Double {
        return usuariosFinanzas.sumOf { it.totalAbono }
    }

    /**
     * Calcula el total de gastos
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
            delay(500)
            // En producción, aquí generarías el archivo Excel real
            Result.success("finanzas_${System.currentTimeMillis()}.xlsx")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}