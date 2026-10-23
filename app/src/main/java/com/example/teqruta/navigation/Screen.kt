package com.example.teqruta.navigation

// Rutas tipadas de la aplicación: evita escribir strings sueltos al navegar.
sealed class Screen(val route: String) {
    // Inicio de sesión
    data object Login : Screen("login_page")
    // Inicio con el resumen y las acciones propias del rol
    data object Home : Screen("home_page")
    // Técnico: registrar el inicio de un traslado
    data object TrasladoNuevo : Screen("traslado_nuevo_page")
    // Técnico: registrar el fin del traslado en curso
    data object TrasladoFinalizar : Screen("traslado_finalizar_page")
    // Lista de traslados (propios, del equipo o todos según el rol); el jefe valida desde aquí
    data object Traslados : Screen("traslados_page")
    // Gerencia / Finanzas: costo por cliente, técnico y vehículo
    data object Reportes : Screen("reportes_page")
    // Administrador: vehículos, ubicaciones y tramos
    data object Catalogos : Screen("catalogos_page")
    // Administrador: usuarios
    data object Usuarios : Screen("usuarios_page")
    data object UsuarioForm : Screen("usuario_form_page")
}
