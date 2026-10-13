package com.example.petcare.model

// Reglas de validación del formulario. Son funciones puras (sin UI ni Android),
object ValidadorMascota {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_TELEFONO = Regex("^[0-9]{9}$")

    fun validarNombreMascota(valor: String): String? = when {
        valor.isBlank() -> "El nombre de la mascota es obligatorio"
        valor.trim().length < 2 -> "Debe tener al menos 2 caracteres"
        !valor.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras"
        else -> null
    }

    fun validarEspecie(valor: String): String? =
        if (valor.isBlank()) "Selecciona una especie" else null

    fun validarEdad(valor: String): String? {
        if (valor.isBlank()) return "La edad es obligatoria"
        val edad = valor.toIntOrNull() ?: return "Debe ser un número entero"
        return if (edad !in 0..30) "La edad debe estar entre 0 y 30 años" else null
    }

    fun validarNombreDueno(valor: String): String? = when {
        valor.isBlank() -> "El nombre del dueño es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        !valor.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras"
        else -> null
    }

    fun validarCorreo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(valor.trim()) -> "Formato de correo inválido (ej: nombre@correo.com)"
        else -> null
    }

    fun validarTelefono(valor: String): String? = when {
        valor.isBlank() -> "El teléfono es obligatorio"
        !REGEX_TELEFONO.matches(valor) -> "Debe tener 9 dígitos (ej: 912345678)"
        else -> null
    }

    fun validarTerminos(acepta: Boolean): String? =
        if (!acepta) "Debes aceptar los términos para continuar" else null
}
