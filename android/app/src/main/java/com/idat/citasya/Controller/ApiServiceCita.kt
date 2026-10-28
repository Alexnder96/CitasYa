package com.idat.citasya.Controller

import retrofit2.Call

import com.idat.citasya.model.ResponseGuardarCita
import com.idat.citasya.model.ResponseHisCitas
import com.idat.citasya.model.ResponsePaciente
import com.idat.citasya.model.ResponseDoctor
import com.idat.citasya.model.ResponseSede
import com.idat.citasya.model.ResponseEspecialidad
import com.idat.citasya.model.ResponseSlotOcupado
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface ApiServiceCita {

    @GET("cita.php")
    fun getCitas(@Query("idpaciente") idPaciente: Int): Call<List<ResponseHisCitas>>

    @POST("cita.php")
    fun guardarcita(@Body cita: ResponseGuardarCita): Call<Void>

    // Horarios ya ocupados de un doctor en un rango de fechas (para deshabilitarlos al reservar)
    @GET("cita.php")
    fun getHorariosOcupados(
        @Query("iddoctor") idDoctor: Int,
        @Query("desde") desde: String,
        @Query("hasta") hasta: String
    ): Call<List<ResponseSlotOcupado>>

    // Cancela una cita propia (baja lógica: cita.php la pasa a estado 'cancelada')
    @DELETE("cita.php")
    fun cancelarCita(@Query("idcita") idCita: Int): Call<Void>

    // estado = 1 trae solo los doctores activos (así responde doctor.php)
    @GET("doctor.php")
    fun getDoctores(@Query("estado") estado: Int): Call<List<ResponseDoctor>>

    @GET("especialidad.php")
    fun getEspecialidades(): Call<List<ResponseEspecialidad>>

    // estado = 1 trae solo las sedes activas (así responde sede.php)
    @GET("sede.php")
    fun getSedes(@Query("estado") estado: Int): Call<List<ResponseSede>>

    @GET("paciente.php")
    fun getPaciente(@Query("idpaciente") idPaciente: Int): Call<ResponsePaciente>

    @GET("paciente.php")
    fun getPacientecorreo(@Query("correo") correo: String): Call<ResponsePaciente>

    // Login validado en el servidor (paciente.php responde según "accion": "login")
    @POST("paciente.php")
    fun login(@Body requestBody: Map<String, String>): Call<ResponsePaciente>

    @POST("paciente.php")
    fun PostRegistrarPaciente(@Body requestBody: Map<String, String>): Call<ResponsePaciente>

    // Recuperación de contraseña: valida dni + correo y setea nuevaPassword (paciente.php responde según "accion")
    @POST("paciente.php")
    fun recuperarPassword(@Body requestBody: Map<String, String>): Call<Void>

    @PUT("paciente.php")
    fun actualizarPassword(@Body requestData: Map<String, String>): Call<Void>
}