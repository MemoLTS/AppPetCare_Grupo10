package com.example.teqruta.model

import kotlin.math.abs
import kotlin.math.roundToInt

// Único lugar donde se calcula el costo de un traslado. El técnico solo declara los kilómetros
// recorridos (con decimales); el monto sale de datos oficiales (distancia del tramo y costo por
// km del vehículo), por lo que no depende de estimaciones manuales. Es una función pura.
//
// Fórmula: costo por distancia = distancia oficial del tramo * costo por km del vehículo
//          costo total = costo por distancia + peajes del tramo
//          desviación = (km declarados - distancia oficial) / distancia oficial
//
// El gasto del vehículo que el técnico declara (opcional) se guarda aparte y no altera este monto.
object CalculadoraCostos {

    // Diferencia máxima tolerada entre la distancia declarada y la oficial (10 %)
    const val TOLERANCIA_DESVIACION = 0.10

    fun calcular(
        tramo: Tramo,
        vehiculo: Vehiculo,
        distanciaDeclaradaKm: Double,
        ahoraMs: Long
    ): CalculoCosto {
        val costoDistancia = (tramo.distanciaKm * vehiculo.costoPorKmClp).roundToInt()
        val desviacion = (distanciaDeclaradaKm - tramo.distanciaKm) / tramo.distanciaKm
        return CalculoCosto(
            distanciaOficialKm = tramo.distanciaKm,
            distanciaDeclaradaKm = distanciaDeclaradaKm,
            costoPorKmClp = vehiculo.costoPorKmClp,
            costoDistanciaClp = costoDistancia,
            peajeClp = tramo.peajeClp,
            costoTotalClp = costoDistancia + tramo.peajeClp,
            desviacionPct = desviacion * 100,
            hayDesviacion = abs(desviacion) > TOLERANCIA_DESVIACION,
            calculadoMs = ahoraMs
        )
    }
}
