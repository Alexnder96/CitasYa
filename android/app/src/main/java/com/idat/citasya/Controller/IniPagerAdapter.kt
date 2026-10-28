package com.idat.citasya.Controller

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.idat.citasya.view.CitasFragment
import com.idat.citasya.view.HistorialFragment
import com.idat.citasya.view.NotificacionesFragment
import com.idat.citasya.view.PerfilFragment

/**
 * Adapter del ViewPager2 que reemplaza al FragmentTransaction.replace() manual:
 * mantiene las 4 pantallas (Citas, Historial, Notificaciones, Perfil) como páginas
 * deslizables, en el mismo orden que navbar_menu.xml.
 */
class IniPagerAdapter(
    activity: FragmentActivity,
    private val correo: String?,
    private val idPaciente: Int
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment {
        val fragment = when (position) {
            0 -> CitasFragment()
            1 -> HistorialFragment()
            2 -> NotificacionesFragment()
            3 -> PerfilFragment()
            else -> throw IllegalStateException("Posición de página inválida: $position")
        }
        fragment.arguments = Bundle().apply {
            putString("correo", correo)
            putInt("idPaciente", idPaciente)
        }
        return fragment
    }
}
