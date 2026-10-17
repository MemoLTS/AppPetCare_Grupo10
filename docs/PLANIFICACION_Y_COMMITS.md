# Planificación sugerida (Trello + GitHub) – TeqRuta

> Esta es una guía para el equipo. Los commits y tarjetas deben hacerlos ustedes
> (la rúbrica evalúa actividad real y distribuida entre ambos integrantes).

## Tablero Trello (listas: Por hacer / En curso / Finalizado)
| Tarjeta | Responsable sugerido | Parte |
|---|---|---|
| Configuración inicial del proyecto + MVVM | Integrante 1 | 1 |
| Tema, AppScaffold y navegación (Screen / NavigationEvent) | Integrante 1 | 1 |
| Login, usuarios, roles y PoliticaAcceso | Integrante 1 | 1 |
| Pantalla Home adaptable (Window Size Classes) según rol | Integrante 2 | 1 |
| Modelo de dominio (vehículos, ubicaciones, tramos, tarifa, traslados) | Integrante 1 | 1 |
| Cálculo de costos centralizado + pruebas unitarias | Integrante 1 | 1 |
| Registro de traslados del técnico (iniciar y finalizar) con validaciones | Integrante 2 | 1 |
| Lista de traslados y validación del jefe de servicio | Integrante 2 | 1 |
| Reporte de costos por cliente, técnico y vehículo | Integrante 2 | 1 |
| Catálogos del administrador (vehículos, ubicaciones, tramos, tarifa) | Ambos | 1 |
| Persistencia local (Room) y trabajo sin conexión | Integrante 1 | 2 |
| Sincronización automática al recuperar señal (WorkManager) | Integrante 1 | 2 |
| Fuente pública de precios de combustible | Integrante 2 | 2 |
| Animaciones y retroalimentación visual | Ambos | 3 |
| README, capturas y revisión final | Ambos | 3 |

> Cámara y galería (que figuraban en el plan anterior) no aparecen en el caso TeqRuta: confirmar con el docente si la rúbrica las exige.

## Ramas y commits sugeridos para la Parte 1
- `main`: `Inicio de proyecto + estructura base MVVM`
- `feature/navegacion`: `Agrega rutas tipadas, NavigationEvent y MainViewModel`
- `feature/navegacion`: `Agrega AppScaffold con menú según el rol`
- `feature/usuarios-roles`: `Agrega login, usuarios, roles y PoliticaAcceso`
- `feature/dominio`: `Agrega vehículos, ubicaciones, tramos, tarifa y traslados`
- `feature/costos`: `Agrega CalculadoraCostos con registro auditable y pruebas`
- `feature/traslados-tecnico`: `Agrega registro de inicio y fin de traslado con validaciones`
- `feature/validacion-jefe`: `Agrega lista de traslados y validación o rechazo por el jefe`
- `feature/reportes`: `Agrega reporte de costos por cliente, técnico y vehículo`
- `feature/catalogos`: `Agrega mantenedores del administrador`
