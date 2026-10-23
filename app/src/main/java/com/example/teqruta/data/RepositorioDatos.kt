package com.example.teqruta.data

import com.example.teqruta.model.CalculadoraCostos
import com.example.teqruta.model.EstadoTraslado
import com.example.teqruta.model.Tramo
import com.example.teqruta.model.Traslado
import com.example.teqruta.model.Ubicacion
import com.example.teqruta.model.Usuario
import com.example.teqruta.model.Vehiculo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

// Única fuente de datos de la app, compartida por todos los ViewModels.
// Los datos viven en la base SQL local (BaseDatos): cada escritura se guarda ahí y luego se
// refleja en los flujos que observan las pantallas. Aquí solo se guardan y consultan datos:
// los permisos y validaciones viven en PoliticaAcceso y en los ViewModels. El cálculo de costos
// pasa siempre por finalizarTraslado, de modo que existe un único punto donde se calcula un monto.
class RepositorioDatos(private val bd: BaseDatos) {

    private val _usuarios = MutableStateFlow(bd.leerUsuarios())
    private val _vehiculos = MutableStateFlow(bd.leerVehiculos())
    private val _ubicaciones = MutableStateFlow(bd.leerUbicaciones())
    private val _tramos = MutableStateFlow(bd.leerTramos())
    private val _traslados = MutableStateFlow(bd.leerTraslados())

    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()
    val vehiculos: StateFlow<List<Vehiculo>> = _vehiculos.asStateFlow()
    val ubicaciones: StateFlow<List<Ubicacion>> = _ubicaciones.asStateFlow()
    val tramos: StateFlow<List<Tramo>> = _tramos.asStateFlow()
    val traslados: StateFlow<List<Traslado>> = _traslados.asStateFlow()

    // Guarda un registro: si su id es 0 le asigna el siguiente id; si no, reemplaza el existente.
    // Primero escribe en la base de datos y, si resulta bien, actualiza la lista en memoria.
    private fun <T> MutableStateFlow<List<T>>.guardar(
        item: T,
        idDe: (T) -> Int,
        conId: (T, Int) -> T,
        enBd: (T) -> Unit
    ): T {
        val guardado = if (idDe(item) == 0) conId(item, (value.maxOfOrNull(idDe) ?: 0) + 1) else item
        enBd(guardado)
        update { lista ->
            if (lista.any { idDe(it) == idDe(guardado) }) {
                lista.map { if (idDe(it) == idDe(guardado)) guardado else it }
            } else {
                lista + guardado
            }
        }
        return guardado
    }

    // ---------- Catálogos ----------

    fun guardarUsuario(u: Usuario): Usuario =
        _usuarios.guardar(u, { it.id }, { x, id -> x.copy(id = id) }, bd::guardarUsuario)

    fun guardarVehiculo(v: Vehiculo): Vehiculo =
        _vehiculos.guardar(v, { it.id }, { x, id -> x.copy(id = id) }, bd::guardarVehiculo)

    fun guardarUbicacion(u: Ubicacion): Ubicacion =
        _ubicaciones.guardar(u, { it.id }, { x, id -> x.copy(id = id) }, bd::guardarUbicacion)

    fun guardarTramo(t: Tramo): Tramo =
        _tramos.guardar(t, { it.id }, { x, id -> x.copy(id = id) }, bd::guardarTramo)

    // Un vehículo se identifica por su patente (única), siempre en mayúsculas.
    fun vehiculoPorPatente(patente: String): Vehiculo? =
        _vehiculos.value.firstOrNull { it.patente == patente.uppercase() }

    // Tramo oficial entre dos ubicaciones, en cualquier sentido.
    fun tramoEntre(a: Int, b: Int, soloActivos: Boolean = true): Tramo? =
        _tramos.value.firstOrNull { it.conecta(a, b) && (!soloActivos || it.activo) }

    // ---------- Traslados ----------

    fun trasladoEnCurso(tecnicoId: Int): Traslado? =
        _traslados.value.firstOrNull { it.tecnicoId == tecnicoId && it.estado == EstadoTraslado.EN_CURSO }

    // Último destino de un técnico: sirve para proponer el siguiente origen.
    fun ultimoDestinoDe(tecnicoId: Int): Int? =
        _traslados.value
            .filter { it.tecnicoId == tecnicoId && it.estado != EstadoTraslado.RECHAZADO }
            .maxByOrNull { it.inicioMs }?.destinoId

    // Última patente que usó un técnico: sirve para proponerla en la orden siguiente.
    fun ultimaPatenteDe(tecnicoId: Int): String? =
        _traslados.value
            .filter { it.tecnicoId == tecnicoId }
            .maxByOrNull { it.inicioMs }?.patente

    // Registra la orden asociada al vehículo por su patente.
    fun iniciarTraslado(
        tecnicoId: Int,
        patente: String,
        origenId: Int,
        destinoId: Int,
        ahoraMs: Long
    ): Traslado = _traslados.guardar(
        Traslado(
            id = 0, tecnicoId = tecnicoId, patente = patente.uppercase(),
            origenId = origenId, destinoId = destinoId, inicioMs = ahoraMs
        ),
        { it.id }, { x, id -> x.copy(id = id) }, bd::guardarTraslado
    )

    // Cierra el traslado con los km declarados (y el gasto opcional), calcula su costo y lo deja
    // pendiente de validación. Retorna null si faltan datos oficiales (tramo o vehículo).
    fun finalizarTraslado(id: Int, distanciaKm: Double, gastoVehiculoClp: Int?, ahoraMs: Long): Traslado? {
        val traslado = _traslados.value.firstOrNull { it.id == id } ?: return null
        val tramo = tramoEntre(traslado.origenId, traslado.destinoId, soloActivos = false) ?: return null
        val vehiculo = vehiculoPorPatente(traslado.patente) ?: return null

        val calculo = CalculadoraCostos.calcular(tramo, vehiculo, distanciaKm, ahoraMs)
        val finalizado = traslado.copy(
            finMs = ahoraMs,
            distanciaKm = distanciaKm,
            gastoVehiculoClp = gastoVehiculoClp,
            estado = EstadoTraslado.PENDIENTE_VALIDACION,
            calculo = calculo
        )
        return _traslados.guardar(finalizado, { it.id }, { x, _ -> x }, bd::guardarTraslado)
    }

    fun resolverTraslado(id: Int, validado: Boolean, jefeId: Int, motivo: String?, ahoraMs: Long): Traslado? {
        val traslado = _traslados.value.firstOrNull { it.id == id } ?: return null
        val resuelto = traslado.copy(
            estado = if (validado) EstadoTraslado.VALIDADO else EstadoTraslado.RECHAZADO,
            validadoPorId = jefeId,
            resueltoMs = ahoraMs,
            motivoRechazo = motivo
        )
        return _traslados.guardar(resuelto, { it.id }, { x, _ -> x }, bd::guardarTraslado)
    }
}
