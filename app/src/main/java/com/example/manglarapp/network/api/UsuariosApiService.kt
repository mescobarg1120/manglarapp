package com.example.manglarapp.network.api

import com.example.manglarapp.network.dto.UsuarioDto
import retrofit2.http.*

interface UsuariosApiService {

    @GET("usuarios")
    suspend fun obtenerUsuarios(): List<UsuarioDto>

    @GET("usuarios/{rut}")
    suspend fun obtenerUsuarioPorRut(@Path("rut") rut: String): UsuarioDto

    @POST("usuarios")
    suspend fun crearUsuario(@Body usuario: UsuarioDto): UsuarioDto

    @PUT("usuarios/{rut}")
    suspend fun actualizarUsuario(
        @Path("rut") rut: String,
        @Body usuario: UsuarioDto
    ): UsuarioDto

    @DELETE("usuarios/{rut}")
    suspend fun eliminarUsuario(@Path("rut") rut: String)

    @GET("usuarios/buscar")
    suspend fun buscarUsuarios(@Query("query") query: String): List<UsuarioDto>

    @PATCH("usuarios/{rut}/estado")
    suspend fun cambiarEstadoUsuario(
        @Path("rut") rut: String,
        @Query("estado") estado: String
    ): UsuarioDto
}