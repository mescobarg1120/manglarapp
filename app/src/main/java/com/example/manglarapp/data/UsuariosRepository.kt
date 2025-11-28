package com.example.manglarapp.data

import com.example.manglarapp.model.EstadoUsuario
import com.example.manglarapp.model.RolUsuario
import com.example.manglarapp.model.Usuario
import com.example.manglarapp.network.NetworkResult
import com.example.manglarapp.network.RetrofitClient
import com.example.manglarapp.network.dto.*
import com.example.manglarapp.network.safeApiCall
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.flow
import kotlin.collections.filter
import kotlin.collections.find
import kotlin.collections.indexOfFirst
import kotlin.collections.map
import kotlin.collections.toList
import kotlin.text.contains
import kotlin.text.isBlank

/**
 * Repository para gestionar operaciones CRUD de usuarios
 * Conectado al backend de Spring Boot con MySQL
 */
class UsuariosRepository {

    private val apiService = RetrofitClient.usuariosApi

    // Cache local (fallback en caso de error de red)
    private val usuariosInMemory = mutableListOf(
        Usuario(
            rut = "12.633.195-9",
            nombre = "Cristina Gonzalez",
            email = "m.escobar2@duocuc.cl",
            rol = RolUsuario.ARRENDATARIO,
            estado = EstadoUsuario.ACTIVO,
            password = "123456"
        )
    )

    /**
     * Obtiene todos los usuarios desde el backend
     */
    fun obtenerUsuarios(): Flow<List<Usuario>> = flow {
        val result = safeApiCall(
            apiCall = { apiService.obtenerUsuarios() },
            transform = { usuarios -> usuarios.map { it.toUsuario() } }
        )

        when (result) {
            is NetworkResult.Success -> {
                emit(result.data)
            }

            is NetworkResult.Error -> {
                println("Error obteniendo usuarios: ${result.message}")
                emit(usuariosInMemory.toList())
            }

            is NetworkResult.Loading -> {
                emit(emptyList())
            }
        }
    }

    /**
     * Obtiene un usuario por RUT desde el backend
     */
    suspend fun obtenerUsuarioPorRut(rut: String): Usuario? {
        val result = safeApiCall(
            apiCall = { apiService.obtenerUsuarioPorRut(rut) },
            transform = { it.toUsuario() }
        )

        return when (result) {
            is NetworkResult.Success -> result.data
            is NetworkResult.Error -> {
                println("Error obteniendo usuario: ${result.message}")
                usuariosInMemory.find { it.rut == rut }
            }
            is NetworkResult.Loading -> null
        }
    }

    /**
     * Crea un nuevo usuario en el backend
     */
    suspend fun crearUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.crearUsuario(usuario.toUsuarioRequest()) },
                transform = { it.toUsuario() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    usuariosInMemory.add(result.data)
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
     * Actualiza un usuario existente en el backend
     */
    suspend fun actualizarUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.actualizarUsuario(usuario.rut, usuario.toUsuarioRequest()) },
                transform = { it.toUsuario() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    val index = usuariosInMemory.indexOfFirst { it.rut == usuario.rut }
                    if (index != -1) {
                        usuariosInMemory[index] = result.data
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
     * Elimina un usuario por RUT en el backend
     */
    suspend fun eliminarUsuario(rut: String): Result<Unit> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.eliminarUsuario(rut) },
                transform = { Unit }
            )

            when (result) {
                is NetworkResult.Success -> {
                    usuariosInMemory.removeIf { it.rut == rut }
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
     * Busca usuarios por nombre o RUT en el backend
     */
    fun buscarUsuarios(query: String): Flow<List<Usuario>> = flow {
        if (query.isBlank()) {
            val result = safeApiCall(
                apiCall = { apiService.obtenerUsuarios() },
                transform = { usuarios -> usuarios.map { it.toUsuario() } }
            )
            when (result) {
                is NetworkResult.Success -> emit(result.data)
                is NetworkResult.Error -> emit(usuariosInMemory.toList())
                is NetworkResult.Loading -> emit(emptyList())
            }
        } else {
            val result = safeApiCall(
                apiCall = { apiService.buscarUsuarios(query) },
                transform = { usuarios -> usuarios.map { it.toUsuario() } }
            )
            when (result) {
                is NetworkResult.Success -> emit(result.data)
                is NetworkResult.Error -> {
                    val resultados = usuariosInMemory.filter { usuario ->
                        usuario.nombre.contains(query, ignoreCase = true) ||
                                usuario.rut.contains(query, ignoreCase = true) ||
                                usuario.email.contains(query, ignoreCase = true)
                    }
                    emit(resultados)
                }

                is NetworkResult.Loading -> emit(emptyList())
            }
        }
    }

    /**
     * Cambia el estado de un usuario en el backend
     */
    suspend fun cambiarEstadoUsuario(rut: String, nuevoEstado: EstadoUsuario): Result<Usuario> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.cambiarEstadoUsuario(rut, nuevoEstado.name) },
                transform = { it.toUsuario() }
            )

            when (result) {
                is NetworkResult.Success -> {
                    val index = usuariosInMemory.indexOfFirst { it.rut == rut }
                    if (index != -1) {
                        usuariosInMemory[index] = result.data
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
     * Login de usuario en el backend
     */
    suspend fun login(email: String, password: String): Result<Usuario> {
        return try {
            val result = safeApiCall(
                apiCall = { apiService.login(LoginRequest(email, password)) },
                transform = { it.usuario.toUsuario() }
            )

            when (result) {
                is NetworkResult.Success -> Result.success(result.data)
                is NetworkResult.Error -> Result.failure(kotlin.Exception(result.message))
                is NetworkResult.Loading -> Result.failure(kotlin.Exception("Operación en progreso"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
