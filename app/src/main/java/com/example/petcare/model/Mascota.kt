package com.example.petcare.model

// Modelo de datos de una mascota ya registrada (lo que se muestra en la lista).
data class Mascota(
    val id: Int,
    val nombre: String,
    val especie: String,
    val edad: Int,
    val nombreDueno: String,
    val correo: String,
    val telefono: String
)
