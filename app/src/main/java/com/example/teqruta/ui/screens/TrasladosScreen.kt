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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.EstadoTraslado
import com.example.teqruta.model.PoliticaAcceso
import com.example.teqruta.model.Traslado
import com.example.teqruta.model.Usuario
import com.example.teqruta.ui.components.CampoFormulario
import com.example.teqruta.ui.components.DialogoFormulario
import com.example.teqruta.ui.components.MensajeCentrado
import com.example.teqruta.ui.utils.Formato
import com.example.teqruta.viewmodel.DatosTraslados
import com.example.teqruta.viewmodel.TrasladoViewModel

private enum class FiltroTraslados(val etiqueta: String) {
    TODOS("Todos"),
    POR_VALIDAR("Por validar"),
    CON_DESVIACION("Con desviación")
}

// Lista de traslados. Cada rol ve solo los suyos (técnico), los de su equipo (jefe) o todos
// (gerencia). El jefe de servicio valida o rechaza desde aquí los traslados pendientes.
@Composable
fun TrasladosScreen(
    viewModel: TrasladoViewModel,
    usuario: Usuario,
    datos: DatosTraslados,
    modifier: Modifier = Modifier
) {
    val puedeVerCostos = PoliticaAcceso.puedeVerCostos(usuario)
    var filtro by rememberSaveable {
        mutableStateOf(if (PoliticaAcceso.puedeValidar(usuario)) FiltroTraslados.POR_VALIDAR else FiltroTraslados.TODOS)
    }

    val visibles = remember(datos, usuario) {
        PoliticaAcceso.filtrarVisibles(usuario, datos.traslados, datos.usuarios)
            .sortedByDescending { it.inicioMs }
    }
    val filtrados = when (filtro) {
        FiltroTraslados.TODOS -> visibles
        FiltroTraslados.POR_VALIDAR -> visibles.filter { it.estado == EstadoTraslado.PENDIENTE_VALIDACION }
        FiltroTraslados.CON_DESVIACION -> visibles.filter { it.hayDesviacion }
    }
    // La desviación es un control interno: el técnico no ve este filtro
    val filtrosDisponibles = FiltroTraslados.entries.filter {
        it != FiltroTraslados.CON_DESVIACION || puedeVerCostos
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filtrosDisponibles.forEach { opcion ->
                FilterChip(
                    selected = filtro == opcion,
                    onClick = { filtro = opcion },
                    label = { Text(opcion.etiqueta) }
                )
            }
        }

        if (filtrados.isEmpty()) {
            MensajeCentrado(
                texto = if (visibles.isEmpty()) "Aún no hay traslados registrados."
                else "No hay traslados con este filtro.",
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filtrados, key = { it.id }) { traslado ->
                    val tecnico = datos.usuariosPorId[traslado.tecnicoId]
                    TarjetaTraslado(
                        traslado = traslado,
                        datos = datos,
                        mostrarTecnico = traslado.tecnicoId != usuario.id,
                        mostrarCostos = puedeVerCostos,
                        puedeResolver = PoliticaAcceso.puedeValidarTraslado(usuario, traslado, tecnico),
                        onValidar = { viewModel.validarTraslado(usuario, traslado.id) },
                        onRechazar = { viewModel.abrirRechazo(usuario, traslado.id) }
                    )
                }
            }
        }
    }

    // Diálogo para registrar el motivo de un rechazo
    val rechazo by viewModel.rechazo.collectAsState()
    rechazo?.let { form ->
        DialogoFormulario(
            titulo = "Rechazar traslado",
            textoGuardar = "Rechazar",
            onGuardar = { viewModel.confirmarRechazo(usuario) },
            onCancelar = viewModel::cerrarRechazo
        ) {
            Text("El técnico verá este motivo.", style = MaterialTheme.typography.bodyMedium)
            CampoFormulario(
                valor = form.motivo,
                onValorChange = viewModel::onMotivoChange,
                etiqueta = "Motivo del rechazo",
                iconoInicial = Icons.Default.Edit,
                error = form.error
            )
        }
    }
}

@Composable
private fun colorEstado(estado: EstadoTraslado): Color = when (estado) {
    EstadoTraslado.EN_CURSO -> MaterialTheme.colorScheme.tertiary
    EstadoTraslado.PENDIENTE_VALIDACION -> MaterialTheme.colorScheme.secondary
    EstadoTraslado.VALIDADO -> MaterialTheme.colorScheme.primary
    EstadoTraslado.RECHAZADO -> MaterialTheme.colorScheme.error
}

@Composable
private fun TarjetaTraslado(
    traslado: Traslado,
    datos: DatosTraslados,
    mostrarTecnico: Boolean,
    mostrarCostos: Boolean,
    puedeResolver: Boolean,
    onValidar: () -> Unit,
    onRechazar: () -> Unit
) {
    val origen = datos.ubicacionesPorId[traslado.origenId]?.nombre ?: "Origen"
    val destino = datos.ubicacionesPorId[traslado.destinoId]?.nombre ?: "Destino"
    val calculo = traslado.calculo

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text("$origen → $destino", style = MaterialTheme.typography.titleMedium)
            Text(
                text = traslado.estado.etiqueta,
                style = MaterialTheme.typography.labelLarge,
                color = colorEstado(traslado.estado)
            )
            Text(
                text = "Inicio: ${Formato.fechaHora(traslado.inicioMs)}" +
                    (traslado.finMs?.let { " · Fin: ${Formato.hora(it)}" } ?: ""),
                style = MaterialTheme.typography.bodySmall
            )
            if (mostrarTecnico) {
                Text(
                    text = "Técnico: ${datos.usuariosPorId[traslado.tecnicoId]?.nombre ?: "Desconocido"}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = "Vehículo: ${datos.vehiculosPorId[traslado.vehiculoId]?.patente ?: "Desconocido"}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (traslado.kmFinal != null) {
                    "Odómetro: ${traslado.kmInicial} → ${traslado.kmFinal} km (${traslado.kmDeclarados} km declarados)"
                } else {
                    "Odómetro inicial: ${traslado.kmInicial} km"
                },
                style = MaterialTheme.typography.bodySmall
            )

            if (mostrarCostos && calculo != null) {
                Text(
                    text = "Distancia oficial: ${Formato.km(calculo.distanciaOficialKm)} · " +
                        "Costo: ${Formato.clp(calculo.costoTotalClp)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Combustible ${Formato.clp(calculo.costoCombustibleClp)} " +
                        "(${Formato.clp(calculo.precioLitroClp)}/L) + peajes ${Formato.clp(calculo.peajeClp)}",
                    style = MaterialTheme.typography.bodySmall
                )
                if (calculo.hayDesviacion) {
                    Text(
                        text = "Desviación ${Formato.porcentaje(calculo.desviacionPct)} entre lo declarado y la distancia oficial",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            if (traslado.estado == EstadoTraslado.RECHAZADO && traslado.motivoRechazo != null) {
                Text(
                    text = "Motivo del rechazo: ${traslado.motivoRechazo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            if (puedeResolver) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onValidar, modifier = Modifier.weight(1f)) { Text("Validar") }
                    OutlinedButton(onClick = onRechazar, modifier = Modifier.weight(1f)) { Text("Rechazar") }
                }
            }
        }
    }
}
