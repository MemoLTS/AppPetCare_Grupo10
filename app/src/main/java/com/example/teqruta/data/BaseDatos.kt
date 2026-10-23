package com.example.teqruta.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.teqruta.model.CalculoCosto
import com.example.teqruta.model.EstadoTraslado
import com.example.teqruta.model.Rol
import com.example.teqruta.model.TipoUbicacion
import com.example.teqruta.model.TipoVehiculo
import com.example.teqruta.model.Tramo
import com.example.teqruta.model.Traslado
import com.example.teqruta.model.Ubicacion
import com.example.teqruta.model.Usuario
import com.example.teqruta.model.Vehiculo

// Base de datos SQL local (SQLite) de la app. Guarda usuarios, vehículos, ubicaciones, tramos y
// traslados, de modo que los datos sobreviven al cierre de la app.
//
// - Los vehículos se registran en su propia tabla y cada traslado los referencia por patente
//   (traslados.patente -> vehiculos.patente).
// - Las claves foráneas están activas: no se puede registrar un traslado con una patente que no exista.
// - Al crearse por primera vez se carga con los datos de ejemplo de DatosIniciales.
//
// Las lecturas y escrituras son de pocas filas, por eso se hacen directamente; al sumar
// sincronización con un servidor conviene moverlas a un hilo de fondo.
class BaseDatos(context: Context) :
    SQLiteOpenHelper(context.applicationContext, NOMBRE, null, VERSION) {

    companion object {
        const val NOMBRE = "teqruta.db"
        const val VERSION = 1
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE usuarios (
                id INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL,
                correo TEXT NOT NULL UNIQUE,
                rol TEXT NOT NULL,
                activo INTEGER NOT NULL DEFAULT 1,
                salt TEXT NOT NULL,
                hash_clave TEXT NOT NULL,
                jefe_id INTEGER REFERENCES usuarios(id)
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE vehiculos (
                id INTEGER PRIMARY KEY,
                patente TEXT NOT NULL UNIQUE,
                descripcion TEXT NOT NULL,
                tipo TEXT NOT NULL,
                costo_por_km_clp REAL NOT NULL CHECK (costo_por_km_clp > 0),
                activo INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE ubicaciones (
                id INTEGER PRIMARY KEY,
                nombre TEXT NOT NULL,
                ciudad TEXT NOT NULL,
                direccion TEXT NOT NULL,
                tipo TEXT NOT NULL,
                activa INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE tramos (
                id INTEGER PRIMARY KEY,
                origen_id INTEGER NOT NULL REFERENCES ubicaciones(id),
                destino_id INTEGER NOT NULL REFERENCES ubicaciones(id),
                distancia_km REAL NOT NULL CHECK (distancia_km > 0),
                peaje_clp INTEGER NOT NULL DEFAULT 0,
                activo INTEGER NOT NULL DEFAULT 1
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE traslados (
                id INTEGER PRIMARY KEY,
                tecnico_id INTEGER NOT NULL REFERENCES usuarios(id),
                patente TEXT NOT NULL REFERENCES vehiculos(patente),
                origen_id INTEGER NOT NULL REFERENCES ubicaciones(id),
                destino_id INTEGER NOT NULL REFERENCES ubicaciones(id),
                inicio_ms INTEGER NOT NULL,
                fin_ms INTEGER,
                distancia_km REAL,
                gasto_vehiculo_clp INTEGER,
                estado TEXT NOT NULL,
                validado_por_id INTEGER REFERENCES usuarios(id),
                resuelto_ms INTEGER,
                motivo_rechazo TEXT,
                calc_distancia_oficial_km REAL,
                calc_costo_por_km_clp REAL,
                calc_costo_distancia_clp INTEGER,
                calc_peaje_clp INTEGER,
                calc_costo_total_clp INTEGER,
                calc_desviacion_pct REAL,
                calc_hay_desviacion INTEGER,
                calc_ms INTEGER
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_traslados_patente ON traslados(patente)")
        db.execSQL("CREATE INDEX idx_traslados_tecnico ON traslados(tecnico_id)")

        // Datos de ejemplo (el orden respeta las claves foráneas)
        DatosIniciales.ubicaciones().forEach { guardarUbicacion(db, it) }
        DatosIniciales.vehiculos().forEach { guardarVehiculo(db, it) }
        DatosIniciales.usuarios().forEach { guardarUsuario(db, it) }
        DatosIniciales.tramos().forEach { guardarTramo(db, it) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Aún no hay versiones anteriores. Las futuras migraciones se agregan aquí.
    }

    // ---------- Escritura (insertar o actualizar por id) ----------

    fun guardarUsuario(u: Usuario) = guardarUsuario(writableDatabase, u)
    fun guardarVehiculo(v: Vehiculo) = guardarVehiculo(writableDatabase, v)
    fun guardarUbicacion(u: Ubicacion) = guardarUbicacion(writableDatabase, u)
    fun guardarTramo(t: Tramo) = guardarTramo(writableDatabase, t)
    fun guardarTraslado(t: Traslado) = guardarTraslado(writableDatabase, t)

    private fun guardarUsuario(db: SQLiteDatabase, u: Usuario) {
        val v = ContentValues().apply {
            put("id", u.id)
            put("nombre", u.nombre)
            put("correo", u.correo)
            put("rol", u.rol.name)
            put("activo", u.activo.aEntero())
            put("salt", u.salt)
            put("hash_clave", u.hashClave)
            opcional("jefe_id", u.jefeId)
        }
        upsert(db, "usuarios", u.id, v)
    }

    private fun guardarVehiculo(db: SQLiteDatabase, x: Vehiculo) {
        val v = ContentValues().apply {
            put("id", x.id)
            put("patente", x.patente)
            put("descripcion", x.descripcion)
            put("tipo", x.tipo.name)
            put("costo_por_km_clp", x.costoPorKmClp)
            put("activo", x.activo.aEntero())
        }
        upsert(db, "vehiculos", x.id, v)
    }

    private fun guardarUbicacion(db: SQLiteDatabase, u: Ubicacion) {
        val v = ContentValues().apply {
            put("id", u.id)
            put("nombre", u.nombre)
            put("ciudad", u.ciudad)
            put("direccion", u.direccion)
            put("tipo", u.tipo.name)
            put("activa", u.activa.aEntero())
        }
        upsert(db, "ubicaciones", u.id, v)
    }

    private fun guardarTramo(db: SQLiteDatabase, t: Tramo) {
        val v = ContentValues().apply {
            put("id", t.id)
            put("origen_id", t.origenId)
            put("destino_id", t.destinoId)
            put("distancia_km", t.distanciaKm)
            put("peaje_clp", t.peajeClp)
            put("activo", t.activo.aEntero())
        }
        upsert(db, "tramos", t.id, v)
    }

    private fun guardarTraslado(db: SQLiteDatabase, t: Traslado) {
        val c = t.calculo
        val v = ContentValues().apply {
            put("id", t.id)
            put("tecnico_id", t.tecnicoId)
            put("patente", t.patente)
            put("origen_id", t.origenId)
            put("destino_id", t.destinoId)
            put("inicio_ms", t.inicioMs)
            opcional("fin_ms", t.finMs)
            opcional("distancia_km", t.distanciaKm)
            opcional("gasto_vehiculo_clp", t.gastoVehiculoClp)
            put("estado", t.estado.name)
            opcional("validado_por_id", t.validadoPorId)
            opcional("resuelto_ms", t.resueltoMs)
            opcional("motivo_rechazo", t.motivoRechazo)
            opcional("calc_distancia_oficial_km", c?.distanciaOficialKm)
            opcional("calc_costo_por_km_clp", c?.costoPorKmClp)
            opcional("calc_costo_distancia_clp", c?.costoDistanciaClp)
            opcional("calc_peaje_clp", c?.peajeClp)
            opcional("calc_costo_total_clp", c?.costoTotalClp)
            opcional("calc_desviacion_pct", c?.desviacionPct)
            opcional("calc_hay_desviacion", c?.hayDesviacion?.aEntero())
            opcional("calc_ms", c?.calculadoMs)
        }
        upsert(db, "traslados", t.id, v)
    }

    // Actualiza la fila si existe y, si no, la inserta. (No se usa REPLACE porque borraría y
    // volvería a crear la fila, y con claves foráneas activas eso es innecesariamente riesgoso.)
    private fun upsert(db: SQLiteDatabase, tabla: String, id: Int, valores: ContentValues) {
        val filas = db.update(tabla, valores, "id = ?", arrayOf(id.toString()))
        if (filas == 0) db.insertOrThrow(tabla, null, valores)
    }

    // ---------- Lectura ----------

    fun leerUsuarios(): List<Usuario> = leer("usuarios") { c ->
        Usuario(
            id = c.int("id"),
            nombre = c.texto("nombre"),
            correo = c.texto("correo"),
            rol = Rol.valueOf(c.texto("rol")),
            activo = c.int("activo") == 1,
            salt = c.texto("salt"),
            hashClave = c.texto("hash_clave"),
            jefeId = c.intONull("jefe_id")
        )
    }

    fun leerVehiculos(): List<Vehiculo> = leer("vehiculos") { c ->
        Vehiculo(
            id = c.int("id"),
            patente = c.texto("patente"),
            descripcion = c.texto("descripcion"),
            tipo = TipoVehiculo.valueOf(c.texto("tipo")),
            costoPorKmClp = c.decimal("costo_por_km_clp"),
            activo = c.int("activo") == 1
        )
    }

    fun leerUbicaciones(): List<Ubicacion> = leer("ubicaciones") { c ->
        Ubicacion(
            id = c.int("id"),
            nombre = c.texto("nombre"),
            ciudad = c.texto("ciudad"),
            direccion = c.texto("direccion"),
            tipo = TipoUbicacion.valueOf(c.texto("tipo")),
            activa = c.int("activa") == 1
        )
    }

    fun leerTramos(): List<Tramo> = leer("tramos") { c ->
        Tramo(
            id = c.int("id"),
            origenId = c.int("origen_id"),
            destinoId = c.int("destino_id"),
            distanciaKm = c.decimal("distancia_km"),
            peajeClp = c.int("peaje_clp"),
            activo = c.int("activo") == 1
        )
    }

    fun leerTraslados(): List<Traslado> = leer("traslados") { c ->
        val distancia = c.decimalONull("distancia_km")
        val calculo = if (c.esNulo("calc_costo_total_clp")) null else CalculoCosto(
            distanciaOficialKm = c.decimal("calc_distancia_oficial_km"),
            distanciaDeclaradaKm = distancia ?: 0.0,
            costoPorKmClp = c.decimal("calc_costo_por_km_clp"),
            costoDistanciaClp = c.int("calc_costo_distancia_clp"),
            peajeClp = c.int("calc_peaje_clp"),
            costoTotalClp = c.int("calc_costo_total_clp"),
            desviacionPct = c.decimal("calc_desviacion_pct"),
            hayDesviacion = c.int("calc_hay_desviacion") == 1,
            calculadoMs = c.long("calc_ms")
        )
        Traslado(
            id = c.int("id"),
            tecnicoId = c.int("tecnico_id"),
            patente = c.texto("patente"),
            origenId = c.int("origen_id"),
            destinoId = c.int("destino_id"),
            inicioMs = c.long("inicio_ms"),
            finMs = c.longONull("fin_ms"),
            distanciaKm = distancia,
            gastoVehiculoClp = c.intONull("gasto_vehiculo_clp"),
            estado = EstadoTraslado.valueOf(c.texto("estado")),
            calculo = calculo,
            validadoPorId = c.intONull("validado_por_id"),
            resueltoMs = c.longONull("resuelto_ms"),
            motivoRechazo = c.textoONull("motivo_rechazo")
        )
    }

    private fun <T> leer(tabla: String, mapear: (Cursor) -> T): List<T> =
        readableDatabase.rawQuery("SELECT * FROM $tabla ORDER BY id", null).use { c ->
            buildList { while (c.moveToNext()) add(mapear(c)) }
        }
}

// ---------- Utilidades de conversión entre filas SQL y valores de Kotlin ----------

private fun Boolean.aEntero(): Int = if (this) 1 else 0

// ContentValues no admite nulos tipados: se guarda NULL explícito cuando no hay valor.
private fun ContentValues.opcional(clave: String, valor: Any?) {
    when (valor) {
        null -> putNull(clave)
        is Int -> put(clave, valor)
        is Long -> put(clave, valor)
        is Double -> put(clave, valor)
        is String -> put(clave, valor)
        else -> error("Tipo no soportado para la columna $clave")
    }
}

private fun Cursor.indice(columna: String): Int = getColumnIndexOrThrow(columna)
private fun Cursor.esNulo(columna: String): Boolean = isNull(indice(columna))
private fun Cursor.int(columna: String): Int = getInt(indice(columna))
private fun Cursor.long(columna: String): Long = getLong(indice(columna))
private fun Cursor.decimal(columna: String): Double = getDouble(indice(columna))
private fun Cursor.texto(columna: String): String = getString(indice(columna))
private fun Cursor.intONull(columna: String): Int? = if (esNulo(columna)) null else int(columna)
private fun Cursor.longONull(columna: String): Long? = if (esNulo(columna)) null else long(columna)
private fun Cursor.decimalONull(columna: String): Double? = if (esNulo(columna)) null else decimal(columna)
private fun Cursor.textoONull(columna: String): String? = if (esNulo(columna)) null else texto(columna)
