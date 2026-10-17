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

// Cierre del traslado en curso: el técnico declara el kilometraje final del odómetro.
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
    val vehiculo = datos.vehiculosPorId[enCurso.vehiculoId]
    val kmFinal = estado.kmFinal.trim().toIntOrNull()
    val declarados = kmFinal?.let { it - enCurso.kmInicial }

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
                if (vehiculo != null) {
                    Text("Vehículo: ${vehiculo.patente}", style = MaterialTheme.typography.bodyMedium)
                }
                Text("Kilometraje inicial: ${enCurso.kmInicial} km", style = MaterialTheme.typography.bodyMedium)
            }
        }

        CampoFormulario(
            valor = estado.kmFinal,
            onValorChange = viewModel::onKmFinalChange,
            etiqueta = "Kilometraje final (odómetro)",
            iconoInicial = Icons.Default.Info,
            error = estado.error,
            tipoTeclado = KeyboardType.Number
        )

        if (declarados != null && declarados > 0) {
            Text(
                text = "Distancia declarada: $declarados km",
                style = MaterialTheme.typography.bodyMedium,
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
