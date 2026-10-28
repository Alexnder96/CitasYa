package com.idat.citasya.Controller

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.idat.citasya.R
import com.idat.citasya.view.IniActivity

// Se dispara cuando llega la hora programada del recordatorio (algunas horas antes de la cita)
// y muestra la notificación local. No depende del servidor: ya tiene toda la info que necesita
// en el propio Intent, guardada al momento de programarla.
class RecordatorioCitaReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val especialidad = intent.getStringExtra("especialidad") ?: "tu especialidad"
        val doctor = intent.getStringExtra("doctor") ?: ""
        val fechaVisible = intent.getStringExtra("fechaVisible") ?: ""
        val horaVisible = intent.getStringExtra("horaVisible") ?: ""
        val idPaciente = intent.getIntExtra("idPaciente", 0)
        val requestCode = intent.getIntExtra("requestCode", 0)

        val canalId = "recordatorios_citas"

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canalExistente = notificationManager.getNotificationChannel(canalId)
            if (canalExistente == null) {
                val canal = NotificationChannel(
                    canalId,
                    "Recordatorios de citas",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Avisos antes de una cita médica agendada"
                }
                notificationManager.createNotificationChannel(canal)
            }
        }

        val intentAbrir = Intent(context, IniActivity::class.java).apply {
            putExtra("idPaciente", idPaciente)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntentAbrir = PendingIntent.getActivity(
            context,
            requestCode,
            intentAbrir,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val texto = if (doctor.isNotBlank()) {
            "Tienes una cita de $especialidad con $doctor el $fechaVisible a las $horaVisible"
        } else {
            "Tienes una cita de $especialidad el $fechaVisible a las $horaVisible"
        }

        val notificacion = NotificationCompat.Builder(context, canalId)
            .setSmallIcon(R.drawable.icon_citas)
            .setContentTitle("Recordatorio de cita")
            .setContentText(texto)
            .setStyle(NotificationCompat.BigTextStyle().bigText(texto))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntentAbrir)
            .build()

        notificationManager.notify(requestCode, notificacion)
    }
}
