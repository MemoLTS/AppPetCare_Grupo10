package com.example.teqruta.model

// Validaciones del registro de traslados (funciones puras, sin UI).
object ValidadorTraslado {

    const val MAX_KM_POR_TRASLADO = 2_000
    private const val MAX_ODOMETRO = 9_999_999

    // minimoOdometro: último kilometraje conocido del vehículo (el odómetro no puede retroceder)
    fun validarKmInicial(valor: String, minimoOdometro: Int?): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa el kilometraje del odómetro (solo números)"
        return when {
            n < 0 || n > MAX_ODOMETRO -> "Kilometraje fuera de rango"
            minimoOdometro != null && n < minimoOdometro ->
                "No puede ser menor al último kilometraje registrado del vehículo ($minimoOdometro km)"
            else -> null
        }
    }

    fun validarKmFinal(valor: String, kmInicial: Int): String? {
        val n = valor.trim().toIntOrNull() ?: return "Ingresa el kilometraje del odómetro (solo números)"
        return when {
            n <= kmInicial -> "Debe ser mayor al kilometraje inicial ($kmInicial km)"
            n - kmInicial > MAX_KM_POR_TRASLADO ->
                "Un traslado no puede superar $MAX_KM_POR_TRASLADO km; revisa el odómetro"
            else -> null
        }
    }

    fun validarMotivoRechazo(valor: String): String? = when {
        valor.isBlank() -> "Debes indicar el motivo del rechazo"
        valor.trim().length < 5 -> "Describe el motivo con al menos 5 caracteres"
        else -> null
    }
}
