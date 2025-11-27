package com.example.manglarapp.network

/**
 * Clase sellada para representar el resultado de una operación de red
 */
sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

/**
 * Extensión para manejar respuestas de Retrofit
 */
suspend fun <T, R> safeApiCall(
    apiCall: suspend () -> retrofit2.Response<T>,
    transform: (T) -> R
): NetworkResult<R> {
    return try {
        val response = apiCall()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                NetworkResult.Success(transform(body))
            } else {
                NetworkResult.Error("Respuesta vacía del servidor")
            }
        } else {
            val errorMessage = when (response.code()) {
                400 -> "Solicitud incorrecta"
                401 -> "No autorizado"
                403 -> "Acceso prohibido"
                404 -> "Recurso no encontrado"
                500 -> "Error del servidor"
                else -> "Error: ${response.message()}"
            }
            NetworkResult.Error(errorMessage, response.code())
        }
    } catch (e: Exception) {
        val errorMessage = when {
            e is java.net.UnknownHostException -> "Sin conexión a internet"
            e is java.net.SocketTimeoutException -> "Tiempo de espera agotado"
            e is java.io.IOException -> "Error de conexión: ${e.localizedMessage}"
            else -> "Error: ${e.localizedMessage ?: "Desconocido"}"
        }
        NetworkResult.Error(errorMessage)
    }
}

/**
 * Extensión para ejecutar código cuando el resultado es exitoso
 */
inline fun <T> NetworkResult<T>.onSuccess(action: (T) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Success) {
        action(data)
    }
    return this
}

/**
 * Extensión para ejecutar código cuando el resultado es error
 */
inline fun <T> NetworkResult<T>.onError(action: (String) -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Error) {
        action(message)
    }
    return this
}

/**
 * Extensión para ejecutar código cuando el resultado está cargando
 */
inline fun <T> NetworkResult<T>.onLoading(action: () -> Unit): NetworkResult<T> {
    if (this is NetworkResult.Loading) {
        action()
    }
    return this
}
