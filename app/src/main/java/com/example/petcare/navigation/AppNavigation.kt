package com.example.petcare.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.petcare.ui.components.AppScaffold
import com.example.petcare.ui.screens.HomeScreen
import com.example.petcare.ui.screens.ListaScreen
import com.example.petcare.ui.screens.RegistroScreen
import com.example.petcare.ui.utils.TipoPantalla
import com.example.petcare.viewmodel.MainViewModel
import com.example.petcare.viewmodel.MascotaViewModel

// Grafo de navegación de la app: define qué pantalla se muestra en cada ruta.
@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    mascotaViewModel: MascotaViewModel,
    tipoPantalla: TipoPantalla
) {
    // Ruta actual, para marcar la pestaña seleccionada en el menú inferior
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    // Navegación desde el menú inferior: evita apilar pantallas repetidas
    val navegarDesdeMenu: (Screen) -> Unit = { destino ->
        mainViewModel.navigateTo(destino, popUpToRoute = Screen.Home, singleTop = true)
    }

    NavHost(navController = navController, startDestination = Screen.Home.route) {

        composable(Screen.Home.route) {
            val mascotas by mascotaViewModel.mascotas.collectAsState()
            AppScaffold(
                titulo = "PetCare",
                rutaActual = rutaActual,
                onNavegar = navegarDesdeMenu
            ) { padding ->
                HomeScreen(
                    tipoPantalla = tipoPantalla,
                    totalMascotas = mascotas.size,
                    onRegistrar = { mainViewModel.navigateTo(Screen.Registro) },
                    onVerLista = { mainViewModel.navigateTo(Screen.Lista) },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(Screen.Registro.route) {
            AppScaffold(
                titulo = "Registrar mascota",
                rutaActual = rutaActual,
                onNavegar = navegarDesdeMenu,
                mostrarVolver = true,
                onVolver = { mainViewModel.navigateBack() }
            ) { padding ->
                RegistroScreen(
                    viewModel = mascotaViewModel,
                    onRegistroExitoso = {
                        mainViewModel.navigateTo(
                            Screen.Lista,
                            popUpToRoute = Screen.Home,
                            singleTop = true
                        )
                    },
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(Screen.Lista.route) {
            AppScaffold(
                titulo = "Mascotas registradas",
                rutaActual = rutaActual,
                onNavegar = navegarDesdeMenu,
                mostrarVolver = true,
                onVolver = { mainViewModel.navigateBack() }
            ) { padding ->
                ListaScreen(
                    viewModel = mascotaViewModel,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}
