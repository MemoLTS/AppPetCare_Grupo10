package com.example.teqruta.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.teqruta.ui.theme.TeqRutaTheme
import com.example.teqruta.ui.utils.TipoPantalla

// Un dato destacado del resumen (por ejemplo "3" / "traslados por validar")
data class DatoHome(val valor: String, val etiqueta: String)

// Un botón de acción de la pantalla de inicio
data class AccionHome(val texto: String, val principal: Boolean, val onClick: () -> Unit)

// Pantalla de inicio adaptable. Su contenido (datos, avisos y acciones) lo arma la navegación
// según el rol de quien inició sesión. En pantallas compactas usa una columna; en medianas y
// expandidas separa el encabezado y el resumen en dos paneles.
@Composable
fun HomeScreen(
    tipoPantalla: TipoPantalla,
    saludo: String,
    datos: List<DatoHome>,
    avisos: List<String>,
    acciones: List<AccionHome>,
    modifier: Modifier = Modifier
) {
    if (tipoPantalla == TipoPantalla.COMPACTA) {
        // Diseño vertical para teléfonos
        Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PanelEncabezado(saludo)
            PanelResumen(datos, avisos, acciones)
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
                PanelEncabezado(saludo)
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                contentAlignment = Alignment.Center
            ) {
                PanelResumen(datos, avisos, acciones)
            }
        }
    }
}

// Logo + título + descripción de la app + quién tiene la sesión
@Composable
private fun PanelEncabezado(saludo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Logo TeqRuta",
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "TeqRuta",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Control de traslados en terreno",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = saludo,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

// Datos destacados, avisos y botones de acción propios del rol
@Composable
private fun PanelResumen(
    datos: List<DatoHome>,
    avisos: List<String>,
    acciones: List<AccionHome>
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        datos.forEach { dato ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dato.valor,
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                    Text(
                        text = dato.etiqueta,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        avisos.forEach { aviso ->
            Text(
                text = aviso,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        acciones.forEach { accion ->
            if (accion.principal) {
                Button(onClick = accion.onClick, modifier = Modifier.fillMaxWidth()) { Text(accion.texto) }
            } else {
                OutlinedButton(onClick = accion.onClick, modifier = Modifier.fillMaxWidth()) { Text(accion.texto) }
            }
        }
    }
}

// Previews para ver el diseño en cada tamaño de pantalla
private val datosPreview = listOf(DatoHome("3", "traslados por validar"))
private val accionesPreview = listOf(
    AccionHome("Revisar traslados", true) {},
    AccionHome("Ver costos", false) {}
)

@Preview(name = "Compacta", widthDp = 360, heightDp = 640, showBackground = true)
@Composable
private fun HomeCompactaPreview() {
    TeqRutaTheme {
        HomeScreen(TipoPantalla.COMPACTA, "Jefa Servicio Uno · Jefe de servicio técnico", datosPreview, emptyList(), accionesPreview)
    }
}

@Preview(name = "Expandida", widthDp = 900, heightDp = 500, showBackground = true)
@Composable
private fun HomeExpandidaPreview() {
    TeqRutaTheme {
        HomeScreen(TipoPantalla.EXPANDIDA, "Jefa Servicio Uno · Jefe de servicio técnico", datosPreview, emptyList(), accionesPreview)
    }
}
