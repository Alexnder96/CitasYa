package com.idat.citasya.view

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.idat.citasya.Controller.AdapterHistorialCita
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RecordatorioHelper
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.R
import com.idat.citasya.databinding.FragmentCitasBinding
import com.idat.citasya.model.ResponseHisCitas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CitasFragment : Fragment() {

    private var _binding: FragmentCitasBinding? = null
    private val binding get() = _binding!!

    private var idPaciente: Int = 0
    private var adapter: AdapterHistorialCita? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCitasBinding.inflate(inflater, container, false)
        val view = binding.root

        idPaciente = arguments?.getInt("idPaciente") ?: 0

        binding.AddCitas.setOnClickListener {
            val intent = Intent(activity, CitaNuevaActivity::class.java)
            intent.putExtra("idPaciente", idPaciente)
            startActivity(intent)
        }

        binding.rvCitaActiva.layoutManager = LinearLayoutManager(requireContext())

        return view
    }

    override fun onResume() {
        super.onResume()
        // Se recarga cada vez que el fragment vuelve a quedar visible (por ejemplo,
        // al volver de CitaNuevaActivity después de reservar), no solo la primera vez.
        cargarCitas()
    }

    private fun cargarCitas() {
        if (_binding == null) return

        // Llamar a la API usando Dispatchers.IO
        GlobalScope.launch(Dispatchers.IO) {
            val apiServiceCita = RetrofitClient.apiService
            val call = apiServiceCita.getCitas(idPaciente)

            call.enqueue(object : Callback<List<ResponseHisCitas>> {
                override fun onResponse(
                    call: Call<List<ResponseHisCitas>>,
                    response: Response<List<ResponseHisCitas>>
                ) {
                    if (_binding == null) return
                    if (response.isSuccessful) {
                        val citas = response.body() ?: emptyList()
                        // La más próxima primero (no la más lejana), a diferencia del historial
                        // que muestra lo más reciente primero pero mirando hacia el pasado.
                        val citasFuturas = filterFutureCitas(citas).sortedWith(compareBy({ it.fecha }, { it.hora }))

                        val adaptadorActual = adapter
                        if (adaptadorActual == null) {
                            val nuevoAdapter = AdapterHistorialCita(citasFuturas) { cita -> confirmarCancelacion(cita) }
                            adapter = nuevoAdapter
                            binding.rvCitaActiva.adapter = nuevoAdapter
                        } else {
                            adaptadorActual.actualizarLista(citasFuturas)
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error al obtener historial",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<List<ResponseHisCitas>>, t: Throwable) {
                    if (_binding == null) return
                    Toast.makeText(
                        requireContext(),
                        ErrorUtils.mensajeDeConexion(t),
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
        }
    }

    private fun confirmarCancelacion(cita: ResponseHisCitas) {
        val ctx = context ?: return
        AlertDialog.Builder(ctx)
            .setTitle("Cancelar cita")
            .setMessage("¿Seguro que deseas cancelar tu cita de ${cita.nombreespecialidad} el ${cita.fecha}?")
            .setPositiveButton("Sí, cancelar") { _, _ -> cancelarCita(cita) }
            .setNegativeButton("No", null)
            .show()
    }

    private fun cancelarCita(cita: ResponseHisCitas) {
        val apiServiceCita = RetrofitClient.apiService

        GlobalScope.launch(Dispatchers.IO) {
            val call = apiServiceCita.cancelarCita(cita.idcita)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (_binding == null) return
                    val ctx = context
                    if (response.isSuccessful) {
                        if (ctx != null) {
                            RecordatorioHelper.cancelar(ctx, cita.iddoctor, cita.fecha, cita.hora)
                            Toast.makeText(ctx, "Cita cancelada", Toast.LENGTH_SHORT).show()
                        }
                        cargarCitas()
                    } else if (ctx != null) {
                        Toast.makeText(ctx, "No se pudo cancelar la cita", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    if (_binding == null) return
                    Toast.makeText(requireContext(), ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adapter = null
        _binding = null
    }


    private fun filterFutureCitas(citas: List<ResponseHisCitas>): List<ResponseHisCitas> {
        val citasFuturas = ArrayList<ResponseHisCitas>()

        val today = Calendar.getInstance()
        today.add(Calendar.DAY_OF_MONTH, -1)

        for (cita in citas) {
            // Las citas canceladas no deben aparecer en "Citas activas" (sí siguen
            // apareciendo en el Historial, ahí con su badge en rojo).
            if (cita.estado.lowercase() == "cancelada") continue

            val citaDate = convertStringToCalendar(cita.fecha)

            if (citaDate != null && !citaDate.before(today)) {
                citasFuturas.add(cita)
            }
        }

        return citasFuturas
    }

    private fun convertStringToCalendar(dateString: String): Calendar? {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = sdf.parse(dateString)

            val calendar = Calendar.getInstance()
            calendar.time = date

            calendar
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }
}