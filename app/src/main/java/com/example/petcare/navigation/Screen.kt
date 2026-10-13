package com.example.petcare.navigation

// Rutas tipadas de la aplicación (sealed class para mayor seguridad)
sealed class Screen(val route: String) {
    // Pantalla de inicio
    data object Home : Screen("home_page")
    // Pantalla con el formulario de registro de mascota
    data object Registro : Screen("registro_page")
    // Pantalla con el listado de mascotas registradas
    data object Lista : Screen("lista_page")
}
