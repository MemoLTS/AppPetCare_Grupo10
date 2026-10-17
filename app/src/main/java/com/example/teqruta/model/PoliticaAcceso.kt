package com.example.teqruta.model

// Única fuente de verdad sobre "quién puede hacer qué". Son funciones puras (sin UI),
// usadas tanto por las pantallas (para mostrar u ocultar opciones) como por los ViewModels
// (para rechazar la operación aunque la UI fallara en ocultarla).
object PoliticaAcceso {

    // Un usuario desactivado no tiene ningún permiso.
    private fun permisos(usuario: Usuario): Set<Permiso> =
        if (usuario.activo) usuario.rol.permisos else emptySet()

    fun puedeRegistrarTraslados(u: Usuario) = Permiso.REGISTRAR_TRASLADOS in permisos(u)
    fun puedeVerTraslados(u: Usuario): Boolean {
        val p = permisos(u)
        return Permiso.VER_TRASLADOS_PROPIOS in p || Permiso.VER_TRASLADOS_EQUIPO in p ||
            Permiso.VER_TRASLADOS_TODOS in p
    }
    fun puedeVerCostos(u: Usuario) = Permiso.VER_COSTOS_DETALLE in permisos(u)
    fun puedeVerReportes(u: Usuario) = Permiso.VER_REPORTES in permisos(u)
    fun puedeValidar(u: Usuario) = Permiso.VALIDAR_TRASLADOS in permisos(u)
    fun puedeGestionarCatalogos(u: Usuario) = Permiso.GESTIONAR_CATALOGOS in permisos(u)
    fun puedeGestionarUsuarios(u: Usuario) = Permiso.GESTIONAR_USUARIOS in permisos(u)

    // "tecnico" es el dueño del traslado; se necesita para saber si pertenece al equipo del jefe.
    fun puedeVerTraslado(actor: Usuario, traslado: Traslado, tecnico: Usuario?): Boolean {
        val p = permisos(actor)
        return Permiso.VER_TRASLADOS_TODOS in p ||
            (Permiso.VER_TRASLADOS_PROPIOS in p && traslado.tecnicoId == actor.id) ||
            (Permiso.VER_TRASLADOS_EQUIPO in p && tecnico?.jefeId == actor.id)
    }

    fun filtrarVisibles(actor: Usuario, traslados: List<Traslado>, usuarios: List<Usuario>): List<Traslado> {
        val porId = usuarios.associateBy { it.id }
        return traslados.filter { puedeVerTraslado(actor, it, porId[it.tecnicoId]) }
    }

    // Solo el jefe del técnico puede validar, y solo traslados ya finalizados y pendientes.
    fun puedeValidarTraslado(actor: Usuario, traslado: Traslado, tecnico: Usuario?): Boolean =
        puedeValidar(actor) &&
            traslado.estado == EstadoTraslado.PENDIENTE_VALIDACION &&
            tecnico?.jefeId == actor.id

    // Solo el propio técnico finaliza su traslado en curso.
    fun puedeFinalizarTraslado(actor: Usuario, traslado: Traslado): Boolean =
        puedeRegistrarTraslados(actor) &&
            traslado.estado == EstadoTraslado.EN_CURSO &&
            traslado.tecnicoId == actor.id
}
