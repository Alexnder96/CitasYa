package com.idat.citasya.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.databinding.ActivityRecuperarPasswordBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RecuperarPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecuperarPasswordBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecuperarPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRestablecerPassword.setOnClickListener {
            val dni = binding.edtDniRecuperar.text.toString().trim()
            val correo = binding.edtCorreoRecuperar.text.toString().trim()
            val nuevaPassword = binding.edtNuevaPassword.text.toString()
            val confirmarPassword = binding.edtConfirmarPassword.text.toString()

            if (dni.isEmpty() || correo.isEmpty() || nuevaPassword.isEmpty()) {
                Toast.makeText(baseContext, "Completa todos los campos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!dni.matches(Regex("^\\d{8}$"))) {
                Toast.makeText(baseContext, "El DNI debe tener 8 dígitos numéricos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
                Toast.makeText(baseContext, "Ingresa un correo válido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (nuevaPassword.length < 6) {
                Toast.makeText(baseContext, "La nueva contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (nuevaPassword != confirmarPassword) {
                Toast.makeText(baseContext, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val requestBody = mapOf(
                "accion" to "recuperar_password",
                "dni" to dni,
                "correo" to correo,
                "nuevaPassword" to nuevaPassword
            )

            val apiService = RetrofitClient.apiService

            GlobalScope.launch(Dispatchers.IO) {
                val call = apiService.recuperarPassword(requestBody)
                call.enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                Toast.makeText(baseContext, "Contraseña actualizada, ya puedes ingresar", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@RecuperarPasswordActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                val mensaje = when (response.code()) {
                                    404 -> "No encontramos una cuenta con ese DNI y correo"
                                    400 -> "Faltan datos por completar"
                                    else -> "No se pudo actualizar la contraseña"
                                }
                                Toast.makeText(baseContext, mensaje, Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        runOnUiThread {
                            Toast.makeText(baseContext, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_LONG).show()
                        }
                        Log.i("Error", "Error al realizar la solicitud $t")
                    }
                })
            }
        }
    }
}
