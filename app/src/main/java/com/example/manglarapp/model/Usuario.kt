package com.example.manglarapp.model

/**
 * Modelo de datos para un Usuario del sistema
 */
data class Usuario(
    val rut: String = "",
    val nombre: String = "",
    val email: String = "",
    val rol: String = "",
    val estado: EstadoUsuario = EstadoUsuario.ACTIVO,
    val password: String = "" // En producción, esto debe estar encriptado
)

/**
 * Enumeración para el estado del usuario
 */
enum class EstadoUsuario {
    ACTIVO,
    INACTIVO,
    BLOQUEADO
}

/**
 * Estado UI para la pantalla de gestión de usuarios
 */
data class UsuariosUiState(
    val usuarios: List<Usuario> = emptyList(),
    val usuarioSeleccionado: Usuario? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val mostrarDialogoNuevo: Boolean = false,
    val mostrarDialogoEditar: Boolean = false,
    val mostrarDialogoEliminar: Boolean = false,
    val busqueda: String = ""
)

/**
 * Clase para errores de validación en formulario de usuario
 */
data class UsuarioValidationErrors(
    val rutError: String? = null,
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null
) {
    fun hasErrors(): Boolean {
        return rutError != null || nombreError != null ||
                emailError != null || passwordError != null
    }
}