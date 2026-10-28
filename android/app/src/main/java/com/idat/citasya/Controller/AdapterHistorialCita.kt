package com.idat.citasya.Controller

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.idat.citasya.R
import com.idat.citasya.model.ResponseHisCitas
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class AdapterHistorialCita(
    private var citas: List<ResponseHisCitas>,
    private val onCancelar: (ResponseHisCitas) -> Unit
) : RecyclerView.Adapter<AdapterHistorialCita.ViewHolder>() {

    // El backend manda fecha como yyyy-MM-dd y hora como HH:mm:ss; en las tarjetas
    // se muestran más amigables: dd-MM-yyyy y sin segundos.
    private val formatoFechaApi = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formatoFechaVisible = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    private val formatoHoraApi = DateTimeFormatter.ofPattern("HH:mm:ss")
    private val formatoHoraVisible = DateTimeFormatter.ofPattern("HH:mm")

    // Misma zona horaria fija que en el resto de la app, para comparar correctamente
    // si una cita ya pasó sin depender de la zona configurada en el dispositivo.
    private val zonaLima = ZoneId.of("America/Lima")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.listahistorialcitas, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val cita = citas[position]
        holder.txtTipoCita.text = cita.nombreespecialidad
        holder.txtCitaFecha.text = try {
            LocalDate.parse(cita.fecha, formatoFechaApi).format(formatoFechaVisible)
        } catch (e: Exception) {
            cita.fecha
        }
        holder.txtCitaHora.text = try {
            LocalTime.parse(cita.hora, formatoHoraApi).format(formatoHoraVisible)
        } catch (e: Exception) {
            cita.hora
        }
        holder.txtCitaDoctor.text = cita.nombre_doctor
        holder.txtCitaSede.text = cita.nombre_sede

        val (etiqueta, color) = when (cita.estado.lowercase()) {
            "confirmada" -> "CONFIRMADA" to Color.parseColor("#4CAF50")
            "cancelada" -> "CANCELADA" to Color.parseColor("#D32F2F")
            "atendida" -> "ATENDIDA" to Color.parseColor("#1976D2")
            "pendiente" -> "PENDIENTE" to Color.parseColor("#F9A825")
            else -> cita.estado.uppercase() to Color.parseColor("#757575")
        }
        holder.txtEstado.text = etiqueta
        (holder.txtEstado.background as? GradientDrawable)?.setColor(color)

        // Solo se puede cancelar una cita que todavía no pasó ni fue cancelada/atendida.
        val noHaPasado = try {
            val fechaHoraCita = LocalDateTime.of(
                LocalDate.parse(cita.fecha, formatoFechaApi),
                LocalTime.parse(cita.hora, formatoHoraApi)
            )
            fechaHoraCita.isAfter(LocalDateTime.now(zonaLima))
        } catch (e: Exception) {
            true // si no se pudo parsear la fecha, no bloqueamos el botón por un dato raro
        }
        val esCancelable = cita.estado.lowercase() in listOf("confirmada", "pendiente") && noHaPasado
        holder.btnCancelar.visibility = if (esCancelable) View.VISIBLE else View.GONE
        holder.btnCancelar.setOnClickListener { onCancelar(cita) }
    }

    override fun getItemCount(): Int {
        return citas.size
    }

    fun actualizarLista(nuevaLista: List<ResponseHisCitas>) {
        citas = nuevaLista
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtTipoCita: TextView = itemView.findViewById(R.id.txtTipoCitaHis)
        val txtCitaFecha: TextView = itemView.findViewById(R.id.txtCitaFechaHis)
        val txtCitaHora: TextView = itemView.findViewById(R.id.txtCitaHoraHis)
        val txtCitaDoctor: TextView = itemView.findViewById(R.id.txtCitaDoctorHis)
        val txtCitaSede: TextView = itemView.findViewById(R.id.txtCitaSedeHis)
        val txtEstado: TextView = itemView.findViewById(R.id.txtEstadoHis)
        val btnCancelar: Button = itemView.findViewById(R.id.btnCancelarCitaHis)
    }
}