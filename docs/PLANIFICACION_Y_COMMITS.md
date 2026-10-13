# Planificación sugerida (Trello + GitHub)

> Este archivo es una guía para el equipo. Los commits y tarjetas deben hacerlos ustedes
> (la rúbrica evalúa actividad real y distribuida entre ambos integrantes).

## Tablero Trello (listas: Por hacer / En curso / Finalizado)
| Tarjeta | Responsable sugerido | Parte |
|---|---|---|
| Configuración inicial del proyecto + MVVM | Integrante 1 | 1 |
| Tema, AppScaffold y navegación (Screen / NavigationEvent) | Integrante 1 | 1 |
| Pantalla Home adaptable (Window Size Classes) | Integrante 2 | 1 |
| Formulario de registro + componentes de campo | Integrante 2 | 1 |
| Validaciones en ValidadorMascota y MascotaViewModel | Integrante 1 | 1 |
| Pantalla Lista de mascotas | Integrante 2 | 1 |
| Persistencia local | Integrante 1 | 2 |
| Cámara y galería | Integrante 2 | 2 |
| Animaciones | Ambos | 3 |
| README, capturas y revisión final | Ambos | 3 |

## Ramas y commits sugeridos para la Parte 1
- `main`: `Inicio de proyecto + estructura base MVVM`
- `feature/navegacion`: `Agrega rutas tipadas, NavigationEvent y MainViewModel`
- `feature/navegacion`: `Agrega AppScaffold con TopAppBar y NavigationBar`
- `feature/home-screen`: `Pantalla Home adaptable con WindowSizeClass`
- `feature/registro-formulario`: `Agrega modelo MascotaUiState y ValidadorMascota`
- `feature/registro-formulario`: `Implementa MascotaViewModel con validación por campo`
- `feature/registro-formulario`: `Agrega RegistroScreen con retroalimentación visual de errores`
- `feature/lista-mascotas`: `Agrega ListaScreen con LazyColumn y eliminación`
