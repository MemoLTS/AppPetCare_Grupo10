package com.example.petcare.model

// Estado del formulario de registro. Los campos de texto se guardan como String
data class MascotaUiState(
    val nombre: String = "",
    val especie: String = "",
    val edad: String = "",
    val nombreDueno: String = "",
    val correo: String = "",
    val telefono: String = "",
    val aceptaTerminos: Boolean = false,
    val errores: MascotaErrores = MascotaErrores()
)

// Mensaje de error por campo (null = campo sin error).
data class MascotaErrores(
    val nombre: String? = null,
    val especie: String? = null,
    val edad: String? = null,
    val nombreDueno: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    val terminos: String? = null
) {
    // Indica si existe al menos un error en el formulario.
    fun tieneErrores(): Boolean = listOf(
        nombre, especie, edad, nombreDueno, correo, telefono, terminos
    ).any { it != null }
}
