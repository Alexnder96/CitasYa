package com.idat.citasya.Controller

import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.idat.citasya.R
import com.idat.citasya.model.ResponseHisCitas
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AdapterCitasNoti(private val citas: List<ResponseHisCitas>) : RecyclerView.Adapter<AdapterCitasNoti.ViewHolder>() {

    private val zonaLima = ZoneId.of("America/Lima")
    private val formatoFechaApi = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formatoHoraApi = DateTimeFormatter.ofPattern("HH:mm:ss")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.listacitasnoti, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = citas[position]

        holder.txtTipoEspe.text = cita.nombreespecialidad

        val esManana = try {
            LocalDate.parse(cita.fecha, formatoFechaApi) == LocalDate.now(zonaLima).plusDays(1)
        } catch (e: Exception) {
            false
        }

        val prefijo = if (esManana) {
            "Recuerda que mañana tienes cita con:\n"
        } else {
            val minutosRestantes = try {
                val horaCita = LocalTime.parse(cita.hora, formatoHoraApi)
                Duration.between(LocalTime.now(zonaLima), horaCita).toMinutes().coerceAtLeast(0)
            } catch (e: Exception) {
                null
            }
            if (minutosRestantes != null) {
                "Recuerda que hoy en ${formatearTiempoRestante(minutosRestantes)} tienes cita con:\n"
            } else {
                "Recuerda que hoy tienes cita con:\n"
            }
        }

        holder.txtCitaDetalle.text = construirMensaje(prefijo, cita.nombre_doctor, cita.nombreespecialidad, cita.nombre_sede)
    }

    // Menos de 60 minutos se muestra en minutos ("35 minutos"); de ahí en adelante se
    // expresa en horas, con los minutos sobrantes si los hay ("4 horas y 19 minutos"),
    // en vez de un número grande de minutos que cuesta más leer.
    private fun formatearTiempoRestante(minutosRestantes: Long): String {
        if (minutosRestantes < 60) {
            return "$minutosRestantes ${if (minutosRestantes == 1L) "minuto" else "minutos"}"
        }

        val horas = minutosRestantes / 60
        val minutos = minutosRestantes % 60
        val textoHoras = "$horas ${if (horas == 1L) "hora" else "horas"}"

        return if (minutos == 0L) {
            textoHoras
        } else {
            "$textoHoras y $minutos ${if (minutos == 1L) "minuto" else "minutos"}"
        }
    }

    // Arma "PREFIJO\nDOCTOR en el área de ESPECIALIDAD en la sede SEDE" con doctor/especialidad/sede
    // en negrita, sin tener que usar HTML ni tres TextViews distintos.
    private fun construirMensaje(prefijo: String, doctor: String, especialidad: String, sede: String): SpannableStringBuilder {
        val sb = SpannableStringBuilder()
        sb.append(prefijo)

        val inicioDoctor = sb.length
        sb.append(doctor)
        sb.setSpan(StyleSpan(Typeface.BOLD), inicioDoctor, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        sb.append(" en el área de ")

        val inicioEspecialidad = sb.length
        sb.append(especialidad)
        sb.setSpan(StyleSpan(Typeface.BOLD), inicioEspecialidad, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        sb.append(" en la ")

        val inicioSede = sb.length
        sb.append(sede)
        sb.setSpan(StyleSpan(Typeface.BOLD), inicioSede, sb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        return sb
    }

    override fun getItemCount(): Int {
        return citas.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTipoEspe: TextView = itemView.findViewById(R.id.txvTipoEspecialidad)
        val txtCitaDetalle: TextView = itemView.findViewById(R.id.txvDetalle)
    }
}