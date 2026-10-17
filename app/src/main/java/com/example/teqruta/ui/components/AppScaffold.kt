package com.example.teqruta.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.text.style.TextOverflow
import com.example.teqruta.model.PoliticaAcceso
import com.example.teqruta.model.Rol
import com.example.teqruta.model.Usuario
import com.example.teqruta.navigation.Screen

// Elemento del menú inferior. "rutas" son todas las pantallas que lo dejan marcado como seleccionado.
private data class ItemMenu(
    val pantalla: Screen,
    val etiqueta: String,
    val icono: ImageVector,
    val rutas: Set<String>
)

// Estructura visual común a todas las pantallas (con sesión iniciada): TopAppBar arriba y
// NavigationBar abajo. El menú solo muestra las secciones que el rol del usuario puede usar.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    titulo: String,
    rutaActual: String?,
    onNavegar: (Screen) -> Unit,
    usuario: Usuario,
    onCerrarSesion: () -> Unit,
    mostrarVolver: Boolean = false,
    onVolver: () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    val items = buildList {
        add(ItemMenu(Screen.Home, "Inicio", Icons.Default.Home, setOf(Screen.Home.route)))
        if (PoliticaAcceso.puedeRegistrarTraslados(usuario)) {
            add(
                ItemMenu(
                    Screen.TrasladoNuevo, "Traslado", Icons.Default.LocationOn,
                    setOf(Screen.TrasladoNuevo.route, Screen.TrasladoFinalizar.route)
                )
            )
        }
        if (PoliticaAcceso.puedeVerTraslados(usuario)) {
            add(
                ItemMenu(
                    Screen.Traslados,
                    if (usuario.rol == Rol.TECNICO) "Mis traslados" else "Traslados",
                    Icons.AutoMirrored.Filled.List,
                    setOf(Screen.Traslados.route)
                )
            )
        }
        if (PoliticaAcceso.puedeVerReportes(usuario)) {
            add(ItemMenu(Screen.Reportes, "Costos", Icons.Default.Info, setOf(Screen.Reportes.route)))
        }
        if (PoliticaAcceso.puedeGestionarCatalogos(usuario)) {
            add(ItemMenu(Screen.Catalogos, "Catálogos", Icons.Default.Settings, setOf(Screen.Catalogos.route)))
        }
        if (PoliticaAcceso.puedeGestionarUsuarios(usuario)) {
            add(
                ItemMenu(
                    Screen.Usuarios, "Usuarios", Icons.Default.Person,
                    setOf(Screen.Usuarios.route, Screen.UsuarioForm.route)
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (mostrarVolver) {
                        IconButton(onClick = onVolver) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    // Cerrar la sesión (el rol del usuario se muestra en la pantalla de inicio)
                    IconButton(onClick = onCerrarSesion) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { item ->
                    NavigationBarItem(
                        selected = rutaActual in item.rutas,
                        onClick = { onNavegar(item.pantalla) },
                        icon = { Icon(item.icono, contentDescription = item.etiqueta) },
                        label = { Text(item.etiqueta, maxLines = 1) }
                    )
                }
            }
        },
        content = contenido
    )
}
