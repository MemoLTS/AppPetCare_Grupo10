package com.example.petcare

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
import com.example.petcare.navigation.AppNavigation
import com.example.petcare.navigation.NavigationEvent
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.utils.toTipoPantalla
import com.example.petcare.viewmodel.MainViewModel
import com.example.petcare.viewmodel.MascotaViewModel
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PetCareTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // ViewModels y NavController
                    val mainViewModel: MainViewModel = viewModel()
                    val mascotaViewModel: MascotaViewModel = viewModel()
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
                        mascotaViewModel = mascotaViewModel,
                        tipoPantalla = tipoPantalla
                    )
                }
            }
        }
    }
}
