package com.example.teqruta.model

// Usuario de la aplicación. La contraseña nunca se guarda: solo su hash y la sal usada.
// Solo se almacenan los datos personales indispensables (nombre y correo).
// jefeId y vehiculoId solo aplican a los técnicos: su jefe de servicio (quien valida sus
// traslados) y el vehículo que tienen asignado.
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean = true,
    val salt: String,
    val hashClave: String,
    val jefeId: Int? = null,
    val vehiculoId: Int? = null
)
