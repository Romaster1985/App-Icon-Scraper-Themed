[***👉🏻 README ENG 🇺🇸 👈🏻***](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/main/docs/README_EN.md)
# 🎨 App Icon Scraper & Themer

[![Apache License 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-34-green.svg)](https://developer.android.com)
[![GitHub Releases](https://img.shields.io/github/v/release/Romaster1985/App-Icon-Scraper-Themed)](https://github.com/Romaster1985/App-Icon-Scraper-Themed/releases)

Una aplicación Android completa para extraer, personalizar y exportar packs de iconos profesionales con compatibilidad multiplataforma.

![image alt](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/d7f3478c74f9ae569d34b729bcb57e338cdfb96b/app/src/main/res/ic_launcher-web.png?raw=true)

## ✨ Características Principales

### 🎯 Escaneo y Selección
* 📱 **Interfaz moderna** - Material Design 3 con experiencia de usuario fluida
* 🔍 **Escaneo inteligente** - Detecta y lista todas las aplicaciones instaladas
* 🎯 **Filtrado avanzado** - Sistema, Usuario, Google Apps y más
* ✅ **Selección múltiple** - Individual o "Seleccionar Todo" con contadores en tiempo real

### 🎨 Personalización Profesional
* 🎭 **Sistema multicapa único** - Selección inteligente de capas de iconos (default, round, foreground, background)
* 📐 **Reescalado independiente** - Control separado para capas frontales con detección automática de áreas transparentes
* 🎛️ **Ajustes avanzados** - Posición, escala, transparencia, tinte, saturación, brillo y contraste
* 🎨 **Control de color** - Intensidad ajustable con fundido a color preseteado
* 👁️ **Vista previa en tiempo real** - Preview inmediato + galería completa de todos los íconos

### 📦 Exportación Profesional
* 🗜️ **Generación de packs** - Crea packs de iconos personalizados listos para usar
* 🤖 **Compatibilidad amplia** - Funciona con Nova Launcher, Lawnchair, Smart Launcher y más
* 🔧 **Alineación automática** - Integración nativa con zipalign-android para APKs perfectos
* 📁 **Múltiples formatos** - Exportación a ZIP y APK (Recomendado)

### 🛡️ Robustez y Calidad
* 🤖 **Crash Guard Logger** - Sistema de autodiagnóstico para estabilidad máxima
* 💾 **Cache inteligente** - Gestión eficiente de memoria y recursos
* 🌐 **Soporte multidioma** - Español e Inglés integrados
* 📊 **Procesamiento optimizado** - Manejo eficiente de grandes cantidades de íconos

## 🚀 Novedades en esta Versión

### ✨ Características Nuevas
* **🎭 Capa Superior (iconupon)** - Nueva capa para efectos superiores personalizados
* **🔧 ZipAlign nativo** - Integración profesional con librería oficial de Muntashir Al-Islam
* **📄 Licencias integradas** - Cumplimiento completo con Apache 2.0
* **🎨 UI reorganizada** - Interfaz más intuitiva y eficiente

### 🛠️ Mejoras Técnicas
* **📦 APKs perfectamente alineados** - Compatibilidad garantizada con todos los launchers
* **🎯 Preview centralizado** - Vista de resultado prominente y clara
* **⚡ Rendimiento optimizado** - Procesamiento más rápido y estable
* **🔧 Código profesional** - Arquitectura limpia y mantenible

## 🛠️ Tecnologías

* **Kotlin** - Lenguaje principal con corrutinas para operaciones asíncronas
* **Android SDK 34** - APIs modernas y compatibilidad extendida
* **Material Design 3** - Interfaz de usuario moderna y accesible
* **Architecture Components** - ViewModel, LiveData para arquitectura robusta
* **RecyclerView** - Listas eficientes y scroll suave
* **Gradle** - Build system moderno con wrapper incluido
* **Active Internal Cache** - Gestión inteligente de memoria

## 📦 Dependencias Utilizadas

Este proyecto utiliza las siguientes librerías open-source:

* **[zipalign-android](https://github.com/MuntashirAkon/zipalign-android)** por Muntashir Al-Islam - Licensed under Apache 2.0
  * Integrado via JitPack según recomendación oficial del autor
  * Proporciona alineación nativa de APKs para compatibilidad perfecta

## 🏗️ Compilación

### Requisitos del Sistema
- **Android Studio** Hedgehog o superior
- **Android SDK** 34 (Android 14)
- **Java** 17 o superior
- **Gradle** 8.0+ (wrapper incluido)

### Build con Gradle (Wrapper Incluido)

```
bash
git clone https://github.com/Romaster1985/App-Icon-Scraper-Themed.git
cd App-Icon-Scraper-Themed
# Dar permisos de ejecución
chmod +x ./gradlew
## Opción 1: Compilación básica debug
./gradlew assembleDebug
## Opción 2: Con limpieza previa
#./gradlew clean assembleDebug
## Opción 3: Con más información de depuración
#./gradlew clean assembleDebug --stacktrace --info
# El APK estará en:
ls -la app/build/outputs/apk/debug/app-debug.apk
# Para ver el tamaño y confirmar que se generó correctamente
file app/build/outputs/apk/debug/app-debug.apk

```

GitHub Actions 🤖

La aplicación se compila automáticamente en cada commit & push a las ramas main/master mediante GitHub Actions.

Tests de integración para garantizar calidad

Artifacts descargables desde la pestaña Actions

# 📖 Guía de Uso

## 🎯 Flujo Principal

📱 Escanear Apps - Presiona "Escanear Apps" para listar todas las aplicaciones

🎯 Filtrar y Seleccionar - Usa los filtros (Todas/Sistema/Usuario/Google Apps) y selecciona aplicaciones

🔄 Tematizar - Presiona "Tematizar" para acceder al editor profesional

🎨 Personalizar - Configura máscaras, colores y ajustes avanzados

👁️ Previsualizar - Usa "Previsualizar Todos" para ver el resultado completo

📦 Exportar - Genera tu pack en ZIP o APK (Archivo ZIP listo para importar en Icon Packer)

## 🎨 Editor Avanzado

🎭 Tres Capas - Fondo (iconback), Máscara (iconmask), Capa Superior (iconupon)

🎛️ Controles Precisos - Seekbars para todos los ajustes con valores en tiempo real

👁️ Preview Interactivo - Click en el preview principal para ciclar entre apps

⚡ Aplicación Masiva - "Aplicar a Todos" procesa todos los íconos seleccionados

## 🔐 Permisos

La aplicación requiere permisos mínimos para funcionamiento óptimo:

* QUERY_ALL_PACKAGES - Para listar aplicaciones instaladas

* WRITE_EXTERNAL_STORAGE - Para guardar archivos ZIP (solo hasta Android 10)

* READ_EXTERNAL_STORAGE - Para cargar imágenes personalizadas

* REQUEST_INSTALL_PACKAGES - Para instalar APKs generados (opcional)

## 📁 Estructura del Proyecto

```
App-Icon-Scraper-Themed/
├── .github/workflows/          # 🤖 CI/CD con GitHub Actions
│   ├── android.yml				# 🚀 Workflow App Principal
│   └── build-base-apk.yml		# 🚀 Workflow Plantilla para el Pack APK
├── app/src/main/
│   ├── java/com/romaster/appiconscrapper/
│   │   ├── MainActivity.kt               # 🏠 Actividad principal
│   │   ├── ThemeCustomizationActivity.kt # 🎨 Editor avanzado
│   │   ├── IconPackGenerator.kt          # 📦 Generador de packs
│   │   ├── NativeZipAlign.kt             # 🔧 Alineador de APKs
│   │   ├── IconThemer.kt                 # 🎭 Motor de tematización
│   │   ├── IconScraper.kt                # 🔍 Extractor de iconos
│   │   ├── IconPreviewActivity.kt        # 👁️ Galería de preview
│   │   ├── LicensesActivity.kt           # 📄 Licencias open-source
│   │   ├── App.kt						  # 🤖 Crash Guardian
│   │   └── [otros archivos .kt]          # 🛠️ Componentes adicionales
│   ├── res/
│   │   ├── layout/              # 🎨 Archivos de diseño
│   │   ├── values/              # 🌐 Strings y recursos
│   │   ├── values-en/           # 🏴󠁧󠁢󠁥󠁮󠁧󠁿 Strings en inglés
│   │   └── assets/              # 📁 Licencias y recursos estáticos
│   └── AndroidManifest.xml      # 📄 Configuración de la app
├── gradle/                      # 🏗️ Configuración de build
├── [archivos de configuración]  # ⚙️ Configuración del proyecto
└── IconPackBase/				 # 📂 Carpeta de trabajo para la Plantilla APK
```

## 👨🏻‍💻 Desarrollador

**Román Ignacio Romero (Romaster)** 🇦🇷
Desarrollador Android & Entusiasta del Tuning de Sistemas

📧 Email: roman.ignacio.romero@gmail.com

🐙 GitHub: [Romaster1985](https://github.com/Romaster1985)

💼 Portafolio: Desarrollador especializado en aplicaciones de sistema y personalización

## 🙏 Agradecimientos

🤝 **Contribuidores de Código Abierto**

**Muntashir Al-Islam** - Por la excelente librería zipalign-android

**Comunidad Kotlin** - Por el ecosistema robusto y documentación excelente

🧠 **Asistencia de Desarrollo**
Esta aplicación fue desarrollada con la asistencia de **DeepSeek** para investigación y resolución de desafíos técnicos complejos.

📄 **Licencia**

Copyright 2025 Román Ignacio Romero (Romaster)

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
Nota: Este proyecto incluye zipalign-android licenciado bajo Apache 2.0.

# 📲 Descarga de la aplicación APK

* Desde [**RELEASES**](https://github.com/Romaster1985/App-Icon-Scraper-Themed/releases) (Recomendado)
* Desde GitHub Actions, entra en el último workflow y descarga el Artifact generado

## 🔗 Enlaces Útiles

- 🐛 [Reportar Issues](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) - ¿Encontraste un error?
- 💡 [Sugerir Features](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) - Ideas para mejorar la app
- 💙​ [Apóyame en Patreon **Romaster Android Tuning**](https://www.patreon.com/romasterdroidtuning?utm_campaign=creatorshare_creator)
- ☕ [También puedes apoyarme con un café](https://buymeacoffee.com/romaster)

---

⭐ **¿Te gusta este proyecto? ¡Dale una estrella en GitHub para apoyar su desarrollo!**