package com.example.teqruta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqruta.data.AppContainer
import com.example.teqruta.data.RepositorioDatos
import com.example.teqruta.model.HashContrasena
import com.example.teqruta.model.LoginUiState
import com.example.teqruta.model.PoliticaAcceso
import com.example.teqruta.model.Rol
import com.example.teqruta.model.Usuario
import com.example.teqruta.model.UsuarioFormState
import com.example.teqruta.model.Vehiculo
import com.example.teqruta.model.ValidadorUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// ViewModel de usuarios: sesión activa, formulario de login y gestión de usuarios
// (esta última solo para quien tenga permiso).
class UsuarioViewModel(
    private val repo: RepositorioDatos = AppContainer.repositorio
) : ViewModel() {

    val usuarios: StateFlow<List<Usuario>> = repo.usuarios
    val vehiculos: StateFlow<List<Vehiculo>> = repo.vehiculos

    // ---------- Sesión ----------

    private val _idSesion = MutableStateFlow<Int?>(null)

    // Usuario con sesión iniciada. Se deriva de la lista, así que si su rol cambia o su cuenta
    // se desactiva, la sesión lo refleja de inmediato (y si se desactiva, la sesión se cierra).
    val usuarioActual: StateFlow<Usuario?> = combine(repo.usuarios, _idSesion) { lista, id ->
        lista.firstOrNull { it.id == id && it.activo }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _login = MutableStateFlow(LoginUiState())
    val login: StateFlow<LoginUiState> = _login.asStateFlow()

    fun onLoginCorreoChange(valor: String) {
        _login.update { it.copy(correo = valor, errorCorreo = null, errorGeneral = null) }
    }

    fun onLoginClaveChange(valor: String) {
        _login.update { it.copy(clave = valor, errorClave = null, errorGeneral = null) }
    }

    // Retorna true si las credenciales son correctas y la cuenta está activa.
    fun iniciarSesion(): Boolean {
        val actual = _login.value
        val errorCorreo = ValidadorUsuario.validarCorreo(actual.correo)
        val errorClave = if (actual.clave.isEmpty()) "La contraseña es obligatoria" else null
        if (errorCorreo != null || errorClave != null) {
            _login.update { it.copy(errorCorreo = errorCorreo, errorClave = errorClave) }
            return false
        }

        val usuario = repo.usuarios.value.firstOrNull {
            it.correo.equals(actual.correo.trim(), ignoreCase = true)
        }
        // Mismo mensaje para "no existe" y "clave incorrecta": no se revela qué correos están registrados.
        if (usuario == null || !HashContrasena.verificar(actual.clave, usuario.salt, usuario.hashClave)) {
            _login.update { it.copy(errorGeneral = "Correo o contraseña incorrectos") }
            return false
        }
        if (!usuario.activo) {
            _login.update {
                it.copy(errorGeneral = "Tu cuenta está desactivada. Contacta a un administrador.")
            }
            return false
        }

        _idSesion.value = usuario.id
        _login.value = LoginUiState()
        return true
    }

    fun cerrarSesion() {
        _idSesion.value = null
        _login.value = LoginUiState()
        _form.value = UsuarioFormState()
    }

    // ---------- Gestión de usuarios (solo administradores) ----------

    private val _form = MutableStateFlow(UsuarioFormState())
    val form: StateFlow<UsuarioFormState> = _form.asStateFlow()

    fun nuevoUsuario(actor: Usuario): Boolean {
        if (!PoliticaAcceso.puedeGestionarUsuarios(actor)) return false
        _form.value = UsuarioFormState()
        return true
    }

    fun editarUsuario(actor: Usuario, id: Int): Boolean {
        if (!PoliticaAcceso.puedeGestionarUsuarios(actor)) return false
        val u = repo.usuarios.value.firstOrNull { it.id == id } ?: return false
        _form.value = UsuarioFormState(
            editandoId = u.id,
            nombre = u.nombre,
            correo = u.correo,
            rol = u.rol,
            activo = u.activo,
            jefeId = u.jefeId,
            vehiculoId = u.vehiculoId
        )
        return true
    }

    // Cada cambio actualiza el valor y limpia el error de ese campo
    private fun cambiar(campo: String, cambio: (UsuarioFormState) -> UsuarioFormState) {
        _form.update { cambio(it).copy(errores = it.errores - campo) }
    }

    fun onNombreChange(valor: String) = cambiar("nombre") { it.copy(nombre = valor) }
    fun onCorreoChange(valor: String) = cambiar("correo") { it.copy(correo = valor) }
    fun onClaveChange(valor: String) = cambiar("clave") { it.copy(clave = valor) }
    fun onRolChange(valor: Rol) = cambiar("rol") { it.copy(rol = valor) }
    fun onJefeChange(id: Int) = cambiar("jefe") { it.copy(jefeId = id) }
    fun onVehiculoChange(id: Int) = cambiar("vehiculo") { it.copy(vehiculoId = id) }
    fun onActivoChange(valor: Boolean) = cambiar("rol") { it.copy(activo = valor) }

    // Valida todos los campos (incluye correo repetido y coherencia de jefe y vehículo).
    fun validarFormulario(): Boolean {
        val f = _form.value
        val lista = repo.usuarios.value
        val existente = lista.firstOrNull { it.id == f.editandoId }
        val correoRepetido = lista.any {
            it.id != f.editandoId && it.correo.equals(f.correo.trim(), ignoreCase = true)
        }

        val errores = buildMap<String, String> {
            ValidadorUsuario.validarNombre(f.nombre)?.let { put("nombre", it) }
            (ValidadorUsuario.validarCorreo(f.correo)
                ?: if (correoRepetido) "Ya existe un usuario con este correo" else null)
                ?.let { put("correo", it) }
            ValidadorUsuario.validarClave(f.clave, obligatoria = f.editandoId == null)
                ?.let { put("clave", it) }
            ValidadorUsuario.validarRol(f.rol)?.let { put("rol", it) }

            if (f.rol == Rol.TECNICO) {
                // Un técnico necesita jefe (quien valida sus traslados) y vehículo asignado.
                val jefe = lista.firstOrNull { it.id == f.jefeId }
                if (jefe == null || jefe.rol != Rol.JEFE_SERVICIO ||
                    (!jefe.activo && jefe.id != existente?.jefeId)
                ) {
                    put("jefe", "Selecciona el jefe de servicio del técnico")
                }
                val vehiculo = repo.vehiculos.value.firstOrNull { it.id == f.vehiculoId }
                if (vehiculo == null || (!vehiculo.activo && vehiculo.id != existente?.vehiculoId)) {
                    put("vehiculo", "Asigna un vehículo activo al técnico")
                }
            }

            // Un jefe no puede dejar de serlo (ni desactivarse) mientras tenga técnicos a cargo.
            if (existente != null && existente.rol == Rol.JEFE_SERVICIO &&
                (f.rol != Rol.JEFE_SERVICIO || !f.activo) &&
                lista.any { it.jefeId == existente.id && it.activo }
            ) {
                put("rol", "Reasigna primero a sus técnicos a otro jefe de servicio")
            }
        }
        _form.update { it.copy(errores = errores) }
        return errores.isEmpty()
    }

    // Crea o actualiza el usuario del formulario. Retorna false si el actor no tiene permiso.
    fun guardarUsuario(actor: Usuario): Boolean {
        if (!PoliticaAcceso.puedeGestionarUsuarios(actor)) return false
        val f = _form.value
        val rol = f.rol ?: return false
        val esTecnico = rol == Rol.TECNICO

        if (f.editandoId == null) {
            val salt = HashContrasena.generarSalt()
            repo.guardarUsuario(
                Usuario(
                    id = 0,
                    nombre = f.nombre.trim(),
                    correo = f.correo.trim().lowercase(),
                    rol = rol,
                    activo = true,
                    salt = salt,
                    hashClave = HashContrasena.hash(f.clave, salt),
                    jefeId = if (esTecnico) f.jefeId else null,
                    vehiculoId = if (esTecnico) f.vehiculoId else null
                )
            )
        } else {
            val existente = repo.usuarios.value.firstOrNull { it.id == f.editandoId } ?: return false
            // Para no quedarse sin acceso, nadie puede cambiar su propio rol ni desactivarse.
            val esUnoMismo = existente.id == actor.id
            val rolFinal = if (esUnoMismo) existente.rol else rol
            val (salt, hash) = if (f.clave.isEmpty()) {
                existente.salt to existente.hashClave
            } else {
                val nuevaSalt = HashContrasena.generarSalt()
                nuevaSalt to HashContrasena.hash(f.clave, nuevaSalt)
            }
            val tecnicoFinal = rolFinal == Rol.TECNICO
            repo.guardarUsuario(
                existente.copy(
                    nombre = f.nombre.trim(),
                    correo = f.correo.trim().lowercase(),
                    rol = rolFinal,
                    activo = if (esUnoMismo) true else f.activo,
                    salt = salt,
                    hashClave = hash,
                    jefeId = if (tecnicoFinal) f.jefeId else null,
                    vehiculoId = if (tecnicoFinal) f.vehiculoId else null
                )
            )
        }
        _form.value = UsuarioFormState()
        return true
    }
}
