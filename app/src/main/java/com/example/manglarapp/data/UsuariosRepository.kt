package com.example.manglarapp.data

import com.example.manglarapp.model.EstadoUsuario
import com.example.manglarapp.model.Usuario
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository para gestionar operaciones CRUD de usuarios
 * En una aplicación real, esto se conectaría a una base de datos o API
 */
class UsuariosRepository {

    // Simulación de base de datos en memoria
    private val usuariosInMemory = mutableListOf(
        Usuario(
            rut = "12.633.195-9",
            nombre = "Cristina Gonzalez",
            email = "m.escobar2@duocuc.cl",
            rol = "Usuario",
            estado = EstadoUsuario.ACTIVO,
            password = "123456"
        )
    )

    /**
     * Obtiene todos los usuarios
     */
    fun obtenerUsuarios(): Flow<List<Usuario>> = flow {
        emit(usuariosInMemory.toList())
    }

    /**
     * Obtiene un usuario por RUT
     */
    suspend fun obtenerUsuarioPorRut(rut: String): Usuario? {
        delay(100) // Simula latencia de red
        return usuariosInMemory.find { it.rut == rut }
    }

    /**
     * Crea un nuevo usuario
     */
    suspend fun crearUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            delay(200) // Simula latencia de red

            // Validar que no exista usuario con mismo RUT
            if (usuariosInMemory.any { it.rut == usuario.rut }) {
                return Result.failure(Exception("Ya existe un usuario con este RUT"))
            }

            usuariosInMemory.add(usuario)
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Actualiza un usuario existente
     */
    suspend fun actualizarUsuario(usuario: Usuario): Result<Usuario> {
        return try {
            delay(200) // Simula latencia de red

            val index = usuariosInMemory.indexOfFirst { it.rut == usuario.rut }
            if (index == -1) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            usuariosInMemory[index] = usuario
            Result.success(usuario)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Elimina un usuario por RUT
     */
    suspend fun eliminarUsuario(rut: String): Result<Unit> {
        return try {
            delay(200) // Simula latencia de red

            val removed = usuariosInMemory.removeIf { it.rut == rut }
            if (!removed) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Busca usuarios por nombre o RUT
     */
    fun buscarUsuarios(query: String): Flow<List<Usuario>> = flow {
        delay(100) // Simula latencia de red

        if (query.isBlank()) {
            emit(usuariosInMemory.toList())
        } else {
            val resultados = usuariosInMemory.filter { usuario ->
                usuario.nombre.contains(query, ignoreCase = true) ||
                        usuario.rut.contains(query, ignoreCase = true) ||
                        usuario.email.contains(query, ignoreCase = true)
            }
            emit(resultados)
        }
    }

    /**
     * Cambia el estado de un usuario
     */
    suspend fun cambiarEstadoUsuario(rut: String, nuevoEstado: EstadoUsuario): Result<Usuario> {
        return try {
            delay(150)

            val index = usuariosInMemory.indexOfFirst { it.rut == rut }
            if (index == -1) {
                return Result.failure(Exception("Usuario no encontrado"))
            }

            val usuarioActualizado = usuariosInMemory[index].copy(estado = nuevoEstado)
            usuariosInMemory[index] = usuarioActualizado

            Result.success(usuarioActualizado)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
