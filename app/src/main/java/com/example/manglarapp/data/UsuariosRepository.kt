package com.example.manglarapp.repository

import com.example.manglarapp.model.Usuario
import com.example.manglarapp.network.RetrofitClient
import com.example.manglarapp.network.dto.toDto
import com.example.manglarapp.network.dto.toUsuario

class UsuariosRepository {

    private val api = RetrofitClient.usuariosApi

    suspend fun obtenerTodos(): List<Usuario> {
        // ✅ Convierte List<UsuarioDto> → List<Usuario>
        return api.obtenerUsuarios().map { it.toUsuario() }
    }

    suspend fun obtenerPorRut(rut: String): Usuario {
        // ✅ Convierte UsuarioDto → Usuario
        return api.obtenerUsuarioPorRut(rut).toUsuario()
    }

    suspend fun crear(usuario: Usuario): Usuario {
        // ✅ Convierte Usuario → UsuarioDto, envía, y convierte la respuesta
        val dto = usuario.toDto()
        val respuesta = api.crearUsuario(dto)
        return respuesta.toUsuario()
    }

    suspend fun actualizar(rut: String, usuario: Usuario): Usuario {
        val dto = usuario.toDto()
        val respuesta = api.actualizarUsuario(rut, dto)
        return respuesta.toUsuario()
    }

    suspend fun eliminar(rut: String) {
        api.eliminarUsuario(rut)
    }
}
