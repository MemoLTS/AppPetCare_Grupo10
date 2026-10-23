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
import com.example.teqruta.model.EstadoTraslado
import com.example.teqruta.model.Usuario
import com.example.teqruta.ui.components.CampoFormulario
import com.example.teqruta.ui.components.MensajeCentrado
import com.example.teqruta.ui.utils.Formato
import com.example.teqruta.viewmodel.DatosTraslados
import com.example.teqruta.viewmodel.TrasladoViewModel

// Cierre del traslado en curso: el técnico declara los kilómetros recorridos (con decimales) y,
// si quiere, el gasto del vehículo en pesos (opcional).
// El costo lo calcula el sistema con datos oficiales; el técnico no lo ve ni lo ingresa.
@Composable
fun TrasladoFinalizarScreen(
    viewModel: TrasladoViewModel,
    usuario: Usuario,
    datos: DatosTraslados,
    onFinalizado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.finalizar.collectAsState()
    val enCurso = datos.traslados.firstOrNull {
        it.tecnicoId == usuario.id && it.estado == EstadoTraslado.EN_CURSO
    }

    if (enCurso == null) {
        MensajeCentrado("No tienes un traslado en curso.", modifier)
        return
    }

    val origen = datos.ubicacionesPorId[enCurso.origenId]?.nombre ?: "Origen"
    val destino = datos.ubicacionesPorId[enCurso.destinoId]?.nombre ?: "Destino"
    val vehiculo = datos.vehiculosPorPatente[enCurso.patente]

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Finalizar traslado",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("$origen → $destino", style = MaterialTheme.typography.titleMedium)
                Text("Inicio: ${Formato.fechaHora(enCurso.inicioMs)}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Vehículo: ${enCurso.patente}" + (vehiculo?.let { " · ${it.descripcion}" } ?: ""),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        CampoFormulario(
            valor = estado.distanciaKm,
            onValorChange = viewModel::onDistanciaChange,
            etiqueta = "Kilómetros recorridos (ej: 12,5)",
            iconoInicial = Icons.Default.Info,
            error = estado.errores["distancia"],
            tipoTeclado = KeyboardType.Decimal
        )

        CampoFormulario(
            valor = estado.gastoClp,
            onValorChange = viewModel::onGastoChange,
            etiqueta = "Gasto del vehículo en pesos (opcional)",
            iconoInicial = Icons.Default.Info,
            error = estado.errores["gasto"],
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
            onClick = { if (viewModel.finalizarTraslado(usuario)) onFinalizado() },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Text("Finalizar y enviar a validación")
        }
    }
}
