package com.example.petcare.navigation

// Eventos de navegación que emite el ViewModel y que la UI ejecuta con el NavController
sealed class NavigationEvent {

    // Navegar a un destino específico, popUpToRoute: si no es null, se limpia la pila hasta esa ruta,
    // inclusive: si es true, también se elimina la ruta indicada en popUpToRoute y singleTop: evita crear copias duplicadas del mismo destino en la parte superior.
    data class NavigateTo(
        val route: Screen,
        val popUpToRoute: Screen? = null,
        val inclusive: Boolean = false,
        val singleTop: Boolean = false
    ) : NavigationEvent()

    // Volver a la pantalla anterior de la pila
    data object PopBackStack : NavigationEvent()

    // Navegar "hacia arriba" en la jerarquía de la app
    data object NavigateUp : NavigationEvent()
}
