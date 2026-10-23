package com.example.teqruta.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PoliticaAccesoTest {

    private fun usuario(id: Int, rol: Rol, jefeId: Int? = null, activo: Boolean = true) =
        Usuario(id, "U$id", "u$id@x.cl", rol, activo, salt = "", hashClave = "", jefeId = jefeId)

    private val admin = usuario(1, Rol.ADMINISTRADOR)
    private val jefeA = usuario(2, Rol.JEFE_SERVICIO)
    private val jefeB = usuario(3, Rol.JEFE_SERVICIO)
    private val tecnicoA = usuario(4, Rol.TECNICO, jefeId = 2)
    private val tecnicoB = usuario(5, Rol.TECNICO, jefeId = 3)
    private val gerencia = usuario(6, Rol.GERENCIA_FINANZAS)
    private val todos = listOf(admin, jefeA, jefeB, tecnicoA, tecnicoB, gerencia)

    private fun traslado(id: Int, tecnico: Usuario, estado: EstadoTraslado) = Traslado(
        id = id, tecnicoId = tecnico.id, patente = "BCDF12", origenId = 1, destinoId = 2,
        inicioMs = 0, estado = estado
    )

    private val deA = traslado(1, tecnicoA, EstadoTraslado.PENDIENTE_VALIDACION)
    private val deB = traslado(2, tecnicoB, EstadoTraslado.PENDIENTE_VALIDACION)
    private val lista = listOf(deA, deB)

    @Test
    fun `el tecnico solo ve sus traslados`() {
        assertEquals(listOf(deA), PoliticaAcceso.filtrarVisibles(tecnicoA, lista, todos))
    }

    @Test
    fun `el jefe solo ve los traslados de su equipo`() {
        assertEquals(listOf(deA), PoliticaAcceso.filtrarVisibles(jefeA, lista, todos))
        assertEquals(listOf(deB), PoliticaAcceso.filtrarVisibles(jefeB, lista, todos))
    }

    @Test
    fun `gerencia ve todos y el administrador ninguno`() {
        assertEquals(lista, PoliticaAcceso.filtrarVisibles(gerencia, lista, todos))
        assertTrue(PoliticaAcceso.filtrarVisibles(admin, lista, todos).isEmpty())
    }

    @Test
    fun `solo el jefe del tecnico puede validar un traslado pendiente`() {
        assertTrue(PoliticaAcceso.puedeValidarTraslado(jefeA, deA, tecnicoA))
        assertFalse(PoliticaAcceso.puedeValidarTraslado(jefeB, deA, tecnicoA))
        assertFalse(PoliticaAcceso.puedeValidarTraslado(tecnicoA, deA, tecnicoA))
        assertFalse(PoliticaAcceso.puedeValidarTraslado(gerencia, deA, tecnicoA))
        val yaValidado = deA.copy(estado = EstadoTraslado.VALIDADO)
        assertFalse(PoliticaAcceso.puedeValidarTraslado(jefeA, yaValidado, tecnicoA))
    }

    @Test
    fun `solo el tecnico finaliza su propio traslado en curso`() {
        val enCurso = deA.copy(estado = EstadoTraslado.EN_CURSO)
        assertTrue(PoliticaAcceso.puedeFinalizarTraslado(tecnicoA, enCurso))
        assertFalse(PoliticaAcceso.puedeFinalizarTraslado(tecnicoB, enCurso))
        assertFalse(PoliticaAcceso.puedeFinalizarTraslado(tecnicoA, deA))
    }

    @Test
    fun `los costos y reportes dependen del rol`() {
        assertFalse(PoliticaAcceso.puedeVerCostos(tecnicoA))
        assertTrue(PoliticaAcceso.puedeVerCostos(jefeA))
        assertTrue(PoliticaAcceso.puedeVerReportes(gerencia))
        assertFalse(PoliticaAcceso.puedeVerReportes(jefeA))
    }

    @Test
    fun `solo el administrador gestiona catalogos y usuarios`() {
        assertTrue(PoliticaAcceso.puedeGestionarCatalogos(admin))
        assertTrue(PoliticaAcceso.puedeGestionarUsuarios(admin))
        listOf(tecnicoA, jefeA, gerencia).forEach {
            assertFalse(PoliticaAcceso.puedeGestionarCatalogos(it))
            assertFalse(PoliticaAcceso.puedeGestionarUsuarios(it))
        }
    }

    @Test
    fun `un usuario desactivado pierde todos sus permisos`() {
        val inactivo = tecnicoA.copy(activo = false)
        assertFalse(PoliticaAcceso.puedeRegistrarTraslados(inactivo))
        assertFalse(PoliticaAcceso.puedeVerTraslados(inactivo))
        assertTrue(PoliticaAcceso.filtrarVisibles(inactivo, lista, todos).isEmpty())
    }
}
