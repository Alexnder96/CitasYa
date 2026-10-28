package com.idat.citasya.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.databinding.ActivityLoginBinding
import com.idat.citasya.model.ResponsePaciente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private var email: String = ""
    private var password: String = ""
    private var idPaciente: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.loginButton.setOnClickListener {
            intentarLogin()
        }

        // Usuario -> "Siguiente" en el teclado pasa el foco directo a Contraseña
        binding.usernameEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                binding.passwordEditText.requestFocus()
                true
            } else {
                false
            }
        }

        // Contraseña -> "Listo"/Enter en el teclado ingresa directo, sin tener que tocar el botón
        binding.passwordEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                binding.loginButton.performClick()
                true
            } else {
                false
            }
        }

        binding.registerButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegistroActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.forgotPasswordButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, RecuperarPasswordActivity::class.java)
            startActivity(intent)
        }
    }

    private fun intentarLogin() {
        email = binding.usernameEditText.text.toString()
        password = binding.passwordEditText.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(baseContext, "Ingresar correo y contraseña", Toast.LENGTH_LONG).show()
            return
        }

        val apiService = RetrofitClient.apiService

        val requestData = mapOf(
            "accion" to "login",
            "correo" to email,
            "password" to password
        )

        // Llamar a la API usando Dispatchers.IO
        GlobalScope.launch(Dispatchers.IO) {
            val call = apiService.login(requestData)
            call.enqueue(object : Callback<ResponsePaciente> {
                override fun onResponse(
                    call: Call<ResponsePaciente>,
                    response: Response<ResponsePaciente>
                ) {
                    if (response.isSuccessful) {
                        val paciente = response.body()
                        if (paciente != null) {
                            idPaciente = paciente.idpaciente
                            val intent = Intent(this@LoginActivity, IniActivity::class.java)
                            intent.putExtra("idPaciente", idPaciente)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(baseContext, "Correo o contraseña incorrecta", Toast.LENGTH_LONG).show()
                        }
                    } else if (response.code() == 403) {
                        Toast.makeText(baseContext, "Tu cuenta está inactiva", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(baseContext, "Correo o contraseña incorrecta", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ResponsePaciente>, t: Throwable) {
                    Toast.makeText(baseContext, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_LONG).show()
                }
            })
        }
    }

}