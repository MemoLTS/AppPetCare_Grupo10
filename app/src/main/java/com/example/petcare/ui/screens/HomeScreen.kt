package com.example.petcare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.background
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.petcare.ui.theme.PetCareTheme
import com.example.petcare.ui.utils.TipoPantalla

// Pantalla de inicio adaptable: en pantallas compactas usa una columna,
// y en medianas/expandidas separa el encabezado y las acciones en dos paneles.
@Composable
fun HomeScreen(
    tipoPantalla: TipoPantalla,
    totalMascotas: Int,
    onRegistrar: () -> Unit,
    onVerLista: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (tipoPantalla == TipoPantalla.COMPACTA) {
        // Diseño vertical para teléfonos
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PanelEncabezado()
            PanelAcciones(totalMascotas, onRegistrar, onVerLista)
        }
    } else {
        // Diseño horizontal para tablets / pantallas grandes
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalArrangement = Arrangement.spacedBy(32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PanelEncabezado()
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                PanelAcciones(totalMascotas, onRegistrar, onVerLista)
            }
        }
    }
}

// Logo + título + descripción de la app
@Composable
private fun PanelEncabezado() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Logo PetCare",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "PetCare",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Registro y seguimiento de mascotas para tu clínica veterinaria",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Tarjeta con el total de mascotas y los botones principales
@Composable
private fun PanelAcciones(
    totalMascotas: Int,
    onRegistrar: () -> Unit,
    onVerLista: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$totalMascotas",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Text(text = "mascotas registradas", style = MaterialTheme.typography.bodyMedium)
            }
        }
        Button(onClick = onRegistrar, modifier = Modifier.fillMaxWidth()) {
            Text("Registrar nueva mascota")
        }
        OutlinedButton(onClick = onVerLista, modifier = Modifier.fillMaxWidth()) {
            Text("Ver mascotas registradas")
        }
    }
}

// Previews para ver el diseño en cada tamaño de pantalla
@Preview(name = "Compacta", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun HomeCompactaPreview() {
    PetCareTheme { HomeScreen(TipoPantalla.COMPACTA, 3, {}, {}) }
}

@Preview(name = "Expandida", widthDp = 900, heightDp = 500, showBackground = true)
@Composable
private fun HomeExpandidaPreview() {
    PetCareTheme { HomeScreen(TipoPantalla.EXPANDIDA, 3, {}, {}) }
}
