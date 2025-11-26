package com.example.manglarapp.model

/**
 * Modelo de datos para una Finanza (registro de ingreso/egreso)
 */
data class Finanza(
    val id: Int = 0,
    val usuarioId: String = "",
    val tipo: TipoFinanza = TipoFinanza.INGRESO,
    val monto: Double = 0.0,
    val descripcion: String = "",
    val fecha: String = "",
    val categoria: String = ""
)

/**
 * Enumeración para el tipo de finanza
 */
enum class TipoFinanza {
    INGRESO,
    EGRESO
}

/**
 * Modelo para el resumen financiero de un usuario
 */
data class UsuarioFinanza(
    val usuarioId: String = "",
    val nombre: String = "",
    val puntos: Int = 0,
    val totalAbono: Double = 0.0,
    val totalDeuda: Double = 0.0
)

/**
 * Estado UI para la pantalla de finanzas
 */
data class FinanzasUiState(
    val mesSeleccionado: String = "Octubre",
    val usuariosFinanzas: List<UsuarioFinanza> = emptyList(),
    val gastos: List<Finanza> = emptyList(),
    val totalIngresos: Double = 0.0,
    val totalGastos: Double = 0.0,
    val saldo: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val mostrarDialogoGasto: Boolean = false
)

/**
 * Errores de validación para el formulario de gastos
 */
data class FinanzaValidationErrors(
    val descripcionError: String? = null,
    val montoError: String? = null,
    val fechaError: String? = null
) {
    fun hasErrors(): Boolean {
        return descripcionError != null || montoError != null || fechaError != null
    }
}