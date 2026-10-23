package com.example.teqruta.data

import android.content.Context

// Contenedor sencillo de dependencias: una única base de datos y un único repositorio para toda
// la app. TeqRutaApp llama a iniciar() al arrancar. Los ViewModels reciben el repositorio como
// parámetro con este valor por defecto, así pueden probarse con otro repositorio.
object AppContainer {
    private lateinit var baseDatos: BaseDatos

    val repositorio: RepositorioDatos by lazy { RepositorioDatos(baseDatos) }

    fun iniciar(context: Context) {
        if (!::baseDatos.isInitialized) baseDatos = BaseDatos(context)
    }
}
