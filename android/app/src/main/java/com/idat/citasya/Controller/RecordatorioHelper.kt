package com.idat.citasya.Controller

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

// Programa (y cancela) el recordatorio local de una cita usando AlarmManager.
// No depende de que la app esté abierta ni del servidor: una vez programada, el sistema
// operativo se encarga de disparar RecordatorioCitaReceiver a la hora indicada.
object RecordatorioHelper {

    private const val HORAS_DE_ANTICIPACION = 2L
    private val zonaLima = ZoneId.of("America/Lima")
    private val formatoFechaApi = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formatoHoraApi = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val formatoFechaVisible = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val formatoHoraVisible = DateTimeFormatter.ofPattern("h:mm a", Locale("es", "PE"))

    // El mismo idcita/doctor+fecha+hora siempre genera el mismo requestCode: así, para cancelar
    // el recordatorio de una cita, no hace falta guardar nada más -- se recalcula igual.
    private fun requestCodeDe(iddoctor: Int, fecha: String, hora: String): Int {
        return "$iddoctor|$fecha|$hora".hashCode()
    }

    private fun pendingIntentDe(
        context: Context,
        requestCode: Int,
        idPaciente: Int,
        especialidad: String,
        doctor: String,
        fechaVisible: String,
        horaVisible: String,
        flagCrear: Boolean
    ): PendingIntent? {
        val intent = Intent(context, RecordatorioCitaReceiver::class.java).apply {
            putExtra("idPaciente", idPaciente)
            putExtra("especialidad", especialidad)
            putExtra("doctor", doctor)
            putExtra("fechaVisible", fechaVisible)
            putExtra("horaVisible", horaVisible)
            putExtra("requestCode", requestCode)
        }

        val flags = if (flagCrear) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        }

        return PendingIntent.getBroadcast(context, requestCode, intent, flags)
    }

    // Programa el recordatorio para 2 horas antes de la cita. Si esa hora ya pasó
    // (por ejemplo, reservaste una cita para dentro de una hora), simplemente no programa nada.
    fun programar(
        context: Context,
        idPaciente: Int,
        iddoctor: Int,
        especialidad: String,
        doctor: String,
        fecha: String,
        hora: String
    ) {
        try {
            val fechaHoraCita = LocalDateTime.of(LocalDate.parse(fecha, formatoFechaApi), LocalTime.parse(hora, formatoHoraApi))
                .atZone(zonaLima)
            val disparoEn = fechaHoraCita.minusHours(HORAS_DE_ANTICIPACION)

            if (disparoEn.isBefore(java.time.ZonedDateTime.now(zonaLima))) {
                return // ya no tiene sentido programar un recordatorio en el pasado
            }

            val requestCode = requestCodeDe(iddoctor, fecha, hora)
            val fechaVisible = fechaHoraCita.toLocalDate().format(formatoFechaVisible)
            val horaVisible = fechaHoraCita.toLocalTime().format(formatoHoraVisible)

            val pendingIntent = pendingIntentDe(
                context, requestCode, idPaciente, especialidad, doctor, fechaVisible, horaVisible, flagCrear = true
            ) ?: return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.set(AlarmManager.RTC_WAKEUP, disparoEn.toInstant().toEpochMilli(), pendingIntent)
        } catch (e: Exception) {
            // Si por algún motivo no se pudo programar (permiso de notificaciones denegado,
            // formato inesperado, etc.), la cita ya quedó reservada igual: solo se pierde el recordatorio.
            Log.w("RecordatorioHelper", "No se pudo programar el recordatorio: $e")
        }
    }

    // Cancela el recordatorio pendiente de una cita (se usa cuando el paciente cancela la cita
    // desde el historial). Si nunca se había programado, no hace nada.
    fun cancelar(context: Context, iddoctor: Int, fecha: String, hora: String) {
        try {
            val requestCode = requestCodeDe(iddoctor, fecha, hora)
            val pendingIntent = pendingIntentDe(
                context, requestCode, 0, "", "", "", "", flagCrear = false
            ) ?: return

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        } catch (e: Exception) {
            Log.w("RecordatorioHelper", "No se pudo cancelar el recordatorio: $e")
        }
    }
}
