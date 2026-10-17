package com.example.teqruta.model

import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

// Hash de contraseñas con PBKDF2-HMAC-SHA256 y una sal aleatoria por usuario.
object HashContrasena {

    // Número de iteraciones moderado para no bloquear la UI mientras los datos viven en memoria.
    // Al agregar persistencia conviene subirlo y ejecutar el hash fuera del hilo principal.
    private const val ITERACIONES = 20_000
    private const val LONGITUD_BITS = 256

    fun generarSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return bytes.aHex()
    }

    fun hash(clave: String, salt: String): String {
        val spec = PBEKeySpec(clave.toCharArray(), salt.toByteArray(Charsets.UTF_8), ITERACIONES, LONGITUD_BITS)
        try {
            val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
            return bytes.aHex()
        } finally {
            spec.clearPassword()
        }
    }

    // Comparación en tiempo constante para no filtrar información por tiempos de respuesta.
    fun verificar(clave: String, salt: String, hashEsperado: String): Boolean =
        MessageDigest.isEqual(
            hash(clave, salt).toByteArray(Charsets.UTF_8),
            hashEsperado.toByteArray(Charsets.UTF_8)
        )

    private fun ByteArray.aHex(): String =
        joinToString("") { "%02x".format(it.toInt() and 0xFF) }
}
