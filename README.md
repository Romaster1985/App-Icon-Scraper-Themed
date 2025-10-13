# App Icon Scraper

Una aplicación Android para extraer y exportar iconos de aplicaciones instaladas en el dispositivo.

## Características

- 📱 Lista todas las aplicaciones instaladas
- 🎯 Filtrado por tipo (Sistema, Usuario, Google Apps)
- ✅ Selección múltiple de aplicaciones
- 📦 Exportación a archivo ZIP
- 🎨 Interfaz de usuario moderna y profesional

## Tecnologías

- Kotlin
- Android SDK
- Material Design 3
- RecyclerView
- Gradle

## Compilación

### Requisitos
- Android Studio Hedgehog o superior
- Android SDK 34
- Java 17

### Build con Gradle

```
bash
./gradlew build

```

GitHub Actions

La aplicación se compila automáticamente en cada push a las ramas main/master mediante GitHub Actions.

Uso

1. Abre la aplicación
2. Usa los filtros para encontrar las aplicaciones deseadas
3. Selecciona las aplicaciones (individualmente o usando "Seleccionar Todo")
4. Presiona "Exportar Seleccionados"
5. Los iconos se guardarán en un archivo ZIP en el almacenamiento interno

Permisos

La aplicación requiere:

· QUERY_ALL_PACKAGES: Para listar aplicaciones instaladas
· WRITE_EXTERNAL_STORAGE: Para guardar el archivo ZIP (solo hasta Android 10)

Estructura del Proyecto

```
app/
├── src/main/
│   ├── java/com/romaster/appiconscrapper/
│   │   ├── MainActivity.kt
│   │   ├── AboutActivity.kt
│   │   ├── AppInfo.kt
│   │   ├── AppAdapter.kt
│   │   └── IconScraper.kt
│   ├── res/
│   └── AndroidManifest.xml
└── build.gradle

```

Desarrollador

Romaster

· Email: roman.ignacio.romero@gmail.com
· GitHub: romaster

Agradecimientos

Esta aplicación fue diseñada con la ayuda de DeepSeek.

Licencia

MIT License - ver archivo LICENSE para más detalles.
