package com.example.teqruta

import android.app.Application
import com.example.teqruta.data.AppContainer

// Se ejecuta antes que cualquier pantalla: abre la base de datos y deja listo el repositorio.
class TeqRutaApp : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.iniciar(this)
    }
}
