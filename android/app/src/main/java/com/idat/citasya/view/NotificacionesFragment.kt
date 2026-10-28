package com.idat.citasya.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.idat.citasya.R
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idat.citasya.Controller.AdapterCitasNoti
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.model.ResponseHisCitas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class NotificacionesFragment : Fragment() {

    // Misma zona horaria fija que en CitaNuevaActivity: evita que "hoy" o los minutos
    // restantes salgan mal calculados si el dispositivo tiene otra zona configurada.
    private val zonaLima = ZoneId.of("America/Lima")
    private val formatoFechaApi = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formatoHoraApi = DateTimeFormatter.ofPattern("HH:mm:ss")

    private var recyclerView: RecyclerView? = null
    private var idPaciente: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_notificaciones, container, false)

        idPaciente = arguments?.getInt("idPaciente")

        recyclerView = view.findViewById<RecyclerView>(R.id.rvNotif)
        recyclerView?.layoutManager = LinearLayoutManager(context)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Se recarga cada vez que el fragment vuelve a quedar visible: con ViewPager2
        // el fragment no se recrea al cambiar de pestaña y volver, solo se llama onResume.
        cargarNotificaciones()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        recyclerView = null
    }

    private fun cargarNotificaciones() {
        val pacienteId = idPaciente ?: return
        val rv = recyclerView ?: return
        val apiServiceCita = RetrofitClient.apiService

        // Llamar a la API usando Dispatchers.IO
        GlobalScope.launch(Dispatchers.IO) {
            val call = apiServiceCita.getCitas(pacienteId)

            call.enqueue(object : Callback<List<ResponseHisCitas>> {
                override fun onResponse(
                    call: Call<List<ResponseHisCitas>>,
                    response: Response<List<ResponseHisCitas>>
                ) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val citas = response.body() ?: emptyList()
                        // Citas de hoy que todavía no pasaron, más las de mañana (todo el día,
                        // ya que mañana completo sigue siendo futuro) -- ordenadas de la más
                        // próxima a la más lejana, igual que en la pestaña Citas.
                        val citasProximas = filtrarCitasDeHoyYManana(citas)
                            .sortedWith(compareBy({ it.fecha }, { it.hora }))
                        rv.adapter = AdapterCitasNoti(citasProximas)
                    } else {
                        Toast.makeText(
                            context,
                            "Error al obtener historial. Código de respuesta: ${response.code()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ResponseHisCitas>>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(context, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    private fun filtrarCitasDeHoyYManana(citas: List<ResponseHisCitas>): List<ResponseHisCitas> {
        val hoy = LocalDate.now(zonaLima)
        val manana = hoy.plusDays(1)
        val ahora = LocalTime.now(zonaLima)

        return citas.filter { cita ->
            if (cita.estado.lowercase() == "cancelada") return@filter false

            val fechaCita = try {
                LocalDate.parse(cita.fecha, formatoFechaApi)
            } catch (e: Exception) {
                return@filter false
            }

            when (fechaCita) {
                hoy -> {
                    val horaCita = try {
                        LocalTime.parse(cita.hora, formatoHoraApi)
                    } catch (e: Exception) {
                        return@filter false
                    }
                    horaCita.isAfter(ahora) // ya no tiene sentido recordar una cita de hoy que ya pasó
                }
                manana -> true // el día de mañana completo sigue siendo futuro
                else -> false
            }
        }
    }
}