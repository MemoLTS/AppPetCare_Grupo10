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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
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
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.Rol
import com.example.teqruta.model.Usuario
import com.example.teqruta.viewmodel.UsuarioViewModel

// Apartado de usuarios (solo administradores): lista de usuarios con acceso a crear y editar.
@Composable
fun UsuariosScreen(
    viewModel: UsuarioViewModel,
    actor: Usuario,
    onNuevo: () -> Unit,
    onEditar: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val usuarios by viewModel.usuarios.collectAsState()
    val usuariosPorId = usuarios.associateBy { it.id }

    Column(modifier = modifier.fillMaxSize()) {
        Button(
            onClick = onNuevo,
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Nuevo usuario", modifier = Modifier.padding(start = 8.dp))
        }
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(usuarios, key = { it.id }) { usuario ->
                TarjetaUsuario(
                    usuario = usuario,
                    esUnoMismo = usuario.id == actor.id,
                    nombreJefe = usuario.jefeId?.let { usuariosPorId[it]?.nombre },
                    onEditar = { onEditar(usuario.id) }
                )
            }
        }
    }
}

@Composable
private fun TarjetaUsuario(
    usuario: Usuario,
    esUnoMismo: Boolean,
    nombreJefe: String?,
    onEditar: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (esUnoMismo) "${usuario.nombre} (tú)" else usuario.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(usuario.correo, style = MaterialTheme.typography.bodyMedium)
                Text(usuario.rol.etiqueta, style = MaterialTheme.typography.bodySmall)
                if (usuario.rol == Rol.TECNICO) {
                    Text(
                        text = "Jefe: ${nombreJefe ?: "sin asignar"}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                if (!usuario.activo) {
                    Text(
                        text = "Cuenta desactivada",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            IconButton(onClick = onEditar) {
                Icon(Icons.Default.Edit, contentDescription = "Editar usuario")
            }
        }
    }
}
