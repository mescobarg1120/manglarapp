package com.example.manglarapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manglarapp.data.FinanzasRepository
import com.example.manglarapp.model.Finanza
import com.example.manglarapp.model.FinanzaValidationErrors
import com.example.manglarapp.model.FinanzasUiState
import com.example.manglarapp.model.TipoFinanza
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado y lógica de la pantalla de Finanzas
 */
class FinanzasViewModel(
    private val repository: FinanzasRepository = FinanzasRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(FinanzasUiState())
    val uiState: StateFlow<FinanzasUiState> = _uiState.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga todos los datos de finanzas
     */
    fun cargarDatos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Cargar usuarios y sus finanzas
            repository.obtenerUsuariosFinanzas().collect { usuarios ->
                _uiState.update { state ->
                    state.copy(usuariosFinanzas = usuarios)
                }
            }

            // Cargar gastos
            repository.obtenerGastos().collect { gastos ->
                _uiState.update { state ->
                    state.copy(
                        gastos = gastos,
                        totalIngresos = repository.calcularTotalIngresos(),
                        totalGastos = repository.calcularTotalGastos(),
                        saldo = repository.calcularSaldo(),
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Cambia el mes seleccionado y recarga los datos
     */
    fun cambiarMes(nuevoMes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(mesSeleccionado = nuevoMes, isLoading = true) }

            repository.obtenerGastosPorMes(nuevoMes).collect { gastos ->
                _uiState.update { state ->
                    state.copy(
                        gastos = gastos,
                        totalGastos = gastos.sumOf { it.monto },
                        saldo = state.totalIngresos - gastos.sumOf { it.monto },
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Muestra el diálogo para agregar gasto
     */
    fun mostrarDialogoGasto() {
        _uiState.update { it.copy(mostrarDialogoGasto = true) }
    }

    /**
     * Cierra todos los diálogos
     */
    fun cerrarDialogos() {
        _uiState.update {
            it.copy(
                mostrarDialogoGasto = false,
                errorMessage = null
            )
        }
    }

    /**
     * Valida los datos de un gasto
     */
    fun validarGasto(gasto: Finanza): FinanzaValidationErrors {
        var errors = FinanzaValidationErrors()

        // Validar descripción
        if (gasto.descripcion.isBlank()) {
            errors = errors.copy(descripcionError = "La descripción es obligatoria")
        } else if (gasto.descripcion.length < 3) {
            errors = errors.copy(descripcionError = "La descripción debe tener al menos 3 caracteres")
        }

        // Validar monto
        if (gasto.monto <= 0) {
            errors = errors.copy(montoError = "El monto debe ser mayor a 0")
        }

        // Validar fecha
        if (gasto.fecha.isBlank()) {
            errors = errors.copy(fechaError = "La fecha es obligatoria")
        } else if (!validarFormatoFecha(gasto.fecha)) {
            errors = errors.copy(fechaError = "Formato inválido (dd/MM/yy)")
        }

        return errors
    }

    /**
     * Crea un nuevo gasto
     */
    fun crearGasto(gasto: Finanza) {
        viewModelScope.launch {
            val errors = validarGasto(gasto)

            if (errors.hasErrors()) {
                _uiState.update {
                    it.copy(errorMessage = "Por favor, corrija los errores en el formulario")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            repository.crearGasto(gasto)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Gasto agregado exitosamente",
                            mostrarDialogoGasto = false
                        )
                    }
                    cargarDatos()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al crear gasto"
                        )
                    }
                }
        }
    }

    /**
     * Elimina un gasto
     */
    fun eliminarGasto(gastoId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.eliminarGasto(gastoId)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Gasto eliminado exitosamente"
                        )
                    }
                    cargarDatos()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al eliminar gasto"
                        )
                    }
                }
        }
    }

    /**
     * Actualiza el abono de un usuario
     */
    fun actualizarAbonoUsuario(usuarioId: String, nuevoAbono: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.actualizarAbonoUsuario(usuarioId, nuevoAbono)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Abono actualizado exitosamente"
                        )
                    }
                    cargarDatos()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al actualizar abono"
                        )
                    }
                }
        }
    }

    /**
     * Exporta los datos a Excel
     */
    fun exportarAExcel() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.exportarAExcel()
                .onSuccess { nombreArchivo ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Archivo exportado: $nombreArchivo"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al exportar"
                        )
                    }
                }
        }
    }

    /**
     * Limpia los mensajes de éxito/error
     */
    fun limpiarMensajes() {
        _uiState.update {
            it.copy(
                successMessage = null,
                errorMessage = null
            )
        }
    }

    // Función auxiliar para validar formato de fecha
    private fun validarFormatoFecha(fecha: String): Boolean {
        // Formato: dd/MM/yy
        val regex = Regex("""^\d{2}/\d{2}/\d{2}$""")
        return regex.matches(fecha)
    }
}
