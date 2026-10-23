package com.example.teqruta.model

// Validaciones del registro de traslados (funciones puras, sin UI).
object ValidadorTraslado {

    const val MAX_KM_POR_TRASLADO = 2_000.0
    const val MAX_GASTO_CLP = 5_000_000

    // Acepta coma o punto decimal. Retorna null si no es un número válido.
    fun parsearDistancia(valor: String): Double? = ValidadorCatalogo.parsearDecimal(valor)

    // Los kilómetros son obligatorios y pueden llevar decimales (ej: 112,5)
    fun validarDistancia(valor: String): String? {
        val n = parsearDistancia(valor) ?: return "Ingresa los kilómetros recorridos (ej: 112,5)"
        return when {
            n <= 0.0 -> "Debe ser mayor a 0 km"
            n > MAX_KM_POR_TRASLADO -> "Un traslado no puede superar ${MAX_KM_POR_TRASLADO.toInt()} km; revisa el valor"
            else -> null
        }
    }

    fun parsearGasto(valor: String): Int? = valor.trim().toIntOrNull()

    // El gasto del vehículo (en pesos) es opcional: vacío es válido
    fun validarGasto(valor: String): String? {
        if (valor.isBlank()) return null
        val n = parsearGasto(valor) ?: return "Ingresa un monto entero en pesos"
        return if (n < 0 || n > MAX_GASTO_CLP) "Debe estar entre $0 y $5.000.000" else null
    }

    fun validarMotivoRechazo(valor: String): String? = when {
        valor.isBlank() -> "Debes indicar el motivo del rechazo"
        valor.trim().length < 5 -> "Describe el motivo con al menos 5 caracteres"
        else -> null
    }
}
