package com.example.petcare.viewmodel

import androidx.lifecycle.ViewModel
import com.example.petcare.model.Mascota
import com.example.petcare.model.MascotaErrores
import com.example.petcare.model.MascotaUiState
import com.example.petcare.model.ValidadorMascota
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel compartido entre las pantallas de registro y lista.
class MascotaViewModel : ViewModel() {

    // Estado interno mutable del formulario
    private val _estado = MutableStateFlow(MascotaUiState())
    // Estado expuesto a la UI (solo lectura)
    val estado: StateFlow<MascotaUiState> = _estado.asStateFlow()

    // Lista de mascotas registradas
    private val _mascotas = MutableStateFlow<List<Mascota>>(emptyList())
    val mascotas: StateFlow<List<Mascota>> = _mascotas.asStateFlow()

    private var siguienteId = 1

    // Cada cambio de campo actualiza el valor y limpia su error
    fun onNombreChange(valor: String) {
        _estado.update { it.copy(nombre = valor, errores = it.errores.copy(nombre = null)) }
    }

    fun onEspecieChange(valor: String) {
        _estado.update { it.copy(especie = valor, errores = it.errores.copy(especie = null)) }
    }

    fun onEdadChange(valor: String) {
        _estado.update { it.copy(edad = valor, errores = it.errores.copy(edad = null)) }
    }

    fun onNombreDuenoChange(valor: String) {
        _estado.update { it.copy(nombreDueno = valor, errores = it.errores.copy(nombreDueno = null)) }
    }

    fun onCorreoChange(valor: String) {
        _estado.update { it.copy(correo = valor, errores = it.errores.copy(correo = null)) }
    }

    fun onTelefonoChange(valor: String) {
        _estado.update { it.copy(telefono = valor, errores = it.errores.copy(telefono = null)) }
    }

    fun onAceptaTerminosChange(valor: Boolean) {
        _estado.update { it.copy(aceptaTerminos = valor, errores = it.errores.copy(terminos = null)) }
    }

    // Valida todos los campos, actualiza los errores y retorna true si el formulario es válido
    fun validarFormulario(): Boolean {
        val actual = _estado.value
        val errores = MascotaErrores(
            nombre = ValidadorMascota.validarNombreMascota(actual.nombre),
            especie = ValidadorMascota.validarEspecie(actual.especie),
            edad = ValidadorMascota.validarEdad(actual.edad),
            nombreDueno = ValidadorMascota.validarNombreDueno(actual.nombreDueno),
            correo = ValidadorMascota.validarCorreo(actual.correo),
            telefono = ValidadorMascota.validarTelefono(actual.telefono),
            terminos = ValidadorMascota.validarTerminos(actual.aceptaTerminos)
        )
        _estado.update { it.copy(errores = errores) }
        return !errores.tieneErrores()
    }

    // Guarda la mascota con los datos del formulario y lo deja limpio para un nuevo registro
    fun guardarMascota() {
        val actual = _estado.value
        val nueva = Mascota(
            id = siguienteId++,
            nombre = actual.nombre.trim(),
            especie = actual.especie,
            edad = actual.edad.toInt(),
            nombreDueno = actual.nombreDueno.trim(),
            correo = actual.correo.trim(),
            telefono = actual.telefono
        )
        _mascotas.update { it + nueva }
        _estado.value = MascotaUiState()
    }

    fun eliminarMascota(id: Int) {
        _mascotas.update { lista -> lista.filter { it.id != id } }
    }
}
