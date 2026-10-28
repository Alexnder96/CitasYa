package com.idat.citasya.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.databinding.ActivityCambioContraBinding
import com.idat.citasya.model.ResponsePaciente
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CambioContraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCambioContraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCambioContraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val idPaciente = intent.getIntExtra("idPaciente", 0)

        binding.btnActualizarContra.setOnClickListener {
            val newPassword = binding.txtContraNueva.text.toString()
            val confirmPassword = binding.txtValiContraseA.text.toString()

            if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this@CambioContraActivity, "Ingresa la nueva contraseña y confírmala", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(this@CambioContraActivity, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this@CambioContraActivity, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            actualizarPasswordPaciente(idPaciente, newPassword)
        }
    }

    private fun actualizarPasswordPaciente(idPaciente: Int, newPassword: String) {
        val apiService = RetrofitClient.apiService

        val requestData = mapOf(
            "idpaciente" to idPaciente.toString(),
            "password" to newPassword
        )

        val call: Call<Void> = apiService.actualizarPassword(requestData)

        call.enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // Manejar respuesta exitosa aquí
                    Toast.makeText(this@CambioContraActivity, "Contraseña actualizada exitosamente", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    // Manejar respuesta de error aquí
                    Toast.makeText(this@CambioContraActivity, "Error al actualizar la contraseña", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Void>, t: Throwable) {
                // Manejar fallo de la solicitud aquí
                Toast.makeText(this@CambioContraActivity, "Error en la solicitud: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}