package com.example.teqruta.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.teqruta.data.AppContainer
import com.example.teqruta.data.RepositorioDatos
import com.example.teqruta.model.FinalizarTrasladoForm
import com.example.teqruta.model.NuevoTrasladoForm
import com.example.teqruta.model.PoliticaAcceso
import com.example.teqruta.model.RechazoForm
import com.example.teqruta.model.TipoUbicacion
import com.example.teqruta.model.Tramo
import com.example.teqruta.model.Traslado
import com.example.teqruta.model.Ubicacion
import com.example.teqruta.model.Usuario
import com.example.teqruta.model.ValidadorCatalogo
import com.example.teqruta.model.ValidadorTraslado
import com.example.teqruta.model.Vehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

// Todo lo que las pantallas necesitan para mostrar traslados, con búsquedas por id ya armadas.
data class DatosTraslados(
    val traslados: List<Traslado>,
    val usuarios: List<Usuario>,
    val vehiculos: List<Vehiculo>,
    val ubicaciones: List<Ubicacion>,
    val tramos: List<Tramo>
) {
    val usuariosPorId: Map<Int, Usuario> = usuarios.associateBy { it.id }
    // El vehículo de un traslado se busca por su patente
    val vehiculosPorPatente: Map<String, Vehiculo> = vehiculos.associateBy { it.patente }
    val ubicacionesPorId: Map<Int, Ubicacion> = ubicaciones.associateBy { it.id }
}

// ViewModel de traslados: registro del técnico (iniciar y finalizar) y validación del jefe.
// Cada operación recibe al usuario que la ejecuta y verifica sus permisos con PoliticaAcceso;
// si no corresponde, no hace nada y retorna false.
class TrasladoViewModel(
    private val repo: RepositorioDatos = AppContainer.repositorio,
    private val reloj: () -> Long = { System.currentTimeMillis() }
) : ViewModel() {

    private fun armarDatos() = DatosTraslados(
        repo.traslados.value, repo.usuarios.value, repo.vehiculos.value,
        repo.ubicaciones.value, repo.tramos.value
    )

    val datos: StateFlow<DatosTraslados> = combine(
        repo.traslados, repo.usuarios, repo.vehiculos, repo.ubicaciones, repo.tramos
    ) { traslados, usuarios, vehiculos, ubicaciones, tramos ->
        DatosTraslados(traslados, usuarios, vehiculos, ubicaciones, tramos)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, armarDatos())

    // Tramo oficial entre dos ubicaciones (null si el administrador aún no lo registró)
    fun tramoOficial(origenId: Int?, destinoId: Int?): Tramo? =
        if (origenId == null || destinoId == null || origenId == destinoId) null
        else repo.tramoEntre(origenId, destinoId)

    // ---------- Iniciar traslado ----------

    private val _nuevo = MutableStateFlow(NuevoTrasladoForm())
    val nuevo: StateFlow<NuevoTrasladoForm> = _nuevo.asStateFlow()

    fun trasladoEnCurso(actor: Usuario): Traslado? = repo.trasladoEnCurso(actor.id)

    // Deja el formulario listo para una orden nueva, proponiendo la patente (la última que usó el
    // técnico) y el origen (su último destino, o la sede) para agilizar el registro.
    fun prepararNuevo(actor: Usuario) {
        val origenSugerido = repo.ultimoDestinoDe(actor.id)
            ?: repo.ubicaciones.value.firstOrNull { it.tipo == TipoUbicacion.SEDE && it.activa }?.id
        _nuevo.value = NuevoTrasladoForm(
            patente = repo.ultimaPatenteDe(actor.id) ?: "",
            origenId = origenSugerido
        )
    }

    // La patente se convierte a mayúsculas (y se limpia de espacios y guiones) al escribirla.
    fun onPatenteChange(valor: String) {
        _nuevo.update {
            it.copy(patente = ValidadorCatalogo.patenteDeEntrada(valor), errores = it.errores - "patente" - "general")
        }
    }

    fun onOrigenChange(id: Int) {
        _nuevo.update { it.copy(origenId = id, errores = it.errores - "origen" - "destino") }
    }

    fun onDestinoChange(id: Int) {
        _nuevo.update { it.copy(destinoId = id, errores = it.errores - "destino") }
    }

    // Valida el formulario y registra la orden asociada al vehículo por su patente.
    // Retorna true si quedó en curso.
    fun iniciarTraslado(actor: Usuario): Boolean {
        if (!PoliticaAcceso.puedeRegistrarTraslados(actor)) return false
        val f = _nuevo.value
        val errores = mutableMapOf<String, String>()

        if (repo.trasladoEnCurso(actor.id) != null) {
            errores["general"] = "Ya tienes un traslado en curso. Finalízalo antes de iniciar otro."
        }

        // El vehículo se identifica por la patente y debe estar registrado y activo
        val patente = ValidadorCatalogo.normalizarPatente(f.patente)
        val vehiculo = repo.vehiculoPorPatente(patente)
        val errorPatente = ValidadorCatalogo.validarPatente(f.patente)
            ?: when {
                vehiculo == null ->
                    "No hay un vehículo registrado con esta patente. Pide al administrador que lo registre."
                !vehiculo.activo -> "El vehículo con esta patente está desactivado."
                else -> null
            }
        if (errorPatente != null) errores["patente"] = errorPatente

        val origen = repo.ubicaciones.value.firstOrNull { it.id == f.origenId && it.activa }
        val destino = repo.ubicaciones.value.firstOrNull { it.id == f.destinoId && it.activa }
        if (origen == null) errores["origen"] = "Selecciona el origen"
        if (destino == null) errores["destino"] = "Selecciona el destino"
        if (origen != null && destino != null) {
            if (origen.id == destino.id) {
                errores["destino"] = "El destino debe ser distinto del origen"
            } else if (repo.tramoEntre(origen.id, destino.id) == null) {
                errores["destino"] = "No hay un tramo oficial registrado entre estas ubicaciones. Avisa al administrador."
            }
        }

        if (errores.isNotEmpty() || vehiculo == null || origen == null || destino == null) {
            _nuevo.update { it.copy(errores = errores) }
            return false
        }
        repo.iniciarTraslado(
            tecnicoId = actor.id,
            patente = vehiculo.patente,
            origenId = origen.id,
            destinoId = destino.id,
            ahoraMs = reloj()
        )
        _nuevo.value = NuevoTrasladoForm()
        return true
    }

    // ---------- Finalizar traslado ----------

    private val _finalizar = MutableStateFlow(FinalizarTrasladoForm())
    val finalizar: StateFlow<FinalizarTrasladoForm> = _finalizar.asStateFlow()

    fun prepararFinalizar() {
        _finalizar.value = FinalizarTrasladoForm()
    }

    fun onDistanciaChange(valor: String) {
        _finalizar.update { it.copy(distanciaKm = valor, errores = it.errores - "distancia" - "general") }
    }

    fun onGastoChange(valor: String) {
        _finalizar.update { it.copy(gastoClp = valor, errores = it.errores - "gasto") }
    }

    // Cierra el traslado en curso del técnico y calcula su costo. La distancia (km, con decimales)
    // es obligatoria; el gasto del vehículo en pesos es opcional.
    // Retorna true si quedó pendiente de validación.
    fun finalizarTraslado(actor: Usuario): Boolean {
        val enCurso = repo.trasladoEnCurso(actor.id) ?: return false
        if (!PoliticaAcceso.puedeFinalizarTraslado(actor, enCurso)) return false

        val f = _finalizar.value
        val errores = buildMap<String, String> {
            ValidadorTraslado.validarDistancia(f.distanciaKm)?.let { put("distancia", it) }
            ValidadorTraslado.validarGasto(f.gastoClp)?.let { put("gasto", it) }
        }
        if (errores.isNotEmpty()) {
            _finalizar.update { it.copy(errores = errores) }
            return false
        }
        val gasto = if (f.gastoClp.isBlank()) null else ValidadorTraslado.parsearGasto(f.gastoClp)
        val resultado = repo.finalizarTraslado(
            id = enCurso.id,
            distanciaKm = ValidadorTraslado.parsearDistancia(f.distanciaKm)!!,
            gastoVehiculoClp = gasto,
            ahoraMs = reloj()
        )
        if (resultado == null) {
            _finalizar.update {
                it.copy(errores = mapOf("general" to "No se pudo calcular el costo: falta el tramo oficial o el vehículo. Avisa al administrador."))
            }
            return false
        }
        _finalizar.value = FinalizarTrasladoForm()
        return true
    }

    // ---------- Validación por el jefe de servicio ----------

    private val _rechazo = MutableStateFlow<RechazoForm?>(null)
    val rechazo: StateFlow<RechazoForm?> = _rechazo.asStateFlow()

    private fun tecnicoDe(t: Traslado): Usuario? = repo.usuarios.value.firstOrNull { it.id == t.tecnicoId }

    fun validarTraslado(actor: Usuario, id: Int): Boolean {
        val t = repo.traslados.value.firstOrNull { it.id == id } ?: return false
        if (!PoliticaAcceso.puedeValidarTraslado(actor, t, tecnicoDe(t))) return false
        repo.resolverTraslado(id, validado = true, jefeId = actor.id, motivo = null, ahoraMs = reloj())
        return true
    }

    fun abrirRechazo(actor: Usuario, id: Int) {
        val t = repo.traslados.value.firstOrNull { it.id == id } ?: return
        if (PoliticaAcceso.puedeValidarTraslado(actor, t, tecnicoDe(t))) {
            _rechazo.value = RechazoForm(trasladoId = id)
        }
    }

    fun onMotivoChange(valor: String) {
        _rechazo.update { it?.copy(motivo = valor, error = null) }
    }

    fun cerrarRechazo() { _rechazo.value = null }

    fun confirmarRechazo(actor: Usuario): Boolean {
        val f = _rechazo.value ?: return false
        val t = repo.traslados.value.firstOrNull { it.id == f.trasladoId } ?: return false
        if (!PoliticaAcceso.puedeValidarTraslado(actor, t, tecnicoDe(t))) return false
        val error = ValidadorTraslado.validarMotivoRechazo(f.motivo)
        if (error != null) {
            _rechazo.update { it?.copy(error = error) }
            return false
        }
        repo.resolverTraslado(t.id, validado = false, jefeId = actor.id, motivo = f.motivo.trim(), ahoraMs = reloj())
        _rechazo.value = null
        return true
    }

    // Deja los formularios vacíos (por ejemplo al cerrar sesión)
    fun limpiarFormularios() {
        _nuevo.value = NuevoTrasladoForm()
        _finalizar.value = FinalizarTrasladoForm()
        _rechazo.value = null
    }
}
