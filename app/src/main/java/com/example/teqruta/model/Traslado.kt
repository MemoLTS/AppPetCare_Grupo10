package com.example.teqruta.model

enum class EstadoTraslado(val etiqueta: String) {
    EN_CURSO("En curso"),
    PENDIENTE_VALIDACION("Por validar"),
    VALIDADO("Validado"),
    RECHAZADO("Rechazado")
}

// Registro auditable del cálculo de costo de un traslado: guarda los datos de entrada
// (distancia oficial, costo por km del vehículo, peaje) y los resultados, para poder reconstruir
// y verificar cómo se obtuvo el monto aunque después cambien el tramo o el costo por km.
data class CalculoCosto(
    val distanciaOficialKm: Double,
    // Distancia (km, con decimales) que declaró el técnico al finalizar
    val distanciaDeclaradaKm: Double,
    val costoPorKmClp: Double,
    // distancia oficial × costo por km
    val costoDistanciaClp: Int,
    val peajeClp: Int,
    val costoTotalClp: Int,
    // (declarada - oficial) / oficial. Positivo = el técnico declaró más km que el trayecto oficial
    val desviacionPct: Double,
    val hayDesviacion: Boolean,
    val calculadoMs: Long
)

// Un traslado es una orden de un tramo recorrido por un técnico (por ejemplo sede → clínica).
// El vehículo se asocia por su patente (los vehículos se registran aparte).
// distanciaKm es la distancia declarada por el técnico (con decimales) y gastoVehiculoClp es el
// gasto del vehículo en pesos, opcional.
data class Traslado(
    val id: Int,
    val tecnicoId: Int,
    val patente: String,
    val origenId: Int,
    val destinoId: Int,
    val inicioMs: Long,
    val finMs: Long? = null,
    val distanciaKm: Double? = null,
    val gastoVehiculoClp: Int? = null,
    val estado: EstadoTraslado = EstadoTraslado.EN_CURSO,
    val calculo: CalculoCosto? = null,
    val validadoPorId: Int? = null,
    val resueltoMs: Long? = null,
    val motivoRechazo: String? = null
) {
    val hayDesviacion: Boolean get() = calculo?.hayDesviacion == true
}
