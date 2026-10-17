package com.example.teqruta.model

enum class EstadoTraslado(val etiqueta: String) {
    EN_CURSO("En curso"),
    PENDIENTE_VALIDACION("Por validar"),
    VALIDADO("Validado"),
    RECHAZADO("Rechazado")
}

// Registro auditable del cálculo de costo de un traslado: guarda los datos de entrada
// (tarifa, distancia, rendimiento, peaje) y los resultados, para poder reconstruir y verificar
// cómo se obtuvo el monto aunque después cambien las tarifas.
data class CalculoCosto(
    val tarifaId: Int,
    val precioLitroClp: Int,
    val distanciaOficialKm: Double,
    val distanciaDeclaradaKm: Int,
    val rendimientoKmPorLitro: Double,
    val litros: Double,
    val costoCombustibleClp: Int,
    val peajeClp: Int,
    val costoTotalClp: Int,
    // (declarada - oficial) / oficial. Positivo = el técnico declaró más km que el trayecto oficial
    val desviacionPct: Double,
    val hayDesviacion: Boolean,
    val calculadoMs: Long
)

// Un traslado es un tramo recorrido por un técnico (por ejemplo sede → clínica).
// kmInicial y kmFinal son las lecturas del odómetro que declara el técnico.
data class Traslado(
    val id: Int,
    val tecnicoId: Int,
    val vehiculoId: Int,
    val origenId: Int,
    val destinoId: Int,
    val inicioMs: Long,
    val finMs: Long? = null,
    val kmInicial: Int,
    val kmFinal: Int? = null,
    val estado: EstadoTraslado = EstadoTraslado.EN_CURSO,
    val calculo: CalculoCosto? = null,
    val validadoPorId: Int? = null,
    val resueltoMs: Long? = null,
    val motivoRechazo: String? = null
) {
    val kmDeclarados: Int? get() = kmFinal?.let { it - kmInicial }
    val hayDesviacion: Boolean get() = calculo?.hayDesviacion == true
}
