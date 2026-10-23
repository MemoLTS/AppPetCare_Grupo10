package com.example.teqruta.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadoraCostosTest {

    // Tramo de 100 km con 2.000 de peaje y un vehículo que cuesta 125,5 por km
    private val tramo = Tramo(1, origenId = 1, destinoId = 2, distanciaKm = 100.0, peajeClp = 2_000)
    private val vehiculo = Vehiculo(1, "BCDF12", "Prueba", TipoVehiculo.EMPRESA, 125.5)

    private fun calcular(kmDeclarados: Double) =
        CalculadoraCostos.calcular(tramo, vehiculo, kmDeclarados, ahoraMs = 123L)

    @Test
    fun `el costo sale de la distancia oficial, no de la declarada`() {
        val a = calcular(100.0)
        val b = calcular(108.5)
        // 100 km * 125,5 = 12.550 + 2.000 de peaje
        assertEquals(12_550, a.costoDistanciaClp)
        assertEquals(14_550, a.costoTotalClp)
        assertEquals(a.costoTotalClp, b.costoTotalClp)
    }

    @Test
    fun `guarda los datos usados para poder auditar el calculo`() {
        val c = calcular(100.0)
        assertEquals(125.5, c.costoPorKmClp, 0.0)
        assertEquals(100.0, c.distanciaOficialKm, 0.0)
        assertEquals(123L, c.calculadoMs)
    }

    @Test
    fun `los km declarados admiten decimales`() {
        assertEquals(104.5, calcular(104.5).distanciaDeclaradaKm, 0.0)
        assertEquals(4.5, calcular(104.5).desviacionPct, 0.0001)
    }

    @Test
    fun `una diferencia dentro de la tolerancia no es desviacion`() {
        assertFalse(calcular(105.0).hayDesviacion)
        assertFalse(calcular(110.0).hayDesviacion)
    }

    @Test
    fun `declarar mas o menos km que el limite marca desviacion`() {
        assertTrue(calcular(115.0).hayDesviacion)
        assertTrue(calcular(85.0).hayDesviacion)
        assertEquals(15.0, calcular(115.0).desviacionPct, 0.0001)
        assertEquals(-15.0, calcular(85.0).desviacionPct, 0.0001)
    }
}
