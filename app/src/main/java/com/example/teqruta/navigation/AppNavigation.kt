package com.example.teqruta.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.teqruta.model.EstadoTraslado
import com.example.teqruta.model.PoliticaAcceso
import com.example.teqruta.model.Rol
import com.example.teqruta.model.Usuario
import com.example.teqruta.ui.components.AppScaffold
import com.example.teqruta.ui.components.MensajeCentrado
import com.example.teqruta.ui.screens.AccionHome
import com.example.teqruta.ui.screens.CatalogosScreen
import com.example.teqruta.ui.screens.DatoHome
import com.example.teqruta.ui.screens.HomeScreen
import com.example.teqruta.ui.screens.LoginScreen
import com.example.teqruta.ui.screens.ReportesScreen
import com.example.teqruta.ui.screens.TrasladoFinalizarScreen
import com.example.teqruta.ui.screens.TrasladoNuevoScreen
import com.example.teqruta.ui.screens.TrasladosScreen
import com.example.teqruta.ui.screens.UsuarioFormScreen
import com.example.teqruta.ui.screens.UsuariosScreen
import com.example.teqruta.ui.utils.Formato
import com.example.teqruta.ui.utils.TipoPantalla
import com.example.teqruta.viewmodel.CatalogoViewModel
import com.example.teqruta.viewmodel.MainViewModel
import com.example.teqruta.viewmodel.TrasladoViewModel
import com.example.teqruta.viewmodel.UsuarioViewModel

// Grafo de navegación de la app: define qué pantalla se muestra en cada ruta.
// Todas las pantallas, salvo el login, exigen una sesión iniciada, y el contenido de cada una
// depende de los permisos del rol.
@Composable
fun AppNavigation(
    navController: NavHostController,
    mainViewModel: MainViewModel,
    usuarioViewModel: UsuarioViewModel,
    trasladoViewModel: TrasladoViewModel,
    catalogoViewModel: CatalogoViewModel,
    tipoPantalla: TipoPantalla
) {
    // Ruta actual, para marcar la pestaña seleccionada en el menú inferior
    val rutaActual = navController.currentBackStackEntryAsState().value?.destination?.route

    val usuarioActual by usuarioViewModel.usuarioActual.collectAsState()
    val datos by trasladoViewModel.datos.collectAsState()

    // Si no hay sesión (se cerró, o la app se restauró sin ella) se vuelve al login
    val haySesion = usuarioActual != null
    LaunchedEffect(haySesion) {
        val ruta = navController.currentDestination?.route
        if (!haySesion && ruta != null && ruta != Screen.Login.route) {
            mainViewModel.navigateTo(Screen.Login, popUpToRoute = Screen.Home, inclusive = true)
        }
    }

    val cerrarSesion = {
        trasladoViewModel.limpiarFormularios()
        catalogoViewModel.cerrarTodo()
        usuarioViewModel.cerrarSesion()
    }

    // El botón "Traslado" lleva a iniciar uno nuevo o, si ya hay uno en curso, a finalizarlo.
    // Los formularios solo se reinician al llegar desde otra pantalla (no al volver a tocar la pestaña).
    val irATraslado = {
        val usuario = usuarioActual
        if (usuario != null && PoliticaAcceso.puedeRegistrarTraslados(usuario)) {
            if (trasladoViewModel.trasladoEnCurso(usuario) != null) {
                if (rutaActual != Screen.TrasladoFinalizar.route) trasladoViewModel.prepararFinalizar()
                mainViewModel.navigateTo(Screen.TrasladoFinalizar, popUpToRoute = Screen.Home, singleTop = true)
            } else {
                if (rutaActual != Screen.TrasladoNuevo.route) trasladoViewModel.prepararNuevo(usuario)
                mainViewModel.navigateTo(Screen.TrasladoNuevo, popUpToRoute = Screen.Home, singleTop = true)
            }
        }
    }

    // Navegación desde el menú inferior: evita apilar pantallas repetidas
    val navegarDesdeMenu: (Screen) -> Unit = { destino ->
        if (destino == Screen.TrasladoNuevo) {
            irATraslado()
        } else {
            mainViewModel.navigateTo(destino, popUpToRoute = Screen.Home, singleTop = true)
        }
    }

    // Envuelve cada pantalla con sesión en el AppScaffold común
    @Composable
    fun Pantalla(
        titulo: String,
        usuario: Usuario,
        mostrarVolver: Boolean = true,
        contenido: @Composable (androidx.compose.foundation.layout.PaddingValues) -> Unit
    ) {
        AppScaffold(
            titulo = titulo,
            rutaActual = rutaActual,
            onNavegar = navegarDesdeMenu,
            usuario = usuario,
            onCerrarSesion = cerrarSesion,
            mostrarVolver = mostrarVolver,
            onVolver = { mainViewModel.navigateBack() },
            contenido = contenido
        )
    }

    NavHost(navController = navController, startDestination = Screen.Login.route) {

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = usuarioViewModel,
                onLoginExitoso = {
                    mainViewModel.navigateTo(Screen.Home, popUpToRoute = Screen.Login, inclusive = true)
                }
            )
        }

        composable(Screen.Home.route) {
            val usuario = usuarioActual ?: return@composable
            val visibles = PoliticaAcceso.filtrarVisibles(usuario, datos.traslados, datos.usuarios)
            val datosHome = mutableListOf<DatoHome>()
            val avisos = mutableListOf<String>()
            val acciones = mutableListOf<AccionHome>()

            when (usuario.rol) {
                Rol.TECNICO -> {
                    val enCurso = trasladoViewModel.trasladoEnCurso(usuario)
                    if (enCurso != null) {
                        val origen = datos.ubicacionesPorId[enCurso.origenId]?.nombre ?: "Origen"
                        val destino = datos.ubicacionesPorId[enCurso.destinoId]?.nombre ?: "Destino"
                        datosHome += DatoHome("En curso", "$origen → $destino (desde las ${Formato.hora(enCurso.inicioMs)})")
                        acciones += AccionHome("Finalizar traslado", true, irATraslado)
                    } else {
                        datosHome += DatoHome(
                            visibles.count { it.estado == EstadoTraslado.PENDIENTE_VALIDACION }.toString(),
                            "traslados esperando validación"
                        )
                        acciones += AccionHome("Iniciar traslado", true, irATraslado)
                    }
                    val rechazados = visibles.count { it.estado == EstadoTraslado.RECHAZADO }
                    if (rechazados > 0) avisos += "Tienes $rechazados traslado(s) rechazado(s). Revisa el motivo en Mis traslados."
                    acciones += AccionHome("Mis traslados", false) { mainViewModel.navigateTo(Screen.Traslados) }
                }
                Rol.JEFE_SERVICIO -> {
                    val porValidar = visibles.filter { it.estado == EstadoTraslado.PENDIENTE_VALIDACION }
                    datosHome += DatoHome(porValidar.size.toString(), "traslados de tu equipo por validar")
                    val conDesviacion = porValidar.count { it.hayDesviacion }
                    if (conDesviacion > 0) avisos += "$conDesviacion con desviación entre lo declarado y la distancia oficial."
                    acciones += AccionHome("Revisar traslados", true) { mainViewModel.navigateTo(Screen.Traslados) }
                }
                Rol.GERENCIA_FINANZAS -> {
                    val validados = visibles.filter { it.estado == EstadoTraslado.VALIDADO }
                    datosHome += DatoHome(
                        Formato.clp(validados.sumOf { it.calculo?.costoTotalClp ?: 0 }),
                        "costo de movilización validado"
                    )
                    datosHome += DatoHome(validados.size.toString(), "traslados validados")
                    acciones += AccionHome("Ver costos", true) { mainViewModel.navigateTo(Screen.Reportes) }
                    acciones += AccionHome("Ver traslados", false) { mainViewModel.navigateTo(Screen.Traslados) }
                }
                Rol.ADMINISTRADOR -> {
                    datosHome += DatoHome(
                        datos.vehiculos.count { it.activo }.toString(), "vehículos activos"
                    )
                    datosHome += DatoHome(
                        datos.usuarios.count { it.activo }.toString(), "usuarios activos"
                    )
                    acciones += AccionHome("Catálogos", true) { mainViewModel.navigateTo(Screen.Catalogos) }
                    acciones += AccionHome("Usuarios", false) { mainViewModel.navigateTo(Screen.Usuarios) }
                }
            }

            Pantalla(titulo = "TeqRuta", usuario = usuario, mostrarVolver = false) { padding ->
                HomeScreen(
                    tipoPantalla = tipoPantalla,
                    saludo = "${usuario.nombre} · ${usuario.rol.etiqueta}",
                    datos = datosHome,
                    avisos = avisos,
                    acciones = acciones,
                    modifier = Modifier.padding(padding)
                )
            }
        }

        composable(Screen.TrasladoNuevo.route) {
            val usuario = usuarioActual ?: return@composable
            Pantalla(titulo = "Iniciar traslado", usuario = usuario) { padding ->
                if (PoliticaAcceso.puedeRegistrarTraslados(usuario)) {
                    TrasladoNuevoScreen(
                        viewModel = trasladoViewModel,
                        usuario = usuario,
                        datos = datos,
                        onIniciado = {
                            mainViewModel.navigateTo(Screen.Home, popUpToRoute = Screen.Home, singleTop = true)
                        },
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    MensajeCentrado("Tu rol no permite registrar traslados.", Modifier.padding(padding))
                }
            }
        }

        composable(Screen.TrasladoFinalizar.route) {
            val usuario = usuarioActual ?: return@composable
            Pantalla(titulo = "Finalizar traslado", usuario = usuario) { padding ->
                if (PoliticaAcceso.puedeRegistrarTraslados(usuario)) {
                    TrasladoFinalizarScreen(
                        viewModel = trasladoViewModel,
                        usuario = usuario,
                        datos = datos,
                        onFinalizado = {
                            mainViewModel.navigateTo(Screen.Traslados, popUpToRoute = Screen.Home, singleTop = true)
                        },
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    MensajeCentrado("Tu rol no permite registrar traslados.", Modifier.padding(padding))
                }
            }
        }

        composable(Screen.Traslados.route) {
            val usuario = usuarioActual ?: return@composable
            Pantalla(
                titulo = if (usuario.rol == Rol.TECNICO) "Mis traslados" else "Traslados",
                usuario = usuario
            ) { padding ->
                if (PoliticaAcceso.puedeVerTraslados(usuario)) {
                    TrasladosScreen(
                        viewModel = trasladoViewModel,
                        usuario = usuario,
                        datos = datos,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    MensajeCentrado("Tu rol no permite ver traslados.", Modifier.padding(padding))
                }
            }
        }

        composable(Screen.Reportes.route) {
            val usuario = usuarioActual ?: return@composable
            Pantalla(titulo = "Costo de movilización", usuario = usuario) { padding ->
                if (PoliticaAcceso.puedeVerReportes(usuario)) {
                    ReportesScreen(datos = datos, modifier = Modifier.padding(padding))
                } else {
                    MensajeCentrado("Tu rol no permite ver los costos.", Modifier.padding(padding))
                }
            }
        }

        composable(Screen.Catalogos.route) {
            val usuario = usuarioActual ?: return@composable
            Pantalla(titulo = "Catálogos", usuario = usuario) { padding ->
                if (PoliticaAcceso.puedeGestionarCatalogos(usuario)) {
                    CatalogosScreen(
                        viewModel = catalogoViewModel,
                        actor = usuario,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    MensajeCentrado("No tienes permiso para gestionar los catálogos.", Modifier.padding(padding))
                }
            }
        }

        composable(Screen.Usuarios.route) {
            val usuario = usuarioActual ?: return@composable
            Pantalla(titulo = "Usuarios", usuario = usuario) { padding ->
                if (PoliticaAcceso.puedeGestionarUsuarios(usuario)) {
                    UsuariosScreen(
                        viewModel = usuarioViewModel,
                        actor = usuario,
                        onNuevo = {
                            if (usuarioViewModel.nuevoUsuario(usuario)) {
                                mainViewModel.navigateTo(Screen.UsuarioForm)
                            }
                        },
                        onEditar = { id ->
                            if (usuarioViewModel.editarUsuario(usuario, id)) {
                                mainViewModel.navigateTo(Screen.UsuarioForm)
                            }
                        },
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    MensajeCentrado("No tienes permiso para gestionar usuarios.", Modifier.padding(padding))
                }
            }
        }

        composable(Screen.UsuarioForm.route) {
            val usuario = usuarioActual ?: return@composable
            Pantalla(titulo = "Usuario", usuario = usuario) { padding ->
                if (PoliticaAcceso.puedeGestionarUsuarios(usuario)) {
                    UsuarioFormScreen(
                        viewModel = usuarioViewModel,
                        actor = usuario,
                        onGuardado = {
                            mainViewModel.navigateTo(Screen.Usuarios, popUpToRoute = Screen.Home, singleTop = true)
                        },
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    MensajeCentrado("No tienes permiso para gestionar usuarios.", Modifier.padding(padding))
                }
            }
        }
    }
}
