package com.example.teqruta.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ReporteCostosTest {

    private val sede = Ubicacion(1, "Sede", "Concepción", "Dir 1", TipoUbicacion.SEDE)
    private val clinicaA = Ubicacion(2, "Clínica A", "Concepción", "Dir 2", TipoUbicacion.CLIENTE)
    private val clinicaB = Ubicacion(3, "Clínica B", "Chillán", "Dir 3", TipoUbicacion.CLIENTE)
    private val ubicaciones = listOf(sede, clinicaA, clinicaB)
    private val tecnico = Usuario(4, "Tecnico", "t@x.cl", Rol.TECNICO, true, "", "", jefeId = 9)
    private val vehiculo = Vehiculo(1, "BCDF12", "Camioneta", TipoVehiculo.EMPRESA, 100.0)

    private fun calculo(costo: Int, desviacion: Boolean = false) = CalculoCosto(
        distanciaOficialKm = 100.0, distanciaDeclaradaKm = 100.0, costoPorKmClp = 100.0,
        costoDistanciaClp = costo, peajeClp = 0, costoTotalClp = costo,
        desviacionPct = 0.0, hayDesviacion = desviacion, calculadoMs = 0
    )

    private fun traslado(id: Int, origen: Int, destino: Int, estado: EstadoTraslado, costo: Int, desv: Boolean = false) =
        Traslado(
            id = id, tecnicoId = 4, patente = "BCDF12", origenId = origen, destinoId = destino,
            inicioMs = 0, distanciaKm = 100.0, estado = estado, calculo = calculo(costo, desv)
        )

    @Test
    fun `el viaje de regreso se atribuye al cliente de origen`() {
        val ida = traslado(1, 1, 2, EstadoTraslado.VALIDADO, 1000)
        val vuelta = traslado(2, 2, 1, EstadoTraslado.VALIDADO, 1000)
        val filas = ReporteCostos.resumir(listOf(ida, vuelta), DimensionReporte.CLIENTE, listOf(tecnico), listOf(vehiculo), ubicaciones)
        assertEquals(1, filas.size)
        assertEquals("Clínica A", filas[0].etiqueta)
        assertEquals(2000, filas[0].costoTotalClp)
        assertEquals(2, filas[0].traslados)
    }

    @Test
    fun `solo cuentan los traslados validados`() {
        val lista = listOf(
            traslado(1, 1, 2, EstadoTraslado.VALIDADO, 1000),
            traslado(2, 1, 2, EstadoTraslado.PENDIENTE_VALIDACION, 5000),
            traslado(3, 1, 2, EstadoTraslado.RECHAZADO, 7000)
        )
        val filas = ReporteCostos.resumir(lista, DimensionReporte.TECNICO, listOf(tecnico), listOf(vehiculo), ubicaciones)
        assertEquals(1000, filas.single().costoTotalClp)
    }

    @Test
    fun `agrupa por cliente y ordena de mayor a menor costo`() {
        val lista = listOf(
            traslado(1, 1, 2, EstadoTraslado.VALIDADO, 1000),
            traslado(2, 1, 3, EstadoTraslado.VALIDADO, 9000, desv = true)
        )
        val filas = ReporteCostos.resumir(lista, DimensionReporte.CLIENTE, listOf(tecnico), listOf(vehiculo), ubicaciones)
        assertEquals(listOf("Clínica B", "Clínica A"), filas.map { it.etiqueta })
        assertEquals(1, filas[0].conDesviacion)
    }

    @Test
    fun `agrupa por vehiculo`() {
        val filas = ReporteCostos.resumir(
            listOf(traslado(1, 1, 2, EstadoTraslado.VALIDADO, 1000)),
            DimensionReporte.VEHICULO, listOf(tecnico), listOf(vehiculo), ubicaciones
        )
        assertEquals("BCDF12", filas.single().etiqueta)
    }
}
