package com.example.manglarapp.network.api

import com.example.manglarapp.network.dto.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Servicio API para operaciones de Usuarios
 */
interface UsuariosApiService {

    /**
     * Obtiene todos los usuarios
     */
    @GET("usuarios")
    suspend fun obtenerUsuarios(): Response<List<UsuarioDto>>

    /**
     * Obtiene un usuario por RUT
     */
    @GET("usuarios/{rut}")
    suspend fun obtenerUsuarioPorRut(@Path("rut") rut: String): Response<UsuarioDto>

    /**
     * Crea un nuevo usuario
     */
    @POST("usuarios")
    suspend fun crearUsuario(@Body request: UsuarioRequest): Response<UsuarioDto>

    /**
     * Actualiza un usuario existente
     */
    @PUT("usuarios/{rut}")
    suspend fun actualizarUsuario(
        @Path("rut") rut: String,
        @Body request: UsuarioRequest
    ): Response<UsuarioDto>

    /**
     * Elimina un usuario
     */
    @DELETE("usuarios/{rut}")
    suspend fun eliminarUsuario(@Path("rut") rut: String): Response<Unit>

    /**
     * Busca usuarios por query
     */
    @GET("usuarios/buscar")
    suspend fun buscarUsuarios(@Query("q") query: String): Response<List<UsuarioDto>>

    /**
     * Login de usuario
     */
    @POST("usuarios/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    /**
     * Cambia el estado de un usuario
     */
    @PATCH("usuarios/{rut}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("rut") rut: String,
        @Query("estado") estado: String
    ): Response<UsuarioDto>
}
