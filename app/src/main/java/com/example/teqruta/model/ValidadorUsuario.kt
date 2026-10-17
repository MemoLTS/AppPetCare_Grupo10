package com.example.teqruta.model

// Reglas de validación de usuarios y del inicio de sesión (funciones puras, sin UI).
object ValidadorUsuario {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

    fun validarNombre(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        !valor.all { it.isLetter() || it == ' ' } -> "Solo se permiten letras"
        else -> null
    }

    fun validarCorreo(valor: String): String? = when {
        valor.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(valor.trim()) -> "Formato de correo inválido (ej: nombre@correo.com)"
        else -> null
    }

    // Al editar un usuario la contraseña es opcional (vacía = no cambiarla).
    fun validarClave(valor: String, obligatoria: Boolean): String? = when {
        valor.isEmpty() -> if (obligatoria) "La contraseña es obligatoria" else null
        valor.length < 8 -> "Debe tener al menos 8 caracteres"
        !valor.any { it.isLetter() } || !valor.any { it.isDigit() } -> "Debe incluir letras y números"
        else -> null
    }

    fun validarRol(rol: Rol?): String? =
        if (rol == null) "Selecciona un rol" else null
}
