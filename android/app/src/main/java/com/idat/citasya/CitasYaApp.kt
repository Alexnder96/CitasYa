package com.idat.citasya

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate

/**
 * Fuerza el modo claro en toda la app, sin importar si el celular tiene
 * activado el modo oscuro del sistema. Se ejecuta antes que cualquier
 * Activity, así que cubre toda la app desde la primera pantalla.
 */
class CitasYaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
