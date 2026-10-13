package com.example.petcare.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.petcare.ui.components.CampoFormulario
import com.example.petcare.ui.components.SelectorEspecie
import com.example.petcare.viewmodel.MascotaViewModel

// Pantalla con el formulario de registro de mascota.
// La UI solo muestra el estado y delega la validación al ViewModel.
@Composable
fun RegistroScreen(
    viewModel: MascotaViewModel,
    onRegistroExitoso: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Observa el estado del formulario de forma reactiva
    val estado by viewModel.estado.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Datos de la mascota",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        CampoFormulario(
            valor = estado.nombre,
            onValorChange = viewModel::onNombreChange,
            etiqueta = "Nombre de la mascota",
            iconoInicial = Icons.Default.Favorite,
            error = estado.errores.nombre
        )

        SelectorEspecie(
            seleccionada = estado.especie,
            onSeleccion = viewModel::onEspecieChange,
            error = estado.errores.especie
        )

        CampoFormulario(
            valor = estado.edad,
            onValorChange = viewModel::onEdadChange,
            etiqueta = "Edad (años)",
            iconoInicial = Icons.Default.DateRange,
            error = estado.errores.edad,
            tipoTeclado = KeyboardType.Number
        )

        Text(
            text = "Datos del dueño",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        )

        CampoFormulario(
            valor = estado.nombreDueno,
            onValorChange = viewModel::onNombreDuenoChange,
            etiqueta = "Nombre del dueño",
            iconoInicial = Icons.Default.Person,
            error = estado.errores.nombreDueno
        )

        CampoFormulario(
            valor = estado.correo,
            onValorChange = viewModel::onCorreoChange,
            etiqueta = "Correo electrónico",
            iconoInicial = Icons.Default.Email,
            error = estado.errores.correo,
            tipoTeclado = KeyboardType.Email
        )

        CampoFormulario(
            valor = estado.telefono,
            onValorChange = viewModel::onTelefonoChange,
            etiqueta = "Teléfono",
            iconoInicial = Icons.Default.Phone,
            error = estado.errores.telefono,
            tipoTeclado = KeyboardType.Phone
        )

        // Aceptación de términos con mensaje de error propio
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = estado.aceptaTerminos,
                    onCheckedChange = viewModel::onAceptaTerminosChange
                )
                Text("Acepto el uso de mis datos para el registro")
            }
            if (estado.errores.terminos != null) {
                Text(
                    text = estado.errores.terminos!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
            onClick = {
                // Si el formulario es válido se guarda y se navega a la lista
                if (viewModel.validarFormulario()) {
                    viewModel.guardarMascota()
                    onRegistroExitoso()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Text("Guardar mascota")
        }
    }
}
