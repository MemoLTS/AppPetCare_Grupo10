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
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.Usuario
import com.example.teqruta.model.ValidadorCatalogo
import com.example.teqruta.ui.components.CampoFormulario
import com.example.teqruta.ui.components.SelectorDesplegable
import com.example.teqruta.viewmodel.DatosTraslados
import com.example.teqruta.viewmodel.TrasladoViewModel

// Registro rápido de una orden de traslado: patente del vehículo, origen y destino.
// El vehículo (registrado aparte por el administrador) se asocia por su patente;
// la hora de inicio la toma la app. Los kilómetros y el gasto se declaran al finalizar.
@Composable
fun TrasladoNuevoScreen(
    viewModel: TrasladoViewModel,
    usuario: Usuario,
    datos: DatosTraslados,
    onIniciado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.nuevo.collectAsState()
    val patente = ValidadorCatalogo.normalizarPatente(estado.patente)
    val vehiculo = datos.vehiculosPorPatente[patente]
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

        CampoFormulario(
            valor = estado.patente,
            onValorChange = viewModel::onPatenteChange,
            etiqueta = "Patente del vehículo",
            iconoInicial = Icons.Default.Info,
            error = estado.errores["patente"],
            capitalizacion = KeyboardCapitalization.Characters
        )

        // Confirma qué vehículo registrado corresponde a la patente escrita
        if (vehiculo != null && estado.errores["patente"] == null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Vehículo registrado", style = MaterialTheme.typography.labelLarge)
                    Text(
                        "${vehiculo.patente} · ${vehiculo.descripcion}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        if (vehiculo.activo) vehiculo.tipo.etiqueta else "${vehiculo.tipo.etiqueta} · Desactivado",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (vehiculo.activo) MaterialTheme.colorScheme.onSurface
                        else MaterialTheme.colorScheme.error
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
