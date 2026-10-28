package com.idat.citasya.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.idat.citasya.Controller.IniPagerAdapter
import com.idat.citasya.R
import com.idat.citasya.databinding.ActivityIniBinding

class IniActivity : AppCompatActivity() {
    private lateinit var binding: ActivityIniBinding
    private var correo: String? = null
    private var idPaciente: Int = 0

    // Mismo orden que las páginas del ViewPager (IniPagerAdapter) y que navbar_menu.xml
    private val idsPorPosicion = listOf(R.id.Citas, R.id.Historial, R.id.Notificaciones, R.id.Perfil)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityIniBinding.inflate(layoutInflater)
        setContentView(binding.root)

        correo = intent.getStringExtra("correo")
        idPaciente = intent.getIntExtra("idPaciente", 0)

        binding.viewPager.adapter = IniPagerAdapter(this, correo, idPaciente)
        // Solo son 4 pantallas livianas, no hace falta ahorrar manteniéndolas descargadas.
        binding.viewPager.offscreenPageLimit = idsPorPosicion.size - 1

        // Tocar un ícono del bottom nav mueve el ViewPager (con animación de deslizado).
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val posicion = idsPorPosicion.indexOf(item.itemId)
            if (posicion != -1) {
                binding.viewPager.setCurrentItem(posicion, true)
            }
            true
        }

        // Deslizar el ViewPager con el dedo actualiza qué ícono queda marcado abajo
        // (isChecked directo sobre el item, no selectedItemId, para no disparar de nuevo
        // el listener de arriba y evitar un loop).
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                binding.bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })
    }
}
