package com.example.teqruta.model

// Usuario de la aplicación. La contraseña nunca se guarda: solo su hash y la sal usada.
// Solo se almacenan los datos personales indispensables (nombre y correo).
// jefeId solo aplica a los técnicos: su jefe de servicio (quien valida sus traslados).
// El vehículo no se asigna al usuario: se indica por patente en cada orden de traslado.
data class Usuario(
    val id: Int,
    val nombre: String,
    val correo: String,
    val rol: Rol,
    val activo: Boolean = true,
    val salt: String,
    val hashClave: String,
    val jefeId: Int? = null
)
