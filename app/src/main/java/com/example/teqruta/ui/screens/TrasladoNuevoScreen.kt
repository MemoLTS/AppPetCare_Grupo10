package com.example.teqruta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.Usuario
import com.example.teqruta.ui.components.CampoFormulario
import com.example.teqruta.ui.components.SelectorDesplegable
import com.example.teqruta.viewmodel.DatosTraslados
import com.example.teqruta.viewmodel.TrasladoViewModel

// Registro rápido del inicio de un traslado: origen, destino y kilometraje del odómetro.
// El vehículo es el que el técnico tiene asignado; la hora de inicio la toma la app.
@Composable
fun TrasladoNuevoScreen(
    viewModel: TrasladoViewModel,
    usuario: Usuario,
    datos: DatosTraslados,
    onIniciado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.nuevo.collectAsState()
    val vehiculo = usuario.vehiculoId?.let { datos.vehiculosPorId[it] }
    val ubicaciones = datos.ubicaciones.filter { it.activa }
    val origen = datos.ubicacionesPorId[estado.origenId]
    val destino = datos.ubicacionesPorId[estado.destinoId]
    val tramoRegistrado = viewModel.tramoOficial(estado.origenId, estado.destinoId) != null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Iniciar traslado",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Vehículo asignado", style = MaterialTheme.typography.labelLarge)
                if (vehiculo != null) {
                    Text("${vehiculo.patente} · ${vehiculo.descripcion}", style = MaterialTheme.typography.bodyLarge)
                    Text(vehiculo.tipo.etiqueta, style = MaterialTheme.typography.bodySmall)
                } else {
                    Text(
                        "No tienes un vehículo asignado. Contacta al administrador.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }

        SelectorDesplegable(
            etiqueta = "Origen",
            opciones = ubicaciones,
            seleccionada = origen,
            textoDe = { it.nombre },
            onSeleccion = { viewModel.onOrigenChange(it.id) },
            error = estado.errores["origen"]
        )

        SelectorDesplegable(
            etiqueta = "Destino",
            opciones = ubicaciones,
            seleccionada = destino,
            textoDe = { it.nombre },
            onSeleccion = { viewModel.onDestinoChange(it.id) },
            error = estado.errores["destino"]
        )

        // Solo se confirma que el tramo existe; la distancia oficial no se muestra al técnico
        if (origen != null && destino != null && origen.id != destino.id) {
            Text(
                text = if (tramoRegistrado) "Tramo oficial registrado" else "Aún no hay un tramo oficial entre estas ubicaciones",
                style = MaterialTheme.typography.bodySmall,
                color = if (tramoRegistrado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        CampoFormulario(
            valor = estado.kmInicial,
            onValorChange = viewModel::onKmInicialChange,
            etiqueta = "Kilometraje inicial (odómetro)",
            iconoInicial = Icons.Default.Info,
            error = estado.errores["km"],
            tipoTeclado = KeyboardType.Number
        )

        if (estado.errores["general"] != null) {
            Text(
                text = estado.errores["general"]!!,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = { if (viewModel.iniciarTraslado(usuario)) onIniciado() },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Text("Iniciar traslado")
        }
    }
}
