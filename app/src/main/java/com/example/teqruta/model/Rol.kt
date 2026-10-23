package com.example.teqruta.model

// Acciones atómicas que se pueden permitir o negar. Cada rol es solo un conjunto de permisos,
// así que agregar un tipo de usuario nuevo es agregar una entrada en el enum Rol.
enum class Permiso {
    REGISTRAR_TRASLADOS,
    VER_TRASLADOS_PROPIOS,
    VER_TRASLADOS_EQUIPO,
    VER_TRASLADOS_TODOS,
    VALIDAR_TRASLADOS,
    VER_COSTOS_DETALLE,   // ver el costo de cada traslado
    VER_REPORTES,         // ver el costo consolidado por cliente, técnico y vehículo
    GESTIONAR_CATALOGOS,  // vehículos, ubicaciones, tramos
    GESTIONAR_USUARIOS
}

// Perfiles de usuario del caso TeqRuta y lo que puede hacer cada uno.
enum class Rol(
    val etiqueta: String,
    val descripcion: String,
    val permisos: Set<Permiso>
) {
    TECNICO(
        etiqueta = "Técnico de servicio",
        descripcion = "Registra el inicio y el fin de sus traslados y ve solo los suyos",
        permisos = setOf(Permiso.REGISTRAR_TRASLADOS, Permiso.VER_TRASLADOS_PROPIOS)
    ),
    JEFE_SERVICIO(
        etiqueta = "Jefe de servicio técnico",
        descripcion = "Valida o rechaza los traslados de su equipo y revisa las desviaciones",
        permisos = setOf(
            Permiso.VER_TRASLADOS_EQUIPO, Permiso.VALIDAR_TRASLADOS, Permiso.VER_COSTOS_DETALLE
        )
    ),
    GERENCIA_FINANZAS(
        etiqueta = "Gerencia / Finanzas",
        descripcion = "Consulta el costo real de traslado por cliente, técnico y vehículo (solo lectura)",
        permisos = setOf(
            Permiso.VER_TRASLADOS_TODOS, Permiso.VER_COSTOS_DETALLE, Permiso.VER_REPORTES
        )
    ),
    ADMINISTRADOR(
        etiqueta = "Administrador del sistema",
        descripcion = "Mantiene vehículos, ubicaciones, tramos y usuarios",
        permisos = setOf(Permiso.GESTIONAR_CATALOGOS, Permiso.GESTIONAR_USUARIOS)
    )
}
