package com.example.teqruta.model

import kotlin.math.abs
import kotlin.math.roundToInt

// Único lugar donde se calcula el costo de un traslado. El técnico solo declara el odómetro;
// el monto sale de datos oficiales (tramo, tarifa y rendimiento del vehículo), por lo que no
// depende de estimaciones manuales. Es una función pura: mismas entradas, mismo resultado.
//
// Fórmula: combustible = (distancia oficial / rendimiento) * precio por litro
//          costo total = combustible + peajes del tramo
//
// Cuando exista un servidor, esta lógica debe ejecutarse allá para que el cálculo quede
// realmente centralizado; la app solo enviará los datos declarados.
object CalculadoraCostos {

    // Diferencia máxima tolerada entre la distancia declarada y la oficial (10 %)
    const val TOLERANCIA_DESVIACION = 0.10

    fun calcular(
        tramo: Tramo,
        tarifa: TarifaCombustible,
        vehiculo: Vehiculo,
        kmDeclarados: Int,
        ahoraMs: Long
    ): CalculoCosto {
        val litros = tramo.distanciaKm / vehiculo.rendimientoKmPorLitro
        val combustible = (litros * tarifa.precioLitroClp).roundToInt()
        val desviacion = (kmDeclarados - tramo.distanciaKm) / tramo.distanciaKm
        return CalculoCosto(
            tarifaId = tarifa.id,
            precioLitroClp = tarifa.precioLitroClp,
            distanciaOficialKm = tramo.distanciaKm,
            distanciaDeclaradaKm = kmDeclarados,
            rendimientoKmPorLitro = vehiculo.rendimientoKmPorLitro,
            litros = litros,
            costoCombustibleClp = combustible,
            peajeClp = tramo.peajeClp,
            costoTotalClp = combustible + tramo.peajeClp,
            desviacionPct = desviacion * 100,
            hayDesviacion = abs(desviacion) > TOLERANCIA_DESVIACION,
            calculadoMs = ahoraMs
        )
    }
}
