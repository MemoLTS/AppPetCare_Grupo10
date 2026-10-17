package com.example.teqruta.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculadoraCostosTest {

    // Tramo de 100 km con 2.000 de peaje, vehículo de 10 km/L y combustible a 1.000 por litro
    private val tramo = Tramo(1, origenId = 1, destinoId = 2, distanciaKm = 100.0, peajeClp = 2_000)
    private val tarifa = TarifaCombustible(7, 1_000, 0L, "prueba", null)
    private val vehiculo = Vehiculo(1, "BCDF12", "Prueba", TipoVehiculo.EMPRESA, 10.0)

    private fun calcular(kmDeclarados: Int) =
        CalculadoraCostos.calcular(tramo, tarifa, vehiculo, kmDeclarados, ahoraMs = 123L)

    @Test
    fun `el costo sale de la distancia oficial, no de la declarada`() {
        val a = calcular(100)
        val b = calcular(108)
        // 100 km / 10 km por litro = 10 litros * 1.000 = 10.000 + 2.000 de peaje
        assertEquals(10_000, a.costoCombustibleClp)
        assertEquals(12_000, a.costoTotalClp)
        assertEquals(a.costoTotalClp, b.costoTotalClp)
    }

    @Test
    fun `guarda los datos usados para poder auditar el calculo`() {
        val c = calcular(100)
        assertEquals(7, c.tarifaId)
        assertEquals(1_000, c.precioLitroClp)
        assertEquals(10.0, c.rendimientoKmPorLitro, 0.0)
        assertEquals(10.0, c.litros, 0.0001)
        assertEquals(123L, c.calculadoMs)
    }

    @Test
    fun `una diferencia dentro de la tolerancia no es desviacion`() {
        assertFalse(calcular(105).hayDesviacion)
        assertFalse(calcular(110).hayDesviacion)
    }

    @Test
    fun `declarar mas o menos km que el limite marca desviacion`() {
        assertTrue(calcular(115).hayDesviacion)
        assertTrue(calcular(85).hayDesviacion)
        assertEquals(15.0, calcular(115).desviacionPct, 0.0001)
        assertEquals(-15.0, calcular(85).desviacionPct, 0.0001)
    }
}
