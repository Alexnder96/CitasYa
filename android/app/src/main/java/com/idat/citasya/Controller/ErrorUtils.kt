package com.idat.citasya.Controller

import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

// Traduce las fallas de red (onFailure de Retrofit) a un mensaje entendible para el paciente,
// en vez de mostrar el texto crudo de la excepción ("java.net.ConnectException: ...").
object ErrorUtils {

    fun mensajeDeConexion(t: Throwable): String {
        return when (t) {
            is UnknownHostException, is ConnectException ->
                "No se pudo conectar con el servidor. Revisa tu conexión a internet o intenta más tarde."
            is SocketTimeoutException ->
                "El servidor está tardando en responder. Intenta nuevamente en unos segundos."
            else ->
                "Servidor no disponible en este momento. Intenta nuevamente más tarde."
        }
    }
}
