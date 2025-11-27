package com.example.manglarapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.manglarapp.model.EstadoUsuario
import com.example.manglarapp.model.Usuario
import com.example.manglarapp.model.UsuarioValidationErrors
import com.example.manglarapp.model.UsuariosUiState
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado y lógica de la pantalla de Usuarios
 */
class UsuariosViewModel(
    private val repository: UsuariosRepository = UsuariosRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(UsuariosUiState())
    val uiState: StateFlow<UsuariosUiState> = _uiState.asStateFlow()

    init {
        cargarUsuarios()
    }

    /**
     * Carga la lista de usuarios desde el repository
     */
    fun cargarUsuarios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.obtenerUsuarios().collect { usuarios ->
                _uiState.update {
                    it.copy(
                        usuarios = usuarios,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            }
        }
    }

    /**
     * Busca usuarios por query
     */
    fun buscarUsuarios(query: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(busqueda = query, isLoading = true) }

            repository.buscarUsuarios(query).collect { usuarios ->
                _uiState.update {
                    it.copy(
                        usuarios = usuarios,
                        isLoading = false
                    )
                }
            }
        }
    }

    /**
     * Muestra el diálogo para crear nuevo usuario
     */
    fun mostrarDialogoNuevoUsuario() {
        _uiState.update {
            it.copy(
                mostrarDialogoNuevo = true,
                usuarioSeleccionado = null
            )
        }
    }

    /**
     * Muestra el diálogo para editar usuario
     */
    fun mostrarDialogoEditarUsuario(usuario: Usuario) {
        _uiState.update {
            it.copy(
                mostrarDialogoEditar = true,
                usuarioSeleccionado = usuario
            )
        }
    }

    /**
     * Muestra el diálogo de confirmación de eliminación
     */
    fun mostrarDialogoEliminar(usuario: Usuario) {
        _uiState.update {
            it.copy(
                mostrarDialogoEliminar = true,
                usuarioSeleccionado = usuario
            )
        }
    }

    /**
     * Cierra todos los diálogos
     */
    fun cerrarDialogos() {
        _uiState.update {
            it.copy(
                mostrarDialogoNuevo = false,
                mostrarDialogoEditar = false,
                mostrarDialogoEliminar = false,
                usuarioSeleccionado = null,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    /**
     * Valida los datos del usuario
     */
    fun validarUsuario(usuario: Usuario): UsuarioValidationErrors {
        var errors = UsuarioValidationErrors()

        // Validar RUT
        if (usuario.rut.isBlank()) {
            errors = errors.copy(rutError = "El RUT es obligatorio")
        } else if (!validarFormatoRut(usuario.rut)) {
            errors = errors.copy(rutError = "Formato de RUT inválido (ej: 12.345.678-9)")
        }

        // Validar nombre
        if (usuario.nombre.isBlank()) {
            errors = errors.copy(nombreError = "El nombre es obligatorio")
        } else if (usuario.nombre.length < 3) {
            errors = errors.copy(nombreError = "El nombre debe tener al menos 3 caracteres")
        }

        // Validar email
        if (usuario.email.isBlank()) {
            errors = errors.copy(emailError = "El email es obligatorio")
        } else if (!validarFormatoEmail(usuario.email)) {
            errors = errors.copy(emailError = "Formato de email inválido")
        }

        // Validar password (solo para usuarios nuevos)
        if (usuario.password.isBlank() && _uiState.value.mostrarDialogoNuevo) {
            errors = errors.copy(passwordError = "La contraseña es obligatoria")
        } else if (usuario.password.isNotBlank() && usuario.password.length < 6) {
            errors = errors.copy(passwordError = "La contraseña debe tener al menos 6 caracteres")
        }

        return errors
    }

    /**
     * Crea un nuevo usuario
     */
    fun crearUsuario(usuario: Usuario) {
        viewModelScope.launch {
            val errors = validarUsuario(usuario)

            if (errors.hasErrors()) {
                _uiState.update {
                    it.copy(errorMessage = "Por favor, corrija los errores en el formulario")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            repository.crearUsuario(usuario)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Usuario creado exitosamente",
                            mostrarDialogoNuevo = false,
                            errorMessage = null
                        )
                    }
                    cargarUsuarios()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al crear usuario"
                        )
                    }
                }
        }
    }

    /**
     * Actualiza un usuario existente
     */
    fun actualizarUsuario(usuario: Usuario) {
        viewModelScope.launch {
            val errors = validarUsuario(usuario)

            if (errors.hasErrors()) {
                _uiState.update {
                    it.copy(errorMessage = "Por favor, corrija los errores en el formulario")
                }
                return@launch
            }

            _uiState.update { it.copy(isLoading = true) }

            repository.actualizarUsuario(usuario)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Usuario actualizado exitosamente",
                            mostrarDialogoEditar = false,
                            errorMessage = null
                        )
                    }
                    cargarUsuarios()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al actualizar usuario"
                        )
                    }
                }
        }
    }

    /**
     * Elimina un usuario
     */
    fun eliminarUsuario(rut: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.eliminarUsuario(rut)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Usuario eliminado exitosamente",
                            mostrarDialogoEliminar = false,
                            errorMessage = null
                        )
                    }
                    cargarUsuarios()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al eliminar usuario",
                            mostrarDialogoEliminar = false
                        )
                    }
                }
        }
    }

    /**
     * Cambia el estado de un usuario
     */
    fun cambiarEstadoUsuario(rut: String, nuevoEstado: EstadoUsuario) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            repository.cambiarEstadoUsuario(rut, nuevoEstado)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            successMessage = "Estado actualizado exitosamente"
                        )
                    }
                    cargarUsuarios()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = error.message ?: "Error al cambiar estado"
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

    // Funciones auxiliares de validación

    private fun validarFormatoRut(rut: String): Boolean {
        // Formato básico: XX.XXX.XXX-X
        val regex = Regex("""^\d{1,2}\.\d{3}\.\d{3}-[\dkK]$""")
        return regex.matches(rut)
    }

    private fun validarFormatoEmail(email: String): Boolean {
        val regex = Regex("""^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
        return regex.matches(email)
    }
}