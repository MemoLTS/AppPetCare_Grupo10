
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
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.DimensionReporte
import com.example.teqruta.model.EstadoTraslado
import com.example.teqruta.model.FilaResumen
import com.example.teqruta.model.ReporteCostos
import com.example.teqruta.ui.components.MensajeCentrado
import com.example.teqruta.ui.utils.Formato
import com.example.teqruta.viewmodel.DatosTraslados

@Composable
fun ReportesScreen(
    datos: DatosTraslados,
    modifier: Modifier = Modifier
) {
    var dimension by rememberSaveable {
        mutableStateOf(DimensionReporte.CLIENTE)
    }

    val filas = remember(datos, dimension) {
        ReporteCostos.resumir(
            datos.traslados,
            dimension,
            datos.usuarios,
            datos.vehiculos,
            datos.ubicaciones
        )
    }

    val total = filas.sumOf { it.costoTotalClp }
    val validados = filas.sumOf { it.traslados }
    val conDesviacion = filas.sumOf { it.conDesviacion }
    val pendientes = datos.traslados.count {
        it.estado == EstadoTraslado.PENDIENTE_VALIDACION
    }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DimensionReporte.entries.forEach { opcion ->
                FilterChip(
                    selected = dimension == opcion,
                    onClick = { dimension = opcion },
                    label = {
                        Text("Por ${opcion.etiqueta.lowercase()}")
                    }
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Costo total validado",
                    style = MaterialTheme.typography.labelLarge
                )

                Text(
                    text = Formato.clp(total),
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )

                Text(
                    text = "$validados traslados validados · $conDesviacion con desviación",
                    style = MaterialTheme.typography.bodySmall
                )

                if (pendientes > 0) {
                    Text(
                        text = "$pendientes traslados aún por validar (no incluidos)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (filas.isEmpty()) {
            MensajeCentrado(
                "Aún no hay traslados validados para informar.",
                Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filas, key = { it.etiqueta }) { fila ->
                    TarjetaFila(fila)
                }
            }
        }
    }
}

@Composable
private fun TarjetaFila(fila: FilaResumen) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = fila.etiqueta,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                Formato.clp(fila.costoTotalClp),
                style = MaterialTheme.typography.titleLarge
            )

            Text(
                text = "${fila.traslados} traslados · ${Formato.km(fila.kmOficiales)} oficiales",
                style = MaterialTheme.typography.bodySmall
            )

            if (fila.gastoDeclaradoClp > 0) {
                Text(
                    text = "Gasto del vehículo declarado: ${Formato.clp(fila.gastoDeclaradoClp)}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (fila.conDesviacion > 0) {
                Text(
                    text = "${fila.conDesviacion} con desviación",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
