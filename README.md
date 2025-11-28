# 📱 PlanIt – Aplicación móvil de gestión de tareas inteligentes



## 📌 Descripción del proyecto

PlanIt es una aplicación móvil desarrollada en Kotlin utilizando Jetpack Compose, cuyo propósito es la gestión eficiente de tareas personales mediante una arquitectura moderna, integración de servicios externos, persistencia local, uso de recursos nativos del dispositivo y validaciones avanzadas.  

Este proyecto fue desarrollado como parte de las Evaluaciones Parciales 2, 3 y 4 del programa formativo de desarrollo de aplicaciones móviles.



La aplicación permite registrar, editar, visualizar y eliminar tareas, utilizando mecanismos de control de estado reactivo y validaciones estructuradas, además de integrar datos externos de clima en tiempo real y generar un ejecutable firmado para su despliegue.



---



## 🏗 Arquitectura y organización (MVVM)



El proyecto fue desarrollado aplicando el patrón arquitectónico **Model – View – ViewModel (MVVM)**.



### Estructura general:





Este diseño desacopla la interfaz, lógica de negocio y almacenamiento, favoreciendo la mantenibilidad y escalabilidad del proyecto.



---



## 🎨 Diseño visual y navegación



La interfaz se desarrolló siguiendo principios de **Material Design 3**, integrando:



- Jerarquía visual clara.

- Formularios estructurados.

- Navegación coherente.

- Optimización para distintos tamaños de pantalla.

- Componentes reutilizables mediante Composables.



Pantallas implementadas:

- Login

- Registro

- Lista de Tareas

- Detalle de Tarea

- Edición

- Perfil

- Vista de Clima



---



## 📝 Formularios y validaciones



La aplicación implementa validación centralizada mediante la clase `TaskValidator`.



### Validaciones aplicadas:

- Título obligatorio.

- Control de fechas.

- Retroalimentación visual inmediata.

- Bloqueo de guardado si existen errores.



Cada campo tiene validación individual y mensajes de error personalizados, asegurando correcta captura de datos.



---



## ⚙ Gestión del estado



El manejo del estado se implementa con:



- `StateFlow` → Estado de pantalla.

- `SharedFlow` → Eventos únicos (snackbar, navegación).



Estados principales:

- `TaskUiState`

- `FormState`

- `WeatherUiState`



Esta arquitectura permite actualizaciones reactivas y comportamiento coherente entre la lógica interna y la interfaz visual.



---



## 🌍 Consumo de API externa – Clima



Se integró la API **OpenWeatherMap** utilizando **Ktor Client**.



### Flujo técnico:



### Estados:

- Idle

- Loading

- Success

- Error



La respuesta es procesada mediante `kotlinx.serialization` y mostrada de forma controlada en la UI.



---



## 💾 Persistencia local



Implementada mediante **Room Database**:



Entidades:

- TaskEntity

- User



Incluye:

- DAOs personalizados

- Migraciones de versiones

- Conversores automáticos (subtareas y prioridad)

- Almacenamiento persistente de datos



---



## 📷 Integración de recursos nativos



Se implementó acceso a:

- Cámara

- Grabación de audio

- Acceso a archivos

- Ubicación

- Autenticación biométrica



Todos los permisos se gestionan correctamente en tiempo de ejecución.



---



## 🧪 Pruebas unitarias



Se implementaron pruebas con JUnit:



Archivos:

- `TaskValidatorTest`

- `ExampleUnitTest`

- `ExampleInstrumentedTest`



Estas pruebas validan:

✔ comportamiento de validación  

✔ estabilidad de la lógica  

✔ correcto entorno de ejecución  



---



## 📦 APK firmado



Se generó:

- Archivo `.jks`

- APK firmado en modo Release

- Instalación verificada

- Documentación incluida



---



## 🛠 Tecnologías utilizadas



- Kotlin

- Jetpack Compose

- MVVM

- Room

- Ktor Client

- Coroutines

- Flow / StateFlow

- Gson

- JUnit

- Android Studio

- GitHub

- Trello

- OpenWeatherMap API



---



## ✅ Estado del proyecto



✔ Funcional  

✔ Integrado  

✔ Probado  

✔ Firmado  

✔ Documentado  



---



## 📎 Repositorio oficial

https://github.com/AguantelBarcelona/PlanIt



---



## 🧾 Conclusión

PlanIt es una aplicación completamente funcional que cumple con los indicadores técnicos IL2 e IL3 exigidos por la rúbrica de evaluación. Implementa arquitectura profesional, validaciones estructuradas, integración de servicios externos, persistencia local y despliegue de aplicación firmada.



---

