package com.example.petcare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.petcare.model.Mascota
import com.example.petcare.viewmodel.MascotaViewModel

// Pantalla con el listado de mascotas registradas
@Composable
fun ListaScreen(
    viewModel: MascotaViewModel,
    modifier: Modifier = Modifier
) {
    val mascotas by viewModel.mascotas.collectAsState()

    if (mascotas.isEmpty()) {
        // Estado vacío: se informa al usuario qué hacer
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Aún no hay mascotas registradas.\nUsa la pestaña \"Registrar\" para agregar la primera.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
    } else {
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(mascotas, key = { it.id }) { mascota ->
                TarjetaMascota(mascota = mascota, onEliminar = { viewModel.eliminarMascota(mascota.id) })
            }
        }
    }
}

// Tarjeta con los datos de una mascota y su dueño
@Composable
private fun TarjetaMascota(mascota: Mascota, onEliminar: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mascota.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("${mascota.especie} · ${mascota.edad} años")
                Text("Dueño: ${mascota.nombreDueno}")
                Text(mascota.correo, style = MaterialTheme.typography.bodySmall)
                Text("Tel: ${mascota.telefono}", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onEliminar) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar mascota",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
