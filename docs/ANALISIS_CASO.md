# Análisis del caso TeqRuta frente al proyecto entregado

## 1. Inconsistencias encontradas en el proyecto original
| # | Hallazgo | Qué se hizo |
|---|---|---|
| 1 | La app entera era **PetCare** (registro de mascotas para una veterinaria): dominio, pantallas, textos, ícono y nombre del paquete no correspondían al caso TeqRuta. | Se reemplazó el dominio por traslados, vehículos, ubicaciones, tramos y tarifas. Paquete `com.example.teqruta`. |
| 2 | Los roles que se habían definido (usuario, gerente, administrador, auditor) **no coinciden con los perfiles del caso** (técnico, jefe de servicio, gerencia/finanzas, administrador). | Roles rehechos según §3.1 del caso. |
| 3 | El "gerente" podía modificar registros; en el caso **Gerencia solo consulta** costos. | Gerencia / Finanzas es solo lectura. |
| 4 | El administrador podía "ver todo"; el caso le asigna mantener vehículos, destinos, tarifas. | El administrador gestiona catálogos y usuarios, pero no ve traslados (mínimo privilegio y menos datos personales expuestos). |
| 5 | La relación entre gerente y usuarios a cargo había quedado como pregunta abierta; el caso la resuelve: **el jefe de servicio valida los traslados de su equipo**. | Cada técnico tiene un jefe asignado; el jefe ve y valida solo a su equipo. |
| 6 | La app permitía editar y eliminar registros libremente. Un control de gastos **auditable** no puede permitirlo. | Los traslados no se editan ni se eliminan: solo se validan o rechazan. Los catálogos se desactivan, no se borran. |
| 7 | No existía cálculo de costos ni tarifas; el caso exige cálculo **centralizado y auditable**. | `CalculadoraCostos` es el único punto de cálculo y cada traslado guarda los datos usados. El técnico no ingresa montos. |
| 8 | El plan de trabajo (`PLANIFICACION_Y_COMMITS.md`) listaba tareas de PetCare. | Actualizado al caso. Cámara/galería no aparece en el caso: confirmar con el docente si la rúbrica la exige. |

## 2. Cumplimiento de los requisitos del caso
| Requisito | Estado |
|---|---|
| Técnicos registran traslados al iniciar y finalizar la visita | ✅ Hecho |
| Registro rápido y sencillo | ✅ Origen y kilometraje se proponen automáticamente; la hora la toma la app |
| Jefe valida los traslados de su equipo y revisa desviaciones | ✅ Hecho (filtro "Con desviación" y aviso en el inicio) |
| Gerencia y Finanzas consultan el costo por cliente (y técnico, y vehículo) | ✅ Hecho (solo traslados validados) |
| Administrador mantiene vehículos, destinos y tarifas | ✅ Hecho, además de tramos (distancia y peaje) y usuarios |
| Detectar desviación entre distancia oficial y declarada | ✅ Hecho (tolerancia 10 %) |
| Dispositivos Android | ✅ minSdk 26 |
| **Funcionar hasta 8 horas sin conexión sin perder datos** y sincronizar al recuperar señal | ❌ **Pendiente.** Los datos hoy viven en memoria: se pierden al cerrar la app. Requiere base de datos local (Room) y sincronización en segundo plano (WorkManager). |
| **Fuente pública de precios de combustible** | ❌ **Pendiente.** La tarifa la registra el administrador a mano (queda indicado como fuente). Hay que elegir la fuente pública y conectarla. |
| **Cálculo centralizado** | ⚠️ Parcial. Hay un único punto de cálculo, pero corre en el teléfono. Para que sea realmente central debe ejecutarse en un servidor; la app solo enviaría lo declarado. |
| **Protección de datos personales** | ⚠️ Parcial. Hecho: mínimo de datos, acceso por rol, contraseñas con hash. Falta: cifrado de los datos en el dispositivo, política de retención y aviso de privacidad. |
| Integración con el ERP | No requerida por el caso |

Marco legal a considerar: la Ley 19.628 está vigente; la Ley 21.719 tiene vigencia prevista para el 1 de diciembre de 2026,
aunque hay un proyecto en trámite para aplazarla un año. Conviene diseñar ya con sus principios (finalidad, minimización, seguridad).

## 3. Supuestos que conviene confirmar con el docente o el cliente
1. **Tolerancia de desviación del 10 %.** Es un valor propuesto; debería definirlo la empresa (hoy es una constante en `CalculadoraCostos`).
2. **El costo se calcula con la distancia oficial**, no con la declarada; la declarada se usa para detectar desviaciones.
3. **Vehículo particular:** el caso no define una compensación por kilómetro; se usa la misma fórmula con el rendimiento registrado.
4. **Un traslado es un tramo** (por ejemplo sede → clínica, clínica → clínica, clínica → sede). El tramo debe existir en el catálogo; si no, no se puede iniciar el traslado.
5. **Atribución a cliente:** el traslado se atribuye al cliente de destino; si el destino es la sede (regreso), al cliente de origen.
6. **Un técnico tiene un solo traslado en curso** a la vez y un vehículo asignado.
7. **Los técnicos no ven costos ni la distancia oficial**, para no sesgar lo que declaran.
8. Los nombres de clientes, distancias, peajes y precio del combustible precargados son ilustrativos.

## 4. Próximos pasos sugeridos
1. Persistencia local con Room (el `RepositorioDatos` es el punto de conexión) y cola de sincronización con WorkManager.
2. Definir el servidor y mover el cálculo de costos a él.
3. Elegir y conectar la fuente pública de precios de combustible.
4. Cifrado en el dispositivo y aviso de privacidad.
