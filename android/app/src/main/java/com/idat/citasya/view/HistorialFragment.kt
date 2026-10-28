package com.idat.citasya.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import com.idat.citasya.R
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.idat.citasya.Controller.AdapterHistorialCita
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RecordatorioHelper
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.model.ResponseHisCitas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HistorialFragment : Fragment() {

    private var idPaciente: Int? = null
    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var txtMensaje: TextView
    private var adapter: AdapterHistorialCita? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el diseño para este fragmento
        val view = inflater.inflate(R.layout.fragment_historial, container, false)

        idPaciente = arguments?.getInt("idPaciente")

        recyclerView = view.findViewById(R.id.rvHistorialCitas)
        recyclerView.layoutManager = LinearLayoutManager(context)
        progressBar = view.findViewById(R.id.progressHistorial)
        txtMensaje = view.findViewById(R.id.txtMensajeHistorial)

        return view
    }

    override fun onResume() {
        super.onResume()
        // Se recarga cada vez que el fragment vuelve a quedar visible: con ViewPager2
        // el fragment no se recrea al cambiar de pestaña y volver, solo se llama onResume.
        cargarHistorial()
    }

    private fun cargarHistorial() {
        val idPac = idPaciente ?: return
        val apiServiceCita = RetrofitClient.apiService

        progressBar.visibility = View.VISIBLE
        txtMensaje.visibility = View.GONE
        recyclerView.visibility = View.GONE

        GlobalScope.launch(Dispatchers.IO) {
            val call = apiServiceCita.getCitas(idPac)

            call.enqueue(object : Callback<List<ResponseHisCitas>> {
                override fun onResponse(
                    call: Call<List<ResponseHisCitas>>,
                    response: Response<List<ResponseHisCitas>>
                ) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val citas = response.body() ?: emptyList()
                        if (citas.isEmpty()) {
                            txtMensaje.text = "Todavía no tienes citas registradas."
                            txtMensaje.visibility = View.VISIBLE
                            recyclerView.visibility = View.GONE
                        } else {
                            recyclerView.visibility = View.VISIBLE
                            val adaptadorActual = adapter
                            if (adaptadorActual == null) {
                                val nuevoAdapter = AdapterHistorialCita(citas) { cita -> confirmarCancelacion(cita) }
                                adapter = nuevoAdapter
                                recyclerView.adapter = nuevoAdapter
                            } else {
                                adaptadorActual.actualizarLista(citas)
                            }
                        }
                    } else {
                        txtMensaje.text = "No se pudo obtener tu historial de citas."
                        txtMensaje.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }

                override fun onFailure(call: Call<List<ResponseHisCitas>>, t: Throwable) {
                    if (!isAdded) return
                    progressBar.visibility = View.GONE
                    txtMensaje.text = ErrorUtils.mensajeDeConexion(t)
                    txtMensaje.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
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
                    if (!isAdded) return
                    val ctx = context
                    if (response.isSuccessful) {
                        if (ctx != null) {
                            RecordatorioHelper.cancelar(ctx, cita.iddoctor, cita.fecha, cita.hora)
                            Toast.makeText(ctx, "Cita cancelada", Toast.LENGTH_SHORT).show()
                        }
                        cargarHistorial()
                    } else if (ctx != null) {
                        Toast.makeText(ctx, "No se pudo cancelar la cita", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    if (!isAdded) return
                    val ctx = context ?: return
                    Toast.makeText(ctx, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_LONG).show()
                }
            })
        }
    }
}
