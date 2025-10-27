package com.example.manglarapp.domain.model

import java.util.UUID

data class Gasto(
    val id: String = UUID.randomUUID().toString(),
    val nombre: String,
    val fecha: String,
    val valor: Double
)

data class ResumenUsuario(
    val usuario: String,
    val puntos: Int,
    val abono: Double,
    val deuda: Double
)