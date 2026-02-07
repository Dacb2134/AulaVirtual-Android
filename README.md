# 📱 Aula Virtual App (Android Client for Moodle)

![Platform](https://img.shields.io/badge/Platform-Android-green)
![Language](https://img.shields.io/badge/Language-Kotlin-orange)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-blue)


## 📖 Descripción del Proyecto

**Aula Virtual App** es una aplicación móvil nativa desarrollada en **Kotlin** que permite a los estudiantes gestionar su vida académica conectándose directamente a una plataforma LMS **Moodle** (versión 5.1 Alpha).

El proyecto soluciona la necesidad de movilidad estudiantil mediante una interfaz moderna y optimizada, implementando un sistema de **Autenticación Híbrida** (Google Sign-In + Token Moodle) que unifica la experiencia de acceso sin comprometer la seguridad ni la identidad del usuario en las entregas.

---

## 🚀 Características Principales

### 🔐 Autenticación & Seguridad
* **Google Sign-In:** Inicio de sesión rápido y seguro.
* **Password Swap Flow:** Mecanismo inteligente que vincula la cuenta de Google con el usuario de Moodle, garantizando que cada tarea se entregue con la identidad real del estudiante.
* **Gestión de Sesión:** Manejo automático de Tokens y expiración.

### 📚 Gestión Académica (Core)
* **Mis Cursos:** Visualización de cursos matriculados con barra de progreso.
* **Contenidos:** Acceso a recursos, PDFs y enlaces por módulos.
* **Tareas:**
    * Subida de archivos desde el almacenamiento local.
    * Validación de fechas de entrega (*Duedate*).
    * Estado de la entrega en tiempo real.
* **Foros:** Lectura de hilos y publicación de respuestas.
* **Calificaciones:** Tabla detallada de notas por actividad.

### ✨ Funcionalidades "Extra" (Valor Agregado)
* **📅 Agenda Académica:** Organizador visual con **filtros** por estado (Pendiente, Enviado, Calificado).
* **👤 Perfil Resumido:** Vista optimizada con información esencial del estudiante.
* **🏆 Gamificación:** Visualización de **Insignias (Badges)** ganadas en los cursos.
* **📂 Archivos Privados:** Acceso y gestión del repositorio personal en la nube de Moodle (*Private Files*).

---

## 🛠️ Arquitectura Técnica

El proyecto sigue los lineamientos de arquitectura moderna de Google:

* **Patrón:** MVVM (Model - View - ViewModel).
* **Lenguaje:** Kotlin 100%.
* **Inyección de Dependencias:** Manual (Repository Pattern).
* **Asincronía:** Kotlin Coroutines & LiveData.
* **Diseño:** XML Layouts + Material Design Components.

### Estructura de Paquetes
```text
com.practicas.aulavirtualapp
├── ui/              # Activities & Fragments (Vistas)
├── viewmodel/       # Lógica de presentación (State Holders)
├── repository/      # Single Source of Truth (Datos)
├── network/         # Retrofit Client & API Interfaces
├── model/           # Data Classes (DTOs)
├── adapter/         # RecyclerView Adapters
└── utils/           # Helpers (Roles, Colors, Extensions)
