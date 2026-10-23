package com.example.teqruta.model

// Entidades que mantiene el administrador. Ninguna se borra: se desactivan (activo = false)
// para que los traslados ya registrados conserven sus referencias y el historial sea auditable.
// Un id = 0 indica un registro nuevo que aún no se guardó.

enum class TipoVehiculo(val etiqueta: String) {
    EMPRESA("De la empresa"),
    PARTICULAR("Particular")
}

data class Vehiculo(
    val id: Int,
    val patente: String,
    val descripcion: String,
    val tipo: TipoVehiculo,
    // Costo del vehículo por kilómetro recorrido (CLP/km, admite decimales). Es la base del costo
    // de cada traslado: distancia oficial del tramo × costo por km.
    val costoPorKmClp: Double,
    val activo: Boolean = true
)

enum class TipoUbicacion(val etiqueta: String) {
    SEDE("Sede"),
    CLIENTE("Cliente (clínica / hospital / centro de diálisis)")
}

data class Ubicacion(
    val id: Int,
    val nombre: String,
    val ciudad: String,
    val direccion: String,
    val tipo: TipoUbicacion,
    val activa: Boolean = true
)

// Tramo oficial entre dos ubicaciones: distancia y peajes de referencia.
// Vale en ambos sentidos (A→B y B→A).
data class Tramo(
    val id: Int,
    val origenId: Int,
    val destinoId: Int,
    val distanciaKm: Double,
    val peajeClp: Int,
    val activo: Boolean = true
) {
    fun conecta(a: Int, b: Int): Boolean =
        (origenId == a && destinoId == b) || (origenId == b && destinoId == a)
}
