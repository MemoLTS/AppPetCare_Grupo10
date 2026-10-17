package com.example.teqruta.data

import com.example.teqruta.model.CalculadoraCostos
import com.example.teqruta.model.EstadoTraslado
import com.example.teqruta.model.TarifaCombustible
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
// Aquí solo se guardan y consultan datos: los permisos y las validaciones de formularios
// viven en PoliticaAcceso y en los ViewModels. El cálculo de costos pasa siempre por
// finalizarTraslado, de modo que existe un único punto donde se calcula un monto.
//
// Hoy los datos viven en memoria. Este es el punto donde se conectará la base de datos local
// (para trabajar sin conexión) y la sincronización con el servidor.
class RepositorioDatos {

    private val _usuarios = MutableStateFlow(DatosIniciales.usuarios())
    private val _vehiculos = MutableStateFlow(DatosIniciales.vehiculos())
    private val _ubicaciones = MutableStateFlow(DatosIniciales.ubicaciones())
    private val _tramos = MutableStateFlow(DatosIniciales.tramos())
    private val _tarifas = MutableStateFlow(DatosIniciales.tarifas())
    private val _traslados = MutableStateFlow<List<Traslado>>(emptyList())

    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()
    val vehiculos: StateFlow<List<Vehiculo>> = _vehiculos.asStateFlow()
    val ubicaciones: StateFlow<List<Ubicacion>> = _ubicaciones.asStateFlow()
    val tramos: StateFlow<List<Tramo>> = _tramos.asStateFlow()
    val tarifas: StateFlow<List<TarifaCombustible>> = _tarifas.asStateFlow()
    val traslados: StateFlow<List<Traslado>> = _traslados.asStateFlow()

    // Guarda un registro: si su id es 0 le asigna el siguiente id; si no, reemplaza el existente.
    private fun <T> MutableStateFlow<List<T>>.guardar(
        item: T,
        idDe: (T) -> Int,
        conId: (T, Int) -> T
    ): T {
        var guardado = item
        update { lista ->
            if (idDe(item) == 0) {
                guardado = conId(item, (lista.maxOfOrNull(idDe) ?: 0) + 1)
                lista + guardado
            } else {
                guardado = item
                lista.map { if (idDe(it) == idDe(item)) item else it }
            }
        }
        return guardado
    }

    // ---------- Catálogos ----------

    fun guardarUsuario(u: Usuario): Usuario = _usuarios.guardar(u, { it.id }) { x, id -> x.copy(id = id) }
    fun guardarVehiculo(v: Vehiculo): Vehiculo = _vehiculos.guardar(v, { it.id }) { x, id -> x.copy(id = id) }
    fun guardarUbicacion(u: Ubicacion): Ubicacion = _ubicaciones.guardar(u, { it.id }) { x, id -> x.copy(id = id) }
    fun guardarTramo(t: Tramo): Tramo = _tramos.guardar(t, { it.id }) { x, id -> x.copy(id = id) }

    // Registra una nueva tarifa vigente; las anteriores quedan como historial.
    fun registrarTarifa(precioLitroClp: Int, fuente: String, usuarioId: Int?, ahoraMs: Long): TarifaCombustible =
        _tarifas.guardar(
            TarifaCombustible(0, precioLitroClp, ahoraMs, fuente, usuarioId),
            { it.id }
        ) { x, id -> x.copy(id = id) }

    fun tarifaVigente(): TarifaCombustible? =
        _tarifas.value.maxWithOrNull(compareBy({ it.vigenteDesdeMs }, { it.id }))

    // Tramo oficial entre dos ubicaciones, en cualquier sentido.
    fun tramoEntre(a: Int, b: Int, soloActivos: Boolean = true): Tramo? =
        _tramos.value.firstOrNull { it.conecta(a, b) && (!soloActivos || it.activo) }

    // ---------- Traslados ----------

    // Último kilometraje conocido del vehículo (el odómetro no retrocede). Se ignoran los rechazados.
    fun ultimoKmDeVehiculo(vehiculoId: Int): Int? =
        _traslados.value
            .filter { it.vehiculoId == vehiculoId && it.estado != EstadoTraslado.RECHAZADO }
            .maxOfOrNull { it.kmFinal ?: it.kmInicial }

    fun trasladoEnCurso(tecnicoId: Int): Traslado? =
        _traslados.value.firstOrNull { it.tecnicoId == tecnicoId && it.estado == EstadoTraslado.EN_CURSO }

    // Último destino de un técnico: sirve para proponer el siguiente origen.
    fun ultimoDestinoDe(tecnicoId: Int): Int? =
        _traslados.value
            .filter { it.tecnicoId == tecnicoId && it.estado != EstadoTraslado.RECHAZADO }
            .maxByOrNull { it.inicioMs }?.destinoId

    fun iniciarTraslado(
        tecnicoId: Int,
        vehiculoId: Int,
        origenId: Int,
        destinoId: Int,
        kmInicial: Int,
        ahoraMs: Long
    ): Traslado = _traslados.guardar(
        Traslado(
            id = 0, tecnicoId = tecnicoId, vehiculoId = vehiculoId,
            origenId = origenId, destinoId = destinoId,
            inicioMs = ahoraMs, kmInicial = kmInicial
        ),
        { it.id }
    ) { x, id -> x.copy(id = id) }

    // Cierra el traslado, calcula su costo con la tarifa vigente y lo deja pendiente de validación.
    // Retorna null si faltan datos oficiales para calcular (tramo, tarifa o vehículo).
    fun finalizarTraslado(id: Int, kmFinal: Int, ahoraMs: Long): Traslado? {
        val traslado = _traslados.value.firstOrNull { it.id == id } ?: return null
        val tramo = tramoEntre(traslado.origenId, traslado.destinoId, soloActivos = false) ?: return null
        val tarifa = tarifaVigente() ?: return null
        val vehiculo = _vehiculos.value.firstOrNull { it.id == traslado.vehiculoId } ?: return null

        val calculo = CalculadoraCostos.calcular(
            tramo = tramo,
            tarifa = tarifa,
            vehiculo = vehiculo,
            kmDeclarados = kmFinal - traslado.kmInicial,
            ahoraMs = ahoraMs
        )
        val finalizado = traslado.copy(
            finMs = ahoraMs,
            kmFinal = kmFinal,
            estado = EstadoTraslado.PENDIENTE_VALIDACION,
            calculo = calculo
        )
        return _traslados.guardar(finalizado, { it.id }) { x, _ -> x }
    }

    fun resolverTraslado(id: Int, validado: Boolean, jefeId: Int, motivo: String?, ahoraMs: Long): Traslado? {
        val traslado = _traslados.value.firstOrNull { it.id == id } ?: return null
        val resuelto = traslado.copy(
            estado = if (validado) EstadoTraslado.VALIDADO else EstadoTraslado.RECHAZADO,
            validadoPorId = jefeId,
            resueltoMs = ahoraMs,
            motivoRechazo = motivo
        )
        return _traslados.guardar(resuelto, { it.id }) { x, _ -> x }
    }
}
