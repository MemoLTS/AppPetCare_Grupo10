# TeqRuta – Traslados en terreno (TEQMED SpA)

Aplicación móvil Android (Kotlin + Jetpack Compose, arquitectura MVVM) para registrar los traslados
del personal técnico de TEQMED, calcular su costo con tarifas oficiales y entregar información
verificable del gasto de movilización por cliente, técnico y vehículo.
Caso: DSY1105 – Desarrollo de Aplicaciones Móviles (CITT Concepción).

> Estado: **Parte 1** (interfaz, navegación, formularios con validación, roles y lógica de negocio con
> datos en memoria). Lo que falta respecto del caso (trabajo sin conexión, fuente pública de combustible,
> servidor) está en `docs/ANALISIS_CASO.md`.

## Qué hace cada perfil

| Perfil | Qué puede hacer |
|---|---|
| **Técnico de servicio** | Inicia y finaliza sus traslados (origen, destino, odómetro). Ve solo los suyos y el motivo si se rechaza alguno. No ve costos. |
| **Jefe de servicio técnico** | Ve los traslados de su equipo, los valida o rechaza (con motivo) y revisa las desviaciones. Ve el costo de cada traslado. |
| **Gerencia / Finanzas** | Solo lectura: ve todos los traslados y el costo real por cliente, técnico y vehículo. |
| **Administrador** | Mantiene vehículos, ubicaciones (sede y clientes), tramos (distancia y peaje), tarifa de combustible y usuarios. |

## Cómo se calcula el costo
El técnico solo declara el odómetro; el monto sale de datos oficiales y se calcula en un único lugar
(`CalculadoraCostos`):

```
combustible = (distancia oficial del tramo / rendimiento del vehículo) × precio por litro vigente
costo total = combustible + peajes del tramo
desviación  = (km declarados − distancia oficial) / distancia oficial     → se marca si supera ±10 %
```
Cada traslado guarda una copia de los datos usados (tarifa, distancia, rendimiento, peaje, resultado), de modo que
el cálculo se puede reconstruir aunque después cambien las tarifas. Solo los traslados **validados** por el jefe
entran a los reportes.

## Flujo de un traslado
`En curso` → (el técnico finaliza) → `Por validar` → (el jefe) → `Validado` o `Rechazado`.
Un traslado no se edita ni se elimina una vez registrado, para mantener la trazabilidad.
Los catálogos (vehículos, ubicaciones, tramos) tampoco se borran: se desactivan.

## Usuarios de prueba

| Correo | Contraseña | Perfil |
|---|---|---|
| admin@teqmed.cl | Admin123 | Administrador |
| jefe1@teqmed.cl | Jefe1234 | Jefe de servicio (equipo: técnicos 1 y 2) |
| jefe2@teqmed.cl | Jefe1234 | Jefe de servicio (equipo: técnico 3) |
| tecnico1@teqmed.cl · tecnico2@teqmed.cl · tecnico3@teqmed.cl | Tecnico123 | Técnico |
| gerencia@teqmed.cl | Gerencia123 | Gerencia / Finanzas |

Las ubicaciones, distancias, peajes, tarifa de combustible y vehículos precargados son **datos de ejemplo**:
el administrador debe reemplazarlos por los reales. Mientras no haya persistencia, todo lo que se cree se pierde
al cerrar la app.

## Estructura
```
app/src/main/java/com/example/teqruta/
├── MainActivity.kt
├── data/         RepositorioDatos (fuente única de datos), DatosIniciales, AppContainer
├── model/        Usuario, Rol/Permiso, PoliticaAcceso, Catalogos (Vehiculo, Ubicacion, Tramo, TarifaCombustible),
│                 Traslado, CalculadoraCostos, ReporteCostos, validadores, formularios, HashContrasena
├── viewmodel/    MainViewModel (navegación), UsuarioViewModel (sesión y usuarios),
│                 TrasladoViewModel (registro y validación), CatalogoViewModel (administración)
├── navigation/   Screen, NavigationEvent, AppNavigation
└── ui/
    ├── components/  AppScaffold, CampoFormulario, SelectorDesplegable, SelectorRol, DialogoFormulario, MensajeCentrado
    ├── screens/     Login, Home, TrasladoNuevo, TrasladoFinalizar, Traslados, Reportes, Catalogos, Usuarios, UsuarioForm
    ├── theme/       Color, Theme, Type
    └── utils/       Formato, WindowSizeUtils
app/src/test/        Pruebas unitarias del cálculo de costos, permisos y reportes
```

## Seguridad y datos personales
- Los permisos están en `PoliticaAcceso`: las pantallas la usan para mostrar u ocultar opciones y los ViewModels
  para rechazar la operación aunque la UI no la oculte. Un usuario desactivado pierde todos sus permisos.
- Contraseñas con hash PBKDF2-HMAC-SHA256 y sal aleatoria; el login no revela qué correos existen.
- Mínimo de datos personales (nombre y correo). Cada perfil ve solo lo necesario: el técnico lo suyo, el jefe su equipo.
- El técnico no ve costos ni la distancia oficial, para no sesgar lo que declara.

## Ejecutar
Abrir la carpeta en Android Studio (Ladybug o superior), sincronizar Gradle y ejecutar en un emulador o dispositivo
con Android 8.0 (API 26) o superior. Las pruebas unitarias se ejecutan con `./gradlew test`.
