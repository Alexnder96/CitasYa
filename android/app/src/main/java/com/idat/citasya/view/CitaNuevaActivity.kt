package com.idat.citasya.view

import android.Manifest
import android.app.AlertDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.graphics.Paint
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RecordatorioHelper
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.R
import com.idat.citasya.databinding.ActivityCitaNuevaBinding
import com.idat.citasya.model.ResponseDoctor
import com.idat.citasya.model.ResponseEspecialidad
import com.idat.citasya.model.ResponseGuardarCita
import com.idat.citasya.model.ResponsePaciente
import com.idat.citasya.model.ResponseSlotOcupado
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CitaNuevaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCitaNuevaBinding

    // Especialidades y doctores activos traídos de la API (ya no hardcodeados)
    private var especialidades: List<ResponseEspecialidad> = emptyList()
    private var doctores: List<ResponseDoctor> = emptyList()
    private var doctoresFiltrados: List<ResponseDoctor> = emptyList()

    private var selectedIdEspecialidad: Int = 0
    private var selectedId: Int = 0
    private lateinit var selectedName: String

    // La sede NO se elige al reservar: es la sede fija del paciente (se trae de su perfil)
    private var idSedePaciente: Int = 0
    private var idPacienteActual: Int = 0

    // Horario de atención: cada 20 minutos, de 8:00 a 16:40 (última cita, termina a las 5:00pm)
    private val horaInicio = LocalTime.of(8, 0)
    private val horaLimite = LocalTime.of(16, 40)

    // Fijamos la zona horaria de Perú en vez de usar la del dispositivo/emulador:
    // si el celular queda con otra zona (ej. UTC), "hoy" podía calcularse mal.
    private val zonaLima = ZoneId.of("America/Lima")

    private val formatoFechaVisible = DateTimeFormatter.ofPattern("dd/MM/yyyy")
    private val formatoFechaApi = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val formatoHoraApi = DateTimeFormatter.ofPattern("HH:mm:ss")

    // Chip de horario elegido (uno solo, aunque estén repartidos en varias filas por día)
    private val chipsDeHorario = mutableListOf<Chip>()
    private var selectedFecha: String? = null
    private var selectedHora: String? = null

    // Contador para descartar respuestas de disponibilidad que ya quedaron obsoletas
    // (si el usuario cambia de médico o de filtro antes de que responda la anterior)
    private var solicitudDisponibilidadActual = 0

    // Cuenta cuántas llamadas de red siguen en curso, para mostrar/ocultar el indicador
    // de carga sin que una respuesta rápida apague el spinner de otra que sigue pendiente.
    private var cargasPendientes = 0

    private val solicitarPermisoNotificaciones =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* si lo niega, la cita igual se reserva, solo no habrá recordatorio */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCitaNuevaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        idPacienteActual = intent.getIntExtra("idPaciente", 0)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            solicitarPermisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        cargarSedeDelPaciente(idPacienteActual)
        cargarEspecialidades()
        cargarDoctoresActivos()

        binding.chipGroupFiltro.setOnCheckedStateChangeListener { _, _ -> cargarDisponibilidad() }
        cargarDisponibilidad()

        binding.btnReservarCita.setOnClickListener {
            if (selectedIdEspecialidad == 0) {
                Toast.makeText(this@CitaNuevaActivity, "Selecciona una especialidad", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (selectedId == 0) {
                Toast.makeText(this@CitaNuevaActivity, "Selecciona un médico", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (idSedePaciente == 0) {
                Toast.makeText(this@CitaNuevaActivity, "No se pudo obtener tu sede, intenta de nuevo", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val fecha = selectedFecha
            val hora = selectedHora
            if (fecha == null || hora == null) {
                Toast.makeText(this@CitaNuevaActivity, "Selecciona un horario disponible", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            confirmarYReservar(fecha, hora)
        }
    }

    // Antes de reservar, resume la elección (médico, especialidad, fecha y hora) y pide confirmación,
    // para evitar que un toque accidental en un chip termine reservando el horario equivocado.
    private fun confirmarYReservar(fecha: String, hora: String) {
        val nombreEspecialidad = especialidades.firstOrNull { it.idespecialidad == selectedIdEspecialidad }?.nombreespecialidad ?: ""
        val fechaVisible = try {
            LocalDate.parse(fecha, formatoFechaApi).format(formatoFechaVisible)
        } catch (e: Exception) {
            fecha
        }
        val horaVisible = try {
            LocalTime.parse(hora, formatoHoraApi).format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            hora
        }

        AlertDialog.Builder(this)
            .setTitle("Confirmar cita")
            .setMessage(
                "Especialidad: $nombreEspecialidad\n" +
                    "Médico: $selectedName\n" +
                    "Fecha: $fechaVisible\n" +
                    "Hora: $horaVisible\n\n" +
                    "¿Deseas reservar esta cita?"
            )
            .setPositiveButton("Confirmar") { _, _ -> reservarCita(fecha, hora, nombreEspecialidad) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun reservarCita(fecha: String, hora: String, nombreEspecialidad: String) {
        val nuevacita = ResponseGuardarCita(fecha, hora, selectedIdEspecialidad, idPacienteActual, selectedId, idSedePaciente)
        val apiServiceCita = RetrofitClient.apiService

        mostrarCargando()

        // Llamar a la API usando Dispatchers.IO
        GlobalScope.launch(Dispatchers.IO) {
            val call = apiServiceCita.guardarcita(nuevacita)
            call.enqueue(object : Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    runOnUiThread {
                        ocultarCargando()
                        if (response.isSuccessful) {
                            Toast.makeText(this@CitaNuevaActivity, "Cita reservada con éxito", Toast.LENGTH_SHORT).show()

                            RecordatorioHelper.programar(
                                this@CitaNuevaActivity, idPacienteActual, selectedId, nombreEspecialidad, selectedName, fecha, hora
                            )

                            finish()
                        } else if (response.code() == 409) {
                            // Alguien más tomó ese horario justo antes -- refrescamos la disponibilidad
                            // para que se vea deshabilitado y el usuario elija otro.
                            val mensaje = try {
                                JSONObject(response.errorBody()?.string() ?: "").optString(
                                    "error", "Ese horario ya fue tomado por otro paciente."
                                )
                            } catch (e: Exception) {
                                "Ese horario ya fue tomado por otro paciente."
                            }
                            Toast.makeText(this@CitaNuevaActivity, mensaje, Toast.LENGTH_LONG).show()
                            cargarDisponibilidad()
                        } else {
                            Toast.makeText(this@CitaNuevaActivity, "Error al reservar la cita", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    runOnUiThread {
                        ocultarCargando()
                        Toast.makeText(this@CitaNuevaActivity, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_LONG).show()
                    }
                }
            })
        }
    }

    // Suma/resta llamadas de red en curso y muestra el spinner solo mientras haya al menos una pendiente.
    private fun mostrarCargando() {
        cargasPendientes++
        binding.progressDisponibilidad.visibility = View.VISIBLE
    }

    private fun ocultarCargando() {
        cargasPendientes = (cargasPendientes - 1).coerceAtLeast(0)
        if (cargasPendientes == 0) {
            binding.progressDisponibilidad.visibility = View.GONE
        }
    }

    // Arma la lista de días a mostrar según el chip de filtro elegido (esta semana / este mes),
    // sin sábados ni domingos porque esos días no se atiende.
    private fun obtenerDiasSegunFiltro(): List<LocalDate> {
        val hoy = LocalDate.now(zonaLima)
        val rango = when (binding.chipGroupFiltro.checkedChipId) {
            R.id.chipEsteMes -> {
                val finMes = hoy.withDayOfMonth(hoy.lengthOfMonth())
                generarRangoDeDias(hoy, finMes)
            }
            else -> generarRangoDeDias(hoy, hoy.plusDays(6)) // "Esta semana" = próximos 7 días
        }
        return rango.filter { it.dayOfWeek != DayOfWeek.SATURDAY && it.dayOfWeek != DayOfWeek.SUNDAY }
    }

    private fun generarRangoDeDias(inicio: LocalDate, fin: LocalDate): List<LocalDate> {
        val lista = mutableListOf<LocalDate>()
        var actual = inicio
        while (!actual.isAfter(fin)) {
            lista.add(actual)
            actual = actual.plusDays(1)
        }
        return lista
    }

    // Franjas de 20 en 20 minutos entre 8:00 y 16:40. Si el día es hoy, oculta las horas ya pasadas.
    private fun generarHorariosDisponibles(dia: LocalDate): List<LocalTime> {
        val horarios = mutableListOf<LocalTime>()
        var t = horaInicio
        val esHoy = dia == LocalDate.now(zonaLima)
        val ahora = LocalTime.now(zonaLima)

        while (!t.isAfter(horaLimite)) {
            if (!esHoy || t.isAfter(ahora)) {
                horarios.add(t)
            }
            t = t.plusMinutes(20)
        }
        return horarios
    }

    // Si ya hay un médico elegido, pide al backend qué horarios tiene ocupados en el rango de
    // días visible y recién ahí dibuja los chips (deshabilitando los que ya estén tomados).
    // Si todavía no hay médico elegido, solo muestra un aviso -- no tiene sentido listar horarios
    // sin saber de quién, porque la disponibilidad depende del médico.
    private fun cargarDisponibilidad() {
        val idDoctorActual = selectedId
        val miSolicitud = ++solicitudDisponibilidadActual

        if (idDoctorActual == 0) {
            renderizarDias(emptySet())
            return
        }

        val dias = obtenerDiasSegunFiltro()
        if (dias.isEmpty()) {
            renderizarDias(emptySet())
            return
        }

        val desde = dias.first().format(formatoFechaApi)
        val hasta = dias.last().format(formatoFechaApi)
        val apiServiceCita = RetrofitClient.apiService

        mostrarCargando()

        GlobalScope.launch(Dispatchers.IO) {
            val call = apiServiceCita.getHorariosOcupados(idDoctorActual, desde, hasta)
            call.enqueue(object : Callback<List<ResponseSlotOcupado>> {
                override fun onResponse(call: Call<List<ResponseSlotOcupado>>, response: Response<List<ResponseSlotOcupado>>) {
                    if (miSolicitud != solicitudDisponibilidadActual) { runOnUiThread { ocultarCargando() }; return } // quedó vieja, se ignora
                    runOnUiThread {
                        ocultarCargando()
                        val ocupados = if (response.isSuccessful) {
                            (response.body() ?: emptyList()).map { "${it.fecha}|${it.hora}" }.toSet()
                        } else {
                            emptySet()
                        }
                        renderizarDias(ocupados)
                    }
                }

                override fun onFailure(call: Call<List<ResponseSlotOcupado>>, t: Throwable) {
                    if (miSolicitud != solicitudDisponibilidadActual) { runOnUiThread { ocultarCargando() }; return }
                    runOnUiThread {
                        ocultarCargando()
                        Toast.makeText(this@CitaNuevaActivity, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_SHORT).show()
                        renderizarDias(emptySet())
                    }
                }
            })
        }
    }

    // Dibuja, para cada día del rango elegido, su encabezado y sus chips de horario.
    // "ocupados" trae claves "yyyy-MM-dd|HH:mm:ss" -- esos chips quedan deshabilitados.
    private fun renderizarDias(ocupados: Set<String>) {
        binding.contenedorDias.removeAllViews()
        chipsDeHorario.clear()
        selectedFecha = null
        selectedHora = null

        if (selectedId == 0) {
            val aviso = TextView(this).apply {
                text = "Selecciona una especialidad y un médico para ver los horarios disponibles."
                setPadding(0, 24, 0, 0)
            }
            binding.contenedorDias.addView(aviso)
            return
        }

        val dias = obtenerDiasSegunFiltro()
        val hoy = LocalDate.now(zonaLima)
        var seMostroAlgo = false

        for (dia in dias) {
            val horarios = generarHorariosDisponibles(dia)
            if (horarios.isEmpty()) continue
            seMostroAlgo = true

            val nombreDia = if (dia == hoy) {
                "Hoy"
            } else {
                dia.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
                    .replaceFirstChar { it.uppercase() }
            }

            val tituloDia = TextView(this).apply {
                text = "$nombreDia · ${dia.format(formatoFechaVisible)}"
                textSize = 16f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, 32, 0, 8)
            }
            binding.contenedorDias.addView(tituloDia)

            // Una sola fila de chips por día, que se puede arrastrar hacia el lado
            // (ChipGroup normal envuelve a varias líneas; para fila única + scroll
            // horizontal hace falta un LinearLayout horizontal dentro de un HorizontalScrollView).
            val filaHorarios = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            }

            for (hora in horarios) {
                val claveSlot = "${dia.format(formatoFechaApi)}|${hora.format(formatoHoraApi)}"
                val estaOcupado = claveSlot in ocupados

                val chip = Chip(this@CitaNuevaActivity).apply {
                    text = String.format(Locale.getDefault(), "%d:%02d hs", hora.hour, hora.minute)
                    isCheckable = true
                    isClickable = !estaOcupado
                    isEnabled = !estaOcupado
                    if (estaOcupado) {
                        alpha = 0.5f
                        paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    }
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                    ).apply { marginEnd = dpToPx(8) }
                    if (!estaOcupado) {
                        setOnClickListener {
                            chipsDeHorario.forEach { it.isChecked = false }
                            this.isChecked = true
                            selectedFecha = dia.format(formatoFechaApi)
                            selectedHora = hora.format(formatoHoraApi)
                        }
                    }
                }
                chipsDeHorario.add(chip)
                filaHorarios.addView(chip)
            }

            val scrollHorarios = HorizontalScrollView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                isHorizontalScrollBarEnabled = false
                addView(filaHorarios)
            }
            binding.contenedorDias.addView(scrollHorarios)
        }

        if (!seMostroAlgo) {
            val vacio = TextView(this).apply {
                text = "No hay horarios disponibles en este rango."
                setPadding(0, 24, 0, 0)
            }
            binding.contenedorDias.addView(vacio)
        }
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    // La sede es fija por paciente: se lee de su propio registro, no se elige acá
    private fun cargarSedeDelPaciente(idPac: Int) {
        val apiService = RetrofitClient.apiService

        mostrarCargando()

        GlobalScope.launch(Dispatchers.IO) {
            val call = apiService.getPaciente(idPac)
            call.enqueue(object : Callback<ResponsePaciente> {
                override fun onResponse(call: Call<ResponsePaciente>, response: Response<ResponsePaciente>) {
                    runOnUiThread {
                        ocultarCargando()
                        if (response.isSuccessful) {
                            idSedePaciente = response.body()?.idsede ?: 0
                        } else {
                            Toast.makeText(this@CitaNuevaActivity, "No se pudo obtener tu sede", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<ResponsePaciente>, t: Throwable) {
                    runOnUiThread {
                        ocultarCargando()
                        Toast.makeText(this@CitaNuevaActivity, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun cargarEspecialidades() {
        val apiServiceCita = RetrofitClient.apiService

        mostrarCargando()

        GlobalScope.launch(Dispatchers.IO) {
            val call = apiServiceCita.getEspecialidades()
            call.enqueue(object : Callback<List<ResponseEspecialidad>> {
                override fun onResponse(call: Call<List<ResponseEspecialidad>>, response: Response<List<ResponseEspecialidad>>) {
                    runOnUiThread {
                        ocultarCargando()
                        if (response.isSuccessful) {
                            especialidades = response.body() ?: emptyList()

                            val nombres = especialidades.map { it.nombreespecialidad }
                            val adapter = ArrayAdapter(this@CitaNuevaActivity, android.R.layout.simple_spinner_item, nombres)
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                            binding.spinnerEspecialidad.adapter = adapter

                            binding.spinnerEspecialidad.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                                override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                                    if (position in especialidades.indices) {
                                        selectedIdEspecialidad = especialidades[position].idespecialidad
                                        actualizarSpinnerMedicos()
                                    }
                                }

                                override fun onNothingSelected(parentView: AdapterView<*>?) {}
                            }
                        } else {
                            Toast.makeText(this@CitaNuevaActivity, "No se pudo cargar la lista de especialidades", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<List<ResponseEspecialidad>>, t: Throwable) {
                    runOnUiThread {
                        ocultarCargando()
                        Toast.makeText(this@CitaNuevaActivity, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    private fun cargarDoctoresActivos() {
        val apiServiceCita = RetrofitClient.apiService

        mostrarCargando()

        GlobalScope.launch(Dispatchers.IO) {
            // estado = 1 -> doctor.php ya filtra y solo devuelve los activos
            val call = apiServiceCita.getDoctores(1)
            call.enqueue(object : Callback<List<ResponseDoctor>> {
                override fun onResponse(call: Call<List<ResponseDoctor>>, response: Response<List<ResponseDoctor>>) {
                    runOnUiThread {
                        ocultarCargando()
                        if (response.isSuccessful) {
                            doctores = response.body() ?: emptyList()
                            // Si la especialidad ya se había elegido, refresca el spinner de médicos con lo recién cargado
                            actualizarSpinnerMedicos()
                        } else {
                            Toast.makeText(this@CitaNuevaActivity, "No se pudo cargar la lista de médicos", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<List<ResponseDoctor>>, t: Throwable) {
                    runOnUiThread {
                        ocultarCargando()
                        Toast.makeText(this@CitaNuevaActivity, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }

    // Filtra los médicos activos ya cargados según la especialidad elegida y
    // vuelve a llenar el spinner de médicos con solo esos.
    private fun actualizarSpinnerMedicos() {
        doctoresFiltrados = doctores.filter { it.idespecialidad == selectedIdEspecialidad }
        selectedId = 0
        cargarDisponibilidad()

        val nombres = doctoresFiltrados.map { it.nombre }
        val adapter = ArrayAdapter(this@CitaNuevaActivity, android.R.layout.simple_spinner_item, nombres)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMedico.adapter = adapter

        binding.spinnerMedico.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                if (position in doctoresFiltrados.indices) {
                    selectedId = doctoresFiltrados[position].iddoctor
                    selectedName = doctoresFiltrados[position].nombre
                    cargarDisponibilidad()
                }
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {}
        }

        if (doctoresFiltrados.isEmpty() && selectedIdEspecialidad != 0) {
            Toast.makeText(this@CitaNuevaActivity, "No hay médicos activos para esa especialidad", Toast.LENGTH_SHORT).show()
        }
    }
}
