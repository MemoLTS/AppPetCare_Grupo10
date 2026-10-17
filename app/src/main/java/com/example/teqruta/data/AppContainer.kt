package com.example.teqruta.data

// Contenedor sencillo de dependencias: una única instancia del repositorio para toda la app.
// Los ViewModels lo reciben como parámetro con este valor por defecto, así pueden crearse
// sin argumentos en la app y con un repositorio de prueba en los tests.
object AppContainer {
    val repositorio: RepositorioDatos by lazy { RepositorioDatos() }
}
