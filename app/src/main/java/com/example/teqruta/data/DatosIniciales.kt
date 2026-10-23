package com.example.teqruta.data

import com.example.teqruta.model.HashContrasena
import com.example.teqruta.model.Rol
import com.example.teqruta.model.TipoUbicacion
import com.example.teqruta.model.TipoVehiculo
import com.example.teqruta.model.Tramo
import com.example.teqruta.model.Ubicacion
import com.example.teqruta.model.Usuario
import com.example.teqruta.model.Vehiculo

// Datos de ejemplo para poder probar la app. Distancias, peajes, costo por km de los vehículos y
// nombres de clientes son ilustrativos: el administrador debe reemplazarlos por los reales.
object DatosIniciales {

    fun usuarios(): List<Usuario> = listOf(
        usuario(1, "Administrador TeqMed", "admin@teqmed.cl", "Admin123", Rol.ADMINISTRADOR),
        usuario(2, "Jefa Servicio Uno", "jefe1@teqmed.cl", "Jefe1234", Rol.JEFE_SERVICIO),
        usuario(3, "Jefe Servicio Dos", "jefe2@teqmed.cl", "Jefe1234", Rol.JEFE_SERVICIO),
        usuario(4, "Tecnico Uno", "tecnico1@teqmed.cl", "Tecnico123", Rol.TECNICO, jefeId = 2),
        usuario(5, "Tecnico Dos", "tecnico2@teqmed.cl", "Tecnico123", Rol.TECNICO, jefeId = 2),
        usuario(6, "Tecnico Tres", "tecnico3@teqmed.cl", "Tecnico123", Rol.TECNICO, jefeId = 3),
        usuario(7, "Gerencia Finanzas", "gerencia@teqmed.cl", "Gerencia123", Rol.GERENCIA_FINANZAS)
    )

    private fun usuario(
        id: Int, nombre: String, correo: String, clave: String, rol: Rol,
        jefeId: Int? = null
    ): Usuario {
        val salt = HashContrasena.generarSalt()
        return Usuario(
            id = id, nombre = nombre, correo = correo, rol = rol, activo = true,
            salt = salt, hashClave = HashContrasena.hash(clave, salt),
            jefeId = jefeId
        )
    }

    fun vehiculos(): List<Vehiculo> = listOf(
        Vehiculo(1, "BCDF12", "Camioneta de servicio", TipoVehiculo.EMPRESA, 150.0),
        Vehiculo(2, "GHJK34", "Auto del técnico", TipoVehiculo.PARTICULAR, 110.0),
        Vehiculo(3, "LPRS56", "Furgón de servicio", TipoVehiculo.EMPRESA, 170.0)
    )

    fun ubicaciones(): List<Ubicacion> = listOf(
        Ubicacion(1, "Sede TeqMed Concepción", "Concepción", "Dirección de la sede", TipoUbicacion.SEDE),
        Ubicacion(2, "Clínica Ejemplo Concepción", "Concepción", "Dirección de ejemplo 100", TipoUbicacion.CLIENTE),
        Ubicacion(3, "Centro de Diálisis Ejemplo Chillán", "Chillán", "Dirección de ejemplo 200", TipoUbicacion.CLIENTE),
        Ubicacion(4, "Hospital Ejemplo Los Ángeles", "Los Ángeles", "Dirección de ejemplo 300", TipoUbicacion.CLIENTE),
        Ubicacion(5, "Clínica Ejemplo Temuco", "Temuco", "Dirección de ejemplo 400", TipoUbicacion.CLIENTE)
    )

    fun tramos(): List<Tramo> = listOf(
        Tramo(1, origenId = 1, destinoId = 2, distanciaKm = 8.0, peajeClp = 0),
        Tramo(2, origenId = 1, destinoId = 3, distanciaKm = 112.0, peajeClp = 3_200),
        Tramo(3, origenId = 1, destinoId = 4, distanciaKm = 110.0, peajeClp = 3_000),
        Tramo(4, origenId = 1, destinoId = 5, distanciaKm = 285.0, peajeClp = 9_000),
        Tramo(5, origenId = 2, destinoId = 3, distanciaKm = 118.0, peajeClp = 3_200),
        Tramo(6, origenId = 3, destinoId = 4, distanciaKm = 100.0, peajeClp = 2_500)
    )
}
