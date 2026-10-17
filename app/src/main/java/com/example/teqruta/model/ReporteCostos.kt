package com.example.teqruta.model

// Una fila del reporte de costo de movilización
data class FilaResumen(
    val etiqueta: String,
    val traslados: Int,
    val kmOficiales: Double,
    val costoTotalClp: Int,
    val conDesviacion: Int
)

enum class DimensionReporte(val etiqueta: String) {
    CLIENTE("Cliente"),
    TECNICO("Técnico"),
    VEHICULO("Vehículo")
}

// Reportes de gasto de movilización. Solo cuentan traslados VALIDADOS por el jefe de servicio,
// que son los que la empresa da por buenos y podría respaldar ante un contrato.
object ReporteCostos {

    // Cliente al que se atribuye un traslado: el cliente de destino; si el destino es la sede
    // (viaje de regreso), el cliente de origen. Un tramo sin clientes es costo interno.
    fun clienteDe(t: Traslado, ubicaciones: Map<Int, Ubicacion>): Ubicacion? {
        val destino = ubicaciones[t.destinoId]
        if (destino?.tipo == TipoUbicacion.CLIENTE) return destino
        val origen = ubicaciones[t.origenId]
        return if (origen?.tipo == TipoUbicacion.CLIENTE) origen else null
    }

    fun resumir(
        traslados: List<Traslado>,
        dimension: DimensionReporte,
        usuarios: List<Usuario>,
        vehiculos: List<Vehiculo>,
        ubicaciones: List<Ubicacion>
    ): List<FilaResumen> {
        val usuariosPorId = usuarios.associateBy { it.id }
        val vehiculosPorId = vehiculos.associateBy { it.id }
        val ubicacionesPorId = ubicaciones.associateBy { it.id }

        return traslados
            .filter { it.estado == EstadoTraslado.VALIDADO && it.calculo != null }
            .groupBy { t ->
                when (dimension) {
                    DimensionReporte.CLIENTE ->
                        clienteDe(t, ubicacionesPorId)?.nombre ?: "Sin cliente (traslado interno)"
                    DimensionReporte.TECNICO -> usuariosPorId[t.tecnicoId]?.nombre ?: "Usuario desconocido"
                    DimensionReporte.VEHICULO -> vehiculosPorId[t.vehiculoId]?.patente ?: "Vehículo desconocido"
                }
            }
            .map { (etiqueta, lista) ->
                FilaResumen(
                    etiqueta = etiqueta,
                    traslados = lista.size,
                    kmOficiales = lista.sumOf { it.calculo!!.distanciaOficialKm },
                    costoTotalClp = lista.sumOf { it.calculo!!.costoTotalClp },
                    conDesviacion = lista.count { it.hayDesviacion }
                )
            }
            .sortedByDescending { it.costoTotalClp }
    }
}
