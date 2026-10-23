package com.example.teqruta.viewmodel

import androidx.lifecycle.ViewModel
import com.example.teqruta.data.AppContainer
import com.example.teqruta.data.RepositorioDatos
import com.example.teqruta.model.PoliticaAcceso
import com.example.teqruta.model.Tramo
import com.example.teqruta.model.TramoForm
import com.example.teqruta.model.Ubicacion
import com.example.teqruta.model.UbicacionForm
import com.example.teqruta.model.Usuario
import com.example.teqruta.model.Vehiculo
import com.example.teqruta.model.VehiculoForm
import com.example.teqruta.model.ValidadorCatalogo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// ViewModel del administrador: mantiene vehículos, ubicaciones y tramos.
// Cada formulario se abre en un diálogo: su estado es null mientras está cerrado.
// Todas las operaciones verifican el permiso GESTIONAR_CATALOGOS.
class CatalogoViewModel(
    private val repo: RepositorioDatos = AppContainer.repositorio,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    val vehiculos: StateFlow<List<Vehiculo>> = repo.vehiculos
    val ubicaciones: StateFlow<List<Ubicacion>> = repo.ubicaciones
    val tramos: StateFlow<List<Tramo>> = repo.tramos
    val usuarios: StateFlow<List<Usuario>> = repo.usuarios

    // ---------- Vehículos ----------

    private val _vehiculoForm = MutableStateFlow<VehiculoForm?>(null)
    val vehiculoForm: StateFlow<VehiculoForm?> = _vehiculoForm.asStateFlow()

    fun nuevoVehiculo(actor: Usuario) {
        if (PoliticaAcceso.puedeGestionarCatalogos(actor)) _vehiculoForm.value = VehiculoForm()
    }

    fun editarVehiculo(actor: Usuario, id: Int) {
        if (!PoliticaAcceso.puedeGestionarCatalogos(actor)) return
        val v = repo.vehiculos.value.firstOrNull { it.id == id } ?: return
        _vehiculoForm.value = VehiculoForm(
            id = v.id, patente = v.patente, descripcion = v.descripcion, tipo = v.tipo,
            costoPorKm = v.costoPorKmClp.toString(), activo = v.activo
        )
    }

    // La patente se convierte a mayúsculas (y se limpia de espacios y guiones) al escribirla.
    fun onPatenteVehiculoChange(valor: String) {
        cambiarVehiculo("patente") { it.copy(patente = ValidadorCatalogo.patenteDeEntrada(valor)) }
    }

    fun cambiarVehiculo(campo: String, cambio: (VehiculoForm) -> VehiculoForm) {
        _vehiculoForm.update { f -> f?.let { cambio(it).copy(errores = it.errores - campo) } }
    }

    fun cerrarVehiculo() { _vehiculoForm.value = null }

    fun guardarVehiculo(actor: Usuario): Boolean {
        if (!PoliticaAcceso.puedeGestionarCatalogos(actor)) return false
        val f = _vehiculoForm.value ?: return false
        val patente = ValidadorCatalogo.normalizarPatente(f.patente)

        val errores = buildMap<String, String> {
            ValidadorCatalogo.validarPatente(f.patente)?.let { put("patente", it) }
            if (!containsKey("patente") &&
                repo.vehiculos.value.any { it.id != f.id && it.patente == patente }
            ) {
                put("patente", "Ya existe un vehículo con esta patente")
            }
            // Los traslados referencian al vehículo por su patente: no se cambia si ya tiene traslados
            val anterior = repo.vehiculos.value.firstOrNull { it.id == f.id }
            if (!containsKey("patente") && anterior != null && anterior.patente != patente &&
                repo.traslados.value.any { it.patente == anterior.patente }
            ) {
                put("patente", "No se puede cambiar la patente: el vehículo ya tiene traslados registrados")
            }
            ValidadorCatalogo.validarDescripcion(f.descripcion)?.let { put("descripcion", it) }
            ValidadorCatalogo.validarCostoPorKm(f.costoPorKm)?.let { put("costoPorKm", it) }
        }
        if (errores.isNotEmpty()) {
            _vehiculoForm.update { it?.copy(errores = errores) }
            return false
        }
        repo.guardarVehiculo(
            Vehiculo(
                id = f.id,
                patente = patente,
                descripcion = f.descripcion.trim(),
                tipo = f.tipo,
                costoPorKmClp = ValidadorCatalogo.parsearDecimal(f.costoPorKm)!!,
                activo = f.activo
            )
        )
        _vehiculoForm.value = null
        return true
    }

    // ---------- Ubicaciones (sede y clientes) ----------

    private val _ubicacionForm = MutableStateFlow<UbicacionForm?>(null)
    val ubicacionForm: StateFlow<UbicacionForm?> = _ubicacionForm.asStateFlow()

    fun nuevaUbicacion(actor: Usuario) {
        if (PoliticaAcceso.puedeGestionarCatalogos(actor)) _ubicacionForm.value = UbicacionForm()
    }

    fun editarUbicacion(actor: Usuario, id: Int) {
        if (!PoliticaAcceso.puedeGestionarCatalogos(actor)) return
        val u = repo.ubicaciones.value.firstOrNull { it.id == id } ?: return
        _ubicacionForm.value = UbicacionForm(
            id = u.id, nombre = u.nombre, ciudad = u.ciudad, direccion = u.direccion,
            tipo = u.tipo, activa = u.activa
        )
    }

    fun cambiarUbicacion(campo: String, cambio: (UbicacionForm) -> UbicacionForm) {
        _ubicacionForm.update { f -> f?.let { cambio(it).copy(errores = it.errores - campo) } }
    }

    fun cerrarUbicacion() { _ubicacionForm.value = null }

    fun guardarUbicacion(actor: Usuario): Boolean {
        if (!PoliticaAcceso.puedeGestionarCatalogos(actor)) return false
        val f = _ubicacionForm.value ?: return false

        val errores = buildMap<String, String> {
            ValidadorCatalogo.validarNombreUbicacion(f.nombre)?.let { put("nombre", it) }
            if (!containsKey("nombre") && repo.ubicaciones.value.any {
                    it.id != f.id && it.nombre.equals(f.nombre.trim(), ignoreCase = true)
                }
            ) {
                put("nombre", "Ya existe una ubicación con este nombre")
            }
            ValidadorCatalogo.validarCiudad(f.ciudad)?.let { put("ciudad", it) }
            ValidadorCatalogo.validarDireccion(f.direccion)?.let { put("direccion", it) }
        }
        if (errores.isNotEmpty()) {
            _ubicacionForm.update { it?.copy(errores = errores) }
            return false
        }
        repo.guardarUbicacion(
            Ubicacion(
                id = f.id, nombre = f.nombre.trim(), ciudad = f.ciudad.trim(),
                direccion = f.direccion.trim(), tipo = f.tipo, activa = f.activa
            )
        )
        _ubicacionForm.value = null
        return true
    }

    // ---------- Tramos (distancia y peaje entre dos ubicaciones) ----------

    private val _tramoForm = MutableStateFlow<TramoForm?>(null)
    val tramoForm: StateFlow<TramoForm?> = _tramoForm.asStateFlow()

    fun nuevoTramo(actor: Usuario) {
        if (PoliticaAcceso.puedeGestionarCatalogos(actor)) _tramoForm.value = TramoForm()
    }

    fun editarTramo(actor: Usuario, id: Int) {
        if (!PoliticaAcceso.puedeGestionarCatalogos(actor)) return
        val t = repo.tramos.value.firstOrNull { it.id == id } ?: return
        _tramoForm.value = TramoForm(
            id = t.id, origenId = t.origenId, destinoId = t.destinoId,
            distanciaKm = t.distanciaKm.toString(), peajeClp = t.peajeClp.toString(), activo = t.activo
        )
    }

    fun cambiarTramo(campo: String, cambio: (TramoForm) -> TramoForm) {
        _tramoForm.update { f -> f?.let { cambio(it).copy(errores = it.errores - campo) } }
    }

    fun cerrarTramo() { _tramoForm.value = null }

    fun guardarTramo(actor: Usuario): Boolean {
        if (!PoliticaAcceso.puedeGestionarCatalogos(actor)) return false
        val f = _tramoForm.value ?: return false

        val errores = buildMap<String, String> {
            if (f.origenId == null) put("origen", "Selecciona el origen")
            if (f.destinoId == null) put("destino", "Selecciona el destino")
            if (f.origenId != null && f.destinoId != null) {
                if (f.origenId == f.destinoId) {
                    put("destino", "El destino debe ser distinto del origen")
                } else if (repo.tramos.value.any { it.id != f.id && it.conecta(f.origenId, f.destinoId) }) {
                    put("destino", "Ya existe un tramo entre estas dos ubicaciones")
                }
            }
            ValidadorCatalogo.validarDistancia(f.distanciaKm)?.let { put("distancia", it) }
            ValidadorCatalogo.validarPeaje(f.peajeClp)?.let { put("peaje", it) }
        }
        if (errores.isNotEmpty()) {
            _tramoForm.update { it?.copy(errores = errores) }
            return false
        }
        repo.guardarTramo(
            Tramo(
                id = f.id,
                origenId = f.origenId!!,
                destinoId = f.destinoId!!,
                distanciaKm = ValidadorCatalogo.parsearDecimal(f.distanciaKm)!!,
                peajeClp = f.peajeClp.trim().toInt(),
                activo = f.activo
            )
        )
        _tramoForm.value = null
        return true
    }

    // Deja todos los formularios cerrados (por ejemplo al cerrar sesión)
    fun cerrarTodo() {
        _vehiculoForm.value = null
        _ubicacionForm.value = null
        _tramoForm.value = null
    }
}
