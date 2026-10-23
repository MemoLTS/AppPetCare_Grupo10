package com.example.teqruta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.TipoUbicacion
import com.example.teqruta.model.TipoVehiculo
import com.example.teqruta.model.Usuario
import com.example.teqruta.ui.components.CampoFormulario
import com.example.teqruta.ui.components.DialogoFormulario
import com.example.teqruta.ui.components.SelectorDesplegable
import com.example.teqruta.ui.utils.Formato
import com.example.teqruta.viewmodel.CatalogoViewModel

private val PESTANAS = listOf("Vehículos", "Ubicaciones", "Tramos")

// Apartado del administrador: mantiene vehículos (con su patente y costo por km), ubicaciones
// (sede y clientes) y tramos con su distancia y peaje. Los registros no se borran: se desactivan.
@Composable
fun CatalogosScreen(
    viewModel: CatalogoViewModel,
    actor: Usuario,
    modifier: Modifier = Modifier
) {
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = pestana) {
            PESTANAS.forEachIndexed { indice, titulo ->
                Tab(
                    selected = pestana == indice,
                    onClick = { pestana = indice },
                    text = { Text(titulo, maxLines = 1) }
                )
            }
        }
        when (pestana) {
            0 -> PestanaVehiculos(viewModel, actor)
            1 -> PestanaUbicaciones(viewModel, actor)
            else -> PestanaTramos(viewModel, actor)
        }
    }
}

// Botón "Nuevo ..." y lista de registros, común a todas las pestañas
@Composable
private fun <T> ListaCatalogo(
    textoNuevo: String,
    onNuevo: () -> Unit,
    elementos: List<T>,
    clave: (T) -> Int,
    contenido: @Composable (T) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Button(
            onClick = onNuevo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text(textoNuevo, modifier = Modifier.padding(start = 8.dp))
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(elementos, key = { clave(it) }) { contenido(it) }
        }
    }
}

// Tarjeta con texto a la izquierda y botón de edición a la derecha
@Composable
private fun TarjetaCatalogo(
    titulo: String,
    detalle: String,
    desactivado: Boolean,
    onEditar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(titulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(detalle, style = MaterialTheme.typography.bodyMedium)
                if (desactivado) {
                    Text(
                        "Desactivado",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar")
            }
        }
    }
}

// Interruptor "activo" que se muestra al editar un registro existente
@Composable
private fun FilaActivo(activo: Boolean, onCambio: (Boolean) -> Unit, error: String?) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Switch(checked = activo, onCheckedChange = onCambio)
        Text("Activo")
    }
    if (error != null) {
        Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
    }
}

// ---------- Vehículos ----------

@Composable
private fun PestanaVehiculos(viewModel: CatalogoViewModel, actor: Usuario) {
    val vehiculos by viewModel.vehiculos.collectAsState()
    val form by viewModel.vehiculoForm.collectAsState()

    ListaCatalogo(
        textoNuevo = "Nuevo vehículo",
        onNuevo = { viewModel.nuevoVehiculo(actor) },
        elementos = vehiculos,
        clave = { it.id }
    ) { v ->
        TarjetaCatalogo(
            titulo = "${v.patente} · ${v.descripcion}",
            detalle = "${v.tipo.etiqueta} · ${Formato.clpPorKm(v.costoPorKmClp)}",
            desactivado = !v.activo,
            onEditar = { viewModel.editarVehiculo(actor, v.id) }
        )
    }

    form?.let { f ->
        DialogoFormulario(
            titulo = if (f.id == 0) "Nuevo vehículo" else "Editar vehículo",
            onGuardar = { viewModel.guardarVehiculo(actor) },
            onCancelar = viewModel::cerrarVehiculo
        ) {
            CampoFormulario(
                valor = f.patente,
                onValorChange = viewModel::onPatenteVehiculoChange,
                etiqueta = "Patente",
                iconoInicial = Icons.Default.Info,
                error = f.errores["patente"],
                capitalizacion = KeyboardCapitalization.Characters
            )
            CampoFormulario(
                valor = f.descripcion,
                onValorChange = { valor -> viewModel.cambiarVehiculo("descripcion") { it.copy(descripcion = valor) } },
                etiqueta = "Descripción",
                iconoInicial = Icons.Default.Edit,
                error = f.errores["descripcion"]
            )
            SelectorDesplegable(
                etiqueta = "Tipo",
                opciones = TipoVehiculo.entries,
                seleccionada = f.tipo,
                textoDe = { it.etiqueta },
                onSeleccion = { tipo -> viewModel.cambiarVehiculo("tipo") { it.copy(tipo = tipo) } },
                error = null
            )
            CampoFormulario(
                valor = f.costoPorKm,
                onValorChange = { valor -> viewModel.cambiarVehiculo("costoPorKm") { it.copy(costoPorKm = valor) } },
                etiqueta = "Costo por kilómetro (CLP/km)",
                iconoInicial = Icons.Default.Info,
                error = f.errores["costoPorKm"],
                tipoTeclado = KeyboardType.Decimal
            )
            if (f.id != 0) {
                FilaActivo(
                    activo = f.activo,
                    onCambio = { valor -> viewModel.cambiarVehiculo("activo") { it.copy(activo = valor) } },
                    error = f.errores["activo"]
                )
            }
        }
    }
}

// ---------- Ubicaciones ----------

@Composable
private fun PestanaUbicaciones(viewModel: CatalogoViewModel, actor: Usuario) {
    val ubicaciones by viewModel.ubicaciones.collectAsState()
    val form by viewModel.ubicacionForm.collectAsState()

    ListaCatalogo(
        textoNuevo = "Nueva ubicación",
        onNuevo = { viewModel.nuevaUbicacion(actor) },
        elementos = ubicaciones,
        clave = { it.id }
    ) { u ->
        TarjetaCatalogo(
            titulo = u.nombre,
            detalle = "${u.ciudad} · ${if (u.tipo == TipoUbicacion.SEDE) "Sede" else "Cliente"}",
            desactivado = !u.activa,
            onEditar = { viewModel.editarUbicacion(actor, u.id) }
        )
    }

    form?.let { f ->
        DialogoFormulario(
            titulo = if (f.id == 0) "Nueva ubicación" else "Editar ubicación",
            onGuardar = { viewModel.guardarUbicacion(actor) },
            onCancelar = viewModel::cerrarUbicacion
        ) {
            CampoFormulario(
                valor = f.nombre,
                onValorChange = { valor -> viewModel.cambiarUbicacion("nombre") { it.copy(nombre = valor) } },
                etiqueta = "Nombre",
                iconoInicial = Icons.Default.Home,
                error = f.errores["nombre"]
            )
            CampoFormulario(
                valor = f.ciudad,
                onValorChange = { valor -> viewModel.cambiarUbicacion("ciudad") { it.copy(ciudad = valor) } },
                etiqueta = "Ciudad",
                iconoInicial = Icons.Default.LocationOn,
                error = f.errores["ciudad"]
            )
            CampoFormulario(
                valor = f.direccion,
                onValorChange = { valor -> viewModel.cambiarUbicacion("direccion") { it.copy(direccion = valor) } },
                etiqueta = "Dirección",
                iconoInicial = Icons.Default.LocationOn,
                error = f.errores["direccion"]
            )
            SelectorDesplegable(
                etiqueta = "Tipo",
                opciones = TipoUbicacion.entries,
                seleccionada = f.tipo,
                textoDe = { it.etiqueta },
                onSeleccion = { tipo -> viewModel.cambiarUbicacion("tipo") { it.copy(tipo = tipo) } },
                error = null
            )
            if (f.id != 0) {
                FilaActivo(
                    activo = f.activa,
                    onCambio = { valor -> viewModel.cambiarUbicacion("activa") { it.copy(activa = valor) } },
                    error = null
                )
            }
        }
    }
}

// ---------- Tramos ----------

@Composable
private fun PestanaTramos(viewModel: CatalogoViewModel, actor: Usuario) {
    val tramos by viewModel.tramos.collectAsState()
    val ubicaciones by viewModel.ubicaciones.collectAsState()
    val form by viewModel.tramoForm.collectAsState()
    val porId = ubicaciones.associateBy { it.id }

    ListaCatalogo(
        textoNuevo = "Nuevo tramo",
        onNuevo = { viewModel.nuevoTramo(actor) },
        elementos = tramos,
        clave = { it.id }
    ) { t ->
        TarjetaCatalogo(
            titulo = "${porId[t.origenId]?.nombre ?: "?"} ↔ ${porId[t.destinoId]?.nombre ?: "?"}",
            detalle = "${Formato.km(t.distanciaKm)} · peajes ${Formato.clp(t.peajeClp)}",
            desactivado = !t.activo,
            onEditar = { viewModel.editarTramo(actor, t.id) }
        )
    }

    form?.let { f ->
        DialogoFormulario(
            titulo = if (f.id == 0) "Nuevo tramo" else "Editar tramo",
            onGuardar = { viewModel.guardarTramo(actor) },
            onCancelar = viewModel::cerrarTramo
        ) {
            SelectorDesplegable(
                etiqueta = "Origen",
                opciones = ubicaciones,
                seleccionada = porId[f.origenId],
                textoDe = { it.nombre },
                onSeleccion = { u -> viewModel.cambiarTramo("origen") { it.copy(origenId = u.id) } },
                error = f.errores["origen"]
            )
            SelectorDesplegable(
                etiqueta = "Destino",
                opciones = ubicaciones,
                seleccionada = porId[f.destinoId],
                textoDe = { it.nombre },
                onSeleccion = { u -> viewModel.cambiarTramo("destino") { it.copy(destinoId = u.id) } },
                error = f.errores["destino"]
            )
            CampoFormulario(
                valor = f.distanciaKm,
                onValorChange = { valor -> viewModel.cambiarTramo("distancia") { it.copy(distanciaKm = valor) } },
                etiqueta = "Distancia oficial (km)",
                iconoInicial = Icons.Default.LocationOn,
                error = f.errores["distancia"],
                tipoTeclado = KeyboardType.Decimal
            )
            CampoFormulario(
                valor = f.peajeClp,
                onValorChange = { valor -> viewModel.cambiarTramo("peaje") { it.copy(peajeClp = valor) } },
                etiqueta = "Peajes del tramo (CLP)",
                iconoInicial = Icons.Default.Info,
                error = f.errores["peaje"],
                tipoTeclado = KeyboardType.Number
            )
            if (f.id != 0) {
                FilaActivo(
                    activo = f.activo,
                    onCambio = { valor -> viewModel.cambiarTramo("activo") { it.copy(activo = valor) } },
                    error = null
                )
            }
        }
    }
}
