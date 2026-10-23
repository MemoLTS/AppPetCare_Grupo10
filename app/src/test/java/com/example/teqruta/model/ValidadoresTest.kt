package com.example.teqruta.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class ValidadoresTest {

    @Test
    fun `la patente siempre se convierte a mayusculas al ingresar`() {
        assertEquals("BCDF12", ValidadorCatalogo.patenteDeEntrada("bcdf12"))
        assertEquals("BCDF12", ValidadorCatalogo.patenteDeEntrada("bc-df 12"))
        assertEquals("BCDF", ValidadorCatalogo.patenteDeEntrada("bcdf"))
    }

    @Test
    fun `la patente no pasa de 6 caracteres`() {
        assertEquals("BCDF12", ValidadorCatalogo.patenteDeEntrada("bcdf1234"))
    }

    @Test
    fun `los km aceptan decimales con coma o punto`() {
        assertNull(ValidadorTraslado.validarDistancia("112,5"))
        assertNull(ValidadorTraslado.validarDistancia("112.5"))
        assertEquals(112.5, ValidadorTraslado.parsearDistancia("112,5")!!, 0.0)
    }

    @Test
    fun `los km son obligatorios y positivos`() {
        assertNotNull(ValidadorTraslado.validarDistancia(""))
        assertNotNull(ValidadorTraslado.validarDistancia("0"))
        assertNotNull(ValidadorTraslado.validarDistancia("abc"))
        assertNotNull(ValidadorTraslado.validarDistancia("5000"))
    }

    @Test
    fun `el gasto del vehiculo es opcional`() {
        assertNull(ValidadorTraslado.validarGasto(""))
        assertNull(ValidadorTraslado.validarGasto("15000"))
        assertNotNull(ValidadorTraslado.validarGasto("12,5"))
        assertNotNull(ValidadorTraslado.validarGasto("-1"))
    }

    @Test
    fun `el costo por km admite decimales`() {
        assertNull(ValidadorCatalogo.validarCostoPorKm("125,5"))
        assertNotNull(ValidadorCatalogo.validarCostoPorKm("0"))
    }
}
