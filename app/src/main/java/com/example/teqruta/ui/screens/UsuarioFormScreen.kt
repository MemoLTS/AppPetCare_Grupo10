package com.example.teqruta.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.Rol
import com.example.teqruta.model.Usuario
import com.example.teqruta.ui.components.CampoFormulario
import com.example.teqruta.ui.components.SelectorDesplegable
import com.example.teqruta.ui.components.SelectorRol
import com.example.teqruta.viewmodel.UsuarioViewModel

// Formulario para crear o editar un usuario (nombre, correo, contraseña, rol y estado).
// Si el rol es técnico, además se elige su jefe de servicio y su vehículo asignado.
@Composable
fun UsuarioFormScreen(
    viewModel: UsuarioViewModel,
    actor: Usuario,
    onGuardado: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.form.collectAsState()
    val usuarios by viewModel.usuarios.collectAsState()
    val vehiculos by viewModel.vehiculos.collectAsState()
    val editando = estado.editandoId != null
    // Nadie puede quitarse a sí mismo el rol ni desactivarse (evita quedarse sin acceso)
    val esUnoMismo = estado.editandoId == actor.id

    // Solo se ofrecen jefes y vehículos activos (más el que el técnico ya tenga asignado)
    val jefes = usuarios.filter { it.rol == Rol.JEFE_SERVICIO && (it.activo || it.id == estado.jefeId) }
    val vehiculosDisponibles = vehiculos.filter { it.activo || it.id == estado.vehiculoId }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (editando) "Editar usuario" else "Nuevo usuario",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth()
        )

        CampoFormulario(
            valor = estado.nombre,
            onValorChange = viewModel::onNombreChange,
            etiqueta = "Nombre completo",
            iconoInicial = Icons.Default.Person,
            error = estado.errores["nombre"]
        )

        CampoFormulario(
            valor = estado.correo,
            onValorChange = viewModel::onCorreoChange,
            etiqueta = "Correo electrónico",
            iconoInicial = Icons.Default.Email,
            error = estado.errores["correo"],
            tipoTeclado = KeyboardType.Email
        )

        CampoFormulario(
            valor = estado.clave,
            onValorChange = viewModel::onClaveChange,
            etiqueta = if (editando) "Nueva contraseña (opcional)" else "Contraseña",
            iconoInicial = Icons.Default.Lock,
            error = estado.errores["clave"],
            tipoTeclado = KeyboardType.Password,
            transformacion = PasswordVisualTransformation()
        )
        Text(
            text = "Mínimo 8 caracteres, con letras y números",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        )

        SelectorRol(
            seleccionado = estado.rol,
            onSeleccion = viewModel::onRolChange,
            error = estado.errores["rol"],
            habilitado = !esUnoMismo
        )

        if (estado.rol == Rol.TECNICO) {
            SelectorDesplegable(
                etiqueta = "Jefe de servicio (valida sus traslados)",
                opciones = jefes,
                seleccionada = jefes.firstOrNull { it.id == estado.jefeId },
                textoDe = { it.nombre },
                onSeleccion = { viewModel.onJefeChange(it.id) },
                error = estado.errores["jefe"]
            )
            SelectorDesplegable(
                etiqueta = "Vehículo asignado",
                opciones = vehiculosDisponibles,
                seleccionada = vehiculosDisponibles.firstOrNull { it.id == estado.vehiculoId },
                textoDe = { "${it.patente} · ${it.descripcion}" },
                onSeleccion = { viewModel.onVehiculoChange(it.id) },
                error = estado.errores["vehiculo"]
            )
        }

        if (editando) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Switch(
                    checked = estado.activo,
                    onCheckedChange = viewModel::onActivoChange,
                    enabled = !esUnoMismo
                )
                Text("Cuenta activa")
            }
        }
        if (esUnoMismo) {
            Text(
                text = "No puedes cambiar tu propio rol ni desactivar tu cuenta.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Button(
            onClick = {
                if (viewModel.validarFormulario() && viewModel.guardarUsuario(actor)) onGuardado()
            },
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 480.dp)
        ) {
            Text(if (editando) "Guardar cambios" else "Crear usuario")
        }
    }
}
