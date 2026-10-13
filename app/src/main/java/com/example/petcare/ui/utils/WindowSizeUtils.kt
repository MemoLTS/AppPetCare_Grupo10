package com.example.petcare.ui.utils

import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass

// Tipos de pantalla que usa la app para adaptar su diseño
enum class TipoPantalla { COMPACTA, MEDIANA, EXPANDIDA }

// Convierte el WindowWidthSizeClass de Material 3 a nuestro tipo reutilizable
fun WindowWidthSizeClass.toTipoPantalla(): TipoPantalla = when (this) {
    WindowWidthSizeClass.Compact -> TipoPantalla.COMPACTA
    WindowWidthSizeClass.Medium -> TipoPantalla.MEDIANA
    else -> TipoPantalla.EXPANDIDA
}
