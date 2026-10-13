package com.example.petcare.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.petcare.navigation.Screen

// Elemento del menú inferior
private data class ItemMenu(val pantalla: Screen, val etiqueta: String, val icono: ImageVector)

// Estructura visual común a todas las pantallas: TopAppBar arriba y NavigationBar abajo.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    titulo: String,
    rutaActual: String?,
    onNavegar: (Screen) -> Unit,
    mostrarVolver: Boolean = false,
    onVolver: () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    val items = listOf(
        ItemMenu(Screen.Home, "Inicio", Icons.Default.Home),
        ItemMenu(Screen.Registro, "Registrar", Icons.Default.Add),
        ItemMenu(Screen.Lista, "Mascotas", Icons.AutoMirrored.Filled.List)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    if (mostrarVolver) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = rutaActual == item.pantalla.route,
                        onClick = { onNavegar(item.pantalla) },
                        icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                        label = { Text(item.etiqueta) }
                    )
                }
            }
        },
        content = contenido
    )
}
