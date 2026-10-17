package com.example.teqruta

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.teqruta.navigation.AppNavigation
import com.example.teqruta.navigation.NavigationEvent
import com.example.teqruta.ui.theme.TeqRutaTheme
import com.example.teqruta.ui.utils.toTipoPantalla
import com.example.teqruta.viewmodel.CatalogoViewModel
import com.example.teqruta.viewmodel.MainViewModel
import com.example.teqruta.viewmodel.TrasladoViewModel
import com.example.teqruta.viewmodel.UsuarioViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TeqRutaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ViewModels y NavController
                    val mainViewModel: MainViewModel = viewModel()
                    val usuarioViewModel: UsuarioViewModel = viewModel()
                    val trasladoViewModel: TrasladoViewModel = viewModel()
                    val catalogoViewModel: CatalogoViewModel = viewModel()
                    val navController = rememberNavController()

                    // Tamaño de pantalla actual (para el diseño adaptable)
                    val tipoPantalla = calculateWindowSizeClass(this@MainActivity)
                        .widthSizeClass.toTipoPantalla()

                    // Escucha los eventos de navegación emitidos por el ViewModel
                    LaunchedEffect(Unit) {
                        mainViewModel.navigationEvents.collectLatest { event ->
                            when (event) {
                                is NavigationEvent.NavigateTo -> {
                                    navController.navigate(event.route.route) {
                                        event.popUpToRoute?.let { destino ->
                                            popUpTo(destino.route) { inclusive = event.inclusive }
                                        }
                                        launchSingleTop = event.singleTop
                                    }
                                }
                                NavigationEvent.PopBackStack -> navController.popBackStack()
                                NavigationEvent.NavigateUp -> navController.navigateUp()
                            }
                        }
                    }

                    AppNavigation(
                        navController = navController,
                        mainViewModel = mainViewModel,
                        usuarioViewModel = usuarioViewModel,
                        trasladoViewModel = trasladoViewModel,
                        catalogoViewModel = catalogoViewModel,
                        tipoPantalla = tipoPantalla
                    )
                }
            }
        }
    }
}
