# PetCare – Registro de mascotas (AppPetCare_Grupo10)

Proyecto academico de la asignatura **DSY1105 – Desarrollo de Aplicaciones Móviles**

Aplicación móvil Android (Kotlin + Jetpack Compose + Material 3, patrón MVVM) para el registro y seguimiento de mascotas de una clínica veterinaria.

## Integrantes
- 1: Pablo Flores 
- 2: Guillermo Schroeder

## Contexto y problema
Una clínica veterinaria pequeña lleva el registro de sus pacientes en papel, lo que genera pérdida de datos y dificulta contactar a los dueños.
PetCare permite registrar mascotas con los datos de su dueño y consultarlas desde el teléfono.

## Funcionalidades implementadas (avance Parte 1)
- Interfaz con `TopAppBar` + `NavigationBar` y navegación entre 3 pantallas (Inicio, Registrar, Mascotas).
- Diseño adaptable (Window Size Classes) en la pantalla de inicio: compacta vs. mediana/expandida.
- Formulario de registro con validación por campo (ícono, borde rojo, ícono de advertencia y mensaje).
- Validaciones en lógica desacoplada (`ValidadorMascota` + `MascotaViewModel`), no en los Composables.
- Lista de mascotas registradas (en memoria) con opción de eliminar.

## Pendiente (siguientes partes)
- Persistencia local de los datos.
- Recursos nativos (cámara y galería para la foto de la mascota).
- Animaciones.

## Estructura del proyecto
```
app/src/main/java/com/example/petcare/
├── model/        Mascota, MascotaUiState, ValidadorMascota
├── viewmodel/    MainViewModel (navegación), MascotaViewModel (formulario y lista)
├── navigation/   Screen, NavigationEvent, AppNavigation
└── ui/
    ├── components/  AppScaffold, CampoFormulario, SelectorEspecie
    ├── screens/     HomeScreen, RegistroScreen, ListaScreen
    ├── theme/       Color, Type, Theme
    └── utils/       WindowSizeUtils
```

## Cómo ejecutar
1. Abrir la carpeta del proyecto con **Android Studio** (versión reciente) y esperar el *Gradle Sync*.
2. Crear un dispositivo en **Device Manager** (API 26 o superior) o conectar un teléfono Android.
3. Presionar **Run ▶**.

