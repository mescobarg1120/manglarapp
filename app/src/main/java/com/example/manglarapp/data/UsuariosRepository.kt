package com.example.manglarapp.data

import android.util.Log
import com.example.manglarapp.model.EstadoUsuario
import com.example.manglarapp.model.Usuario
import com.example.manglarapp.network.ApiConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class UsuariosRepository {

    private val api = ApiConfig.usuarioApi
    private val TAG = "UsuariosRepositoryApi"

    /**
     * Obtiene todos los usuarios desde la API
     */
    fun obtenerUsuarios(): Flow<List<Usuario>> = flow {
        try {
            val response = api.obtenerTodos()
            if (response.isSuccessful) {
                emit(response.body() ?: emptyList())
            } else {
                Log.e(TAG, "Error al obtener usuarios: ${response.code()}")
                emit(emptyList())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener usuarios", e)
            emit(emptyList())
        }
    }

    /**
     * Obtiene un usuario por RUT desde la API
     */
    suspend fun obtenerUsuarioPorRut(rut: String): Usuario? {
        return try {
            val response = api.obtenerPorRut(rut)
            if (response.isSuccessful) {
                response.body()
            } else {
                Log.e(TAG, "Error al obtener usuario: ${response.code()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al obtener usuario", e)
            null
        }
    }

    /**
     * Crea un nuevo usuario en la API
     */
    suspend fun crearUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val response = api.crear(usuario)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al crear usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al crear usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Actualiza un usuario existente en la API
     */
    suspend fun actualizarUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            val response = api.actualizar(usuario.rut, usuario)
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Error al actualizar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al actualizar usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Elimina un usuario por RUT desde la API
     */
    suspend fun eliminarUsuario(rut: String): Result<Unit> {
        return try {
            val response = api.eliminar(rut)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Error al eliminar usuario: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al eliminar usuario", e)
            Result.failure(e)
        }
    }

    /**
     * Busca usuarios por nombre o RUT en la API
     */
    fun buscarUsuarios(query: String): Flow<List<Usuario>> = flow {
        try {
            if (query.isBlank()) {
                val response = api.obtenerTodos()
                if (response.isSuccessful) {
                    emit(response.body() ?: emptyList())
                } else {
                    emit(emptyList())
                }
            } else {
                val response = api.buscar(query)
                if (response.isSuccessful) {
                    emit(response.body() ?: emptyList())
                } else {
                    emit(emptyList())
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al buscar usuarios", e)
            emit(emptyList())
        }
    }

    /**
     * Cambia el estado de un usuario
     * Nota: Primero obtiene el usuario, cambia su estado y lo actualiza
     */
    suspend fun cambiarEstadoUsuario(rut: String, nuevoEstado: EstadoUsuario): Result<Usuario> {
        return try {
            val usuario = obtenerUsuarioPorRut(rut)
            if (usuario != null) {
                val usuarioActualizado = usuario.copy(estado = nuevoEstado)
                actualizarUsuario(usuarioActualizado)
            } else {
                Result.failure(Exception("Usuario no encontrado"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción al cambiar estado usuario", e)
            Result.failure(e)
        }
    }
}