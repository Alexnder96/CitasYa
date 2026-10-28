package com.idat.citasya.Controller

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    // Único lugar con la URL del backend. Debe terminar en "/".
    //  - Emulador de Android Studio: http://10.0.2.2/Api_citas/
    //  - Teléfono físico: la URL de tu túnel o la IP de tu PC, por ejemplo https://xxxx.trycloudflare.com/Api_citas/
    private const val BASE_URL = "http://10.0.2.2/Api_citas/"
    val apiService: ApiServiceCita by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiServiceCita::class.java)
    }
}
