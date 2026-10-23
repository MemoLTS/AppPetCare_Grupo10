package com.example.teqruta.model

// Estados de los formularios. Los campos numéricos se guardan como String para reflejar
// lo que el usuario escribe (incluso si es inválido). "errores" asocia el nombre de cada
// campo con su mensaje, así todos los formularios comparten el mismo mecanismo.

// ---------- Sesión y usuarios ----------

data class LoginUiState(
    val correo: String = "",
    val clave: String = "",
    val errorCorreo: String? = null,
    val errorClave: String? = null,
    // Error general (credenciales incorrectas o cuenta desactivada)
    val errorGeneral: String? = null
)

// editandoId == null significa "usuario nuevo".
data class UsuarioFormState(
    val editandoId: Int? = null,
    val nombre: String = "",
    val correo: String = "",
    val clave: String = "",
    val rol: Rol? = null,
    val activo: Boolean = true,
    val jefeId: Int? = null,
    val errores: Map<String, String> = emptyMap()
)

// ---------- Catálogos (formularios en diálogo; id = 0 es un registro nuevo) ----------

data class VehiculoForm(
    val id: Int = 0,
    val patente: String = "",
    val descripcion: String = "",
    val tipo: TipoVehiculo = TipoVehiculo.EMPRESA,
    val costoPorKm: String = "",
    val activo: Boolean = true,
    val errores: Map<String, String> = emptyMap()
)

data class UbicacionForm(
    val id: Int = 0,
    val nombre: String = "",
    val ciudad: String = "",
    val direccion: String = "",
    val tipo: TipoUbicacion = TipoUbicacion.CLIENTE,
    val activa: Boolean = true,
    val errores: Map<String, String> = emptyMap()
)

data class TramoForm(
    val id: Int = 0,
    val origenId: Int? = null,
    val destinoId: Int? = null,
    val distanciaKm: String = "",
    val peajeClp: String = "0",
    val activo: Boolean = true,
    val errores: Map<String, String> = emptyMap()
)

// ---------- Traslados ----------

// La orden se asocia al vehículo por su patente (se guarda siempre en mayúsculas).
data class NuevoTrasladoForm(
    val patente: String = "",
    val origenId: Int? = null,
    val destinoId: Int? = null,
    val errores: Map<String, String> = emptyMap()
)

// Distancia en km (con decimales, obligatoria) y gasto del vehículo en pesos (opcional).
data class FinalizarTrasladoForm(
    val distanciaKm: String = "",
    val gastoClp: String = "",
    val errores: Map<String, String> = emptyMap()
)

data class RechazoForm(
    val trasladoId: Int,
    val motivo: String = "",
    val error: String? = null
)
