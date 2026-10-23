package com.example.teqruta.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formatos de presentación con la convención chilena (punto para miles, coma decimal).
object Formato {
    private val locale = Locale("es", "CL")

    fun clp(monto: Int): String = "$" + NumberFormat.getIntegerInstance(locale).format(monto)

    // Kilómetros con sus decimales (hasta 2): 112,0 km · 12,35 km
    fun km(valor: Double): String {
        val formato = NumberFormat.getNumberInstance(locale)
        formato.minimumFractionDigits = 1
        formato.maximumFractionDigits = 2
        return formato.format(valor) + " km"
    }

    // Costo por kilómetro: $130,0/km
    fun clpPorKm(valor: Double): String = "$" + String.format(locale, "%.1f", valor) + "/km"

    fun porcentaje(valor: Double): String = String.format(locale, "%+.1f %%", valor)

    fun fechaHora(ms: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date(ms))

    fun hora(ms: Long): String = SimpleDateFormat("HH:mm", locale).format(Date(ms))
}
