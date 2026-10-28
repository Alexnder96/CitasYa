package com.idat.citasya.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.Intent
import android.util.Log
import android.util.Patterns
import android.widget.Toast
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.databinding.ActivityRegistroBinding
import com.idat.citasya.model.ResponsePaciente
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegistroActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistroBinding

    private var dni: String = ""
    private var email: String = ""
    private var password: String = ""
    private var confirmarPassword: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegistroBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ya no se piden nombre/edad/sexo/telefono/sede: se jalan del hospital por DNI.
        binding.btnregistrar.setOnClickListener {
            dni = binding.edtDni.text.toString().trim()
            email = binding.edtusuario.text.toString().trim()
            password = binding.edtpassword.text.toString()
            confirmarPassword = binding.edtconfirmarpassword.text.toString()

            if (dni.isEmpty() || email.isEmpty() || password.isEmpty() || confirmarPassword.isEmpty()) {
                Toast.makeText(baseContext, "Ingresa tu DNI, correo, contraseña y confírmala", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!dni.matches(Regex("^\\d{8}$"))) {
                Toast.makeText(baseContext, "El DNI debe tener 8 dígitos numéricos", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(baseContext, "Ingresa un correo válido", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(baseContext, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmarPassword) {
                Toast.makeText(baseContext, "Las contraseñas no coinciden", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val requestBody = HashMap<String, String>()
            requestBody["dni"] = dni
            requestBody["correo"] = email
            requestBody["password"] = password

            val apiService = RetrofitClient.apiService

            // Llamar a la API usando Dispatchers.IO
            GlobalScope.launch(Dispatchers.IO) {
                val call = apiService.PostRegistrarPaciente(requestBody)
                call.enqueue(object : Callback<ResponsePaciente> {
                    override fun onResponse(
                        call: Call<ResponsePaciente>,
                        response: Response<ResponsePaciente>
                    ) {
                        runOnUiThread {
                            if (response.isSuccessful) {
                                Toast.makeText(baseContext, "Usuario creado con éxito", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@RegistroActivity, LoginActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // Mostramos el mensaje real que manda paciente.php (DNI no encontrado,
                                // DNI ya registrado, correo ya registrado, etc.) en vez de adivinarlo por el código.
                                val mensaje = try {
                                    JSONObject(response.errorBody()?.string() ?: "").optString(
                                        "error", "Error al registrar el paciente"
                                    )
                                } catch (e: Exception) {
                                    "Error al registrar el paciente"
                                }
                                Toast.makeText(baseContext, mensaje, Toast.LENGTH_LONG).show()
                            }
                        }
                    }

                    override fun onFailure(call: Call<ResponsePaciente>, t: Throwable) {
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