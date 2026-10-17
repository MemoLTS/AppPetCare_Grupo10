package com.example.teqruta.ui.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Formatos de presentación con la convención chilena (punto para miles, coma decimal).
object Formato {
    private val locale = Locale("es", "CL")

    fun clp(monto: Int): String = "$" + NumberFormat.getIntegerInstance(locale).format(monto)

    fun km(valor: Double): String = String.format(locale, "%.1f km", valor)

    fun porcentaje(valor: Double): String = String.format(locale, "%+.1f %%", valor)

    fun fechaHora(ms: Long): String = SimpleDateFormat("dd/MM/yyyy HH:mm", locale).format(Date(ms))

    fun hora(ms: Long): String = SimpleDateFormat("HH:mm", locale).format(Date(ms))
}
