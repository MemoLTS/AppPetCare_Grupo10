package com.example.teqruta.model

// Validaciones de los formularios del administrador (funciones puras, sin UI).
object ValidadorCatalogo {

    // Patentes chilenas: formato antiguo (2 letras + 4 números) o nuevo (4 consonantes + 2 números)
    private val REGEX_PATENTE = Regex("^([A-Z]{2}[0-9]{4}|[BCDFGHJKLPRSTVWXYZ]{4}[0-9]{2})$")

    fun normalizarPatente(valor: String): String =
        valor.uppercase().filter { it.isLetterOrDigit() }

    fun validarPatente(valor: String): String? = when {
        valor.isBlank() -> "La patente es obligatoria"
        !REGEX_PATENTE.matches(normalizarPatente(valor)) -> "Formato inválido (ej: ABCD12 o AB1234)"
        else -> null
    }

    fun validarDescripcion(valor: String): String? = when {
        valor.isBlank() -> "La descripción es obligatoria"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }

    // Acepta coma o punto decimal. Retorna null si no es un número válido.
    fun parsearDecimal(valor: String): Double? = valor.trim().replace(',', '.').toDoubleOrNull()

    fun validarRendimiento(valor: String): String? {
        val n = parsearDecimal(valor) ?: return "Ingresa un número (ej: 11,5)"
        return if (n < 3.0 || n > 40.0) "Debe estar entre 3 y 40 km por litro" else null
    }

    fun validarNombreUbicacion(valor: String): String? = when {
        valor.isBlank() -> "El nombre es obligatorio"
        valor.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        else -> null
    }

    fun validarCiudad(valor: String): String? =
        if (valor.isBlank()) "La ciudad es obligatoria" else null

    fun validarDireccion(valor: String): String? = when {
        valor.isBlank() -> "La dirección es obligatoria"
        valor.trim().length < 5 -> "Debe tener al menos 5 caracteres"
        else -> null
    }

    fun validarDistancia(valor: String): String? {
        val n = parsearDecimal(valor) ?: return "Ingresa un número (ej: 112,5)"
        return if (n <= 0.0 || n > 1500.0) "Debe ser mayor a 0 y hasta 1.500 km" else null
    }

    fun validarPeaje(valor: String): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa un monto entero en pesos (0 si no hay peaje)"
        return if (n < 0 || n > 200_000) "Debe estar entre $0 y $200.000" else null
    }

    fun validarPrecioLitro(valor: String): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa el precio en pesos por litro"
        return if (n < 300 || n > 5_000) "Debe estar entre $300 y $5.000 por litro" else null
    }
}
