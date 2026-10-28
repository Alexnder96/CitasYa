package com.idat.citasya.view

import android.app.Activity.RESULT_OK
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.idat.citasya.Controller.ErrorUtils
import com.idat.citasya.Controller.RetrofitClient
import com.idat.citasya.databinding.FragmentPerfilBinding
import com.idat.citasya.model.ResponsePaciente
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PerfilFragment : Fragment() {

    private var binding: FragmentPerfilBinding? = null
    private var idPaciente: Int? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPerfilBinding.inflate(inflater, container, false)

        idPaciente = arguments?.getInt("idPaciente")

        binding?.btnCambiarContra?.setOnClickListener(){
            val intent = Intent(activity, CambioContraActivity::class.java)
            intent.putExtra("idPaciente", idPaciente)
            startActivity(intent)
        }

        binding?.LogoutButon?.setOnClickListener {
            val builder = AlertDialog.Builder(requireActivity())
            builder.setTitle("Cerrar sesión")
            builder.setMessage("¿Está seguro de que desea cerrar sesión?")
            builder.setPositiveButton("Cerrar sesión") { dialog, which ->
                val intent = Intent(activity, LoginActivity::class.java)
                startActivity(intent)
                activity?.finish()
                Toast.makeText(requireActivity().baseContext, "Se cerró la sesión", Toast.LENGTH_LONG).show()
            }
            builder.setNegativeButton("Cancelar", null)
            builder.show()
        }




        return binding?.root
    }

    override fun onResume() {
        super.onResume()
        // Se recarga cada vez que el fragment vuelve a quedar visible: con ViewPager2
        // el fragment no se recrea al cambiar de pestaña y volver, solo se llama onResume.
        cargarPerfil()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    private fun cargarPerfil() {
        val idPac = idPaciente ?: return
        val apiService = RetrofitClient.apiService

        // Llamar a la API usando Dispatchers.IO
        GlobalScope.launch(Dispatchers.IO) {
            val call = apiService.getPaciente(idPac)
            call.enqueue(object : Callback<ResponsePaciente> {
                override fun onResponse(
                    call: Call<ResponsePaciente>,
                    response: Response<ResponsePaciente>
                ) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        val paciente = response.body()
                        paciente?.let { mostrarDatosPerfil(it) }
                    } else {
                        Toast.makeText(context, "Error al obtener paciente", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ResponsePaciente>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(context, ErrorUtils.mensajeDeConexion(t), Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun mostrarDatosPerfil(paciente: ResponsePaciente?) {
        paciente?.let {
            binding?.txvnombre?.text = it.nombre
            binding?.txvedad?.text = it.edad.toString()
            binding?.txvgenero?.text = it.sexo.toString()
            binding?.txvcorreo?.text = it.correo.toString()
            binding?.txvtelefono?.text = it.telefono.toString()
            binding?.txvsede?.text = it.nombre_sede
        } ?: run {
            Toast.makeText(context, "Error al llenar TV", Toast.LENGTH_SHORT).show()
        }
    }



}