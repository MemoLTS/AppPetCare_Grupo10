package com.example.teqruta.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.teqruta.model.Rol

// Selector de rol con una descripción de lo que puede hacer cada uno.
// Muestra todos los roles definidos en el enum Rol, así que uno nuevo aparece solo.
@Composable
fun SelectorRol(
    seleccionado: Rol?,
    onSeleccion: (Rol) -> Unit,
    error: String?,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = "Rol",
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurface
        )
        Rol.entries.forEach { rol ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = seleccionado == rol,
                        enabled = habilitado,
                        onClick = { onSeleccion(rol) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(selected = seleccionado == rol, onClick = null, enabled = habilitado)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(rol.etiqueta, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        rol.descripcion,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
