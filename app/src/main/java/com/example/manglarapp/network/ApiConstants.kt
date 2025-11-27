package com.example.manglarapp.network

/**
 * Constantes para la configuración de la API
 */
object ApiConstants {
    // TODO: Cambiar esta URL por la de tu servidor Spring Boot
    // Ejemplos:
    // - Para emulador Android: "http://10.0.2.2:8080/api/"
    // - Para dispositivo físico en misma red: "http://192.168.x.x:8080/api/"
    // - Para servidor remoto: "https://tuservidor.com/api/"
    const val BASE_URL = "http://10.0.2.2:8080/api/"

    // Timeouts
    const val CONNECT_TIMEOUT = 30L
    const val READ_TIMEOUT = 30L
    const val WRITE_TIMEOUT = 30L

    // Endpoints
    object Endpoints {
        // Usuarios
        const val USUARIOS = "usuarios"
        const val USUARIO_LOGIN = "usuarios/login"
        const val USUARIO_BY_RUT = "usuarios/{rut}"

        // Tareas
        const val TAREAS = "tareas"
        const val TAREA_BY_ID = "tareas/{id}"
        const val TAREA_TOMAR = "tareas/{id}/tomar"
        const val TAREA_COMPLETAR = "tareas/{id}/completar"
        const val TAREA_APROBAR = "tareas/{id}/aprobar"
        const val TAREA_RECHAZAR = "tareas/{id}/rechazar"
        const val TAREA_LIBERAR = "tareas/{id}/liberar"
        const val TAREAS_PENDIENTES = "tareas/pendientes-aprobacion"

        // Finanzas
        const val FINANZAS = "finanzas"
        const val FINANZAS_USUARIOS = "finanzas/usuarios"
        const val FINANZAS_GASTOS = "finanzas/gastos"
        const val FINANZAS_GASTOS_MES = "finanzas/gastos/{mes}"
        const val FINANZAS_ABONO = "finanzas/usuarios/{usuarioId}/abono"
    }
}
