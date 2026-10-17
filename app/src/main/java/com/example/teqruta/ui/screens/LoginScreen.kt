package com.example.teqruta.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.teqruta.ui.components.CampoFormulario
import com.example.teqruta.viewmodel.UsuarioViewModel

// Pantalla de inicio de sesión. La validación y la verificación de credenciales están en el ViewModel.
@Composable
fun LoginScreen(
    viewModel: UsuarioViewModel,
    onLoginExitoso: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.login.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            modifier = Modifier.widthIn(max = 480.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                    modifier = Modifier.size(52.dp)
                )
            }
            Text(
                text = "TeqRuta",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text("Control de traslados en terreno · TeqMed", style = MaterialTheme.typography.bodyLarge)

            CampoFormulario(
                valor = estado.correo,
                onValorChange = viewModel::onLoginCorreoChange,
                etiqueta = "Correo electrónico",
                iconoInicial = Icons.Default.Email,
                error = estado.errorCorreo,
                tipoTeclado = KeyboardType.Email
            )

            CampoFormulario(
                valor = estado.clave,
                onValorChange = viewModel::onLoginClaveChange,
                etiqueta = "Contraseña",
                iconoInicial = Icons.Default.Lock,
                error = estado.errorClave,
                tipoTeclado = KeyboardType.Password,
                transformacion = PasswordVisualTransformation()
            )

            if (estado.errorGeneral != null) {
                Text(
                    text = estado.errorGeneral!!,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Button(
                onClick = { if (viewModel.iniciarSesion()) onLoginExitoso() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Iniciar sesión")
            }
        }
    }
}
