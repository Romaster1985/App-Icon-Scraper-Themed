# App Icon Scraper & Themer

Una aplicación Android para extraer, personalizar y exportar iconos de aplicaciones instaladas en el dispositivo.

## Características

* 📱 Interfaz de usuario simple y profesional
* 🔍Escanea y 📋 lista todas las aplicaciones instaladas
* 🎯 Filtrado inteligente (Sistema, Usuario, Google Apps)
* ✅ Selección múltiple de aplicaciones
* 🎨 Sistema único de tematización de iconos (agregados iconos multicapa)
* 📍 Ajuste de posición, escala, transparencia, tinte, saturación, brillo y contraste
* 🎛️ Control de intensidad de color (fundido a color preseteado)
* 👁️ Vista previa en tiempo real + vista previa de todos los íconos
* 📦 Generación de packs de iconos personalizados
* 🗜️Exportación a archivo ZIP

## Tecnologías:

* Kotlin
* Android SDK
* Material Design 3
* RecyclerView
* Gradle

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

## Uso

1. Abre la aplicación y presiona "Escanear Apps"
2. Usa los filtros para encontrar las aplicaciones deseadas
3. Selecciona las aplicaciones (individualmente o usando "Seleccionar Todo")
4. Presiona "Tematizar"
5. Selecciona la máscara/fondo para los íconos (png)
6. Elige un color, intensidad, transparencia, capas, etc. y ajusta los íconos dentro de la máscara a tu gusto
7. Presiona "Aplicar a Todos Los Íconos"
7b. Previsualiza todos los íconos seleccionados
8. Presiona "Exportar Pack de Íconos"
9. Los iconos se guardarán en un archivo ZIP en la carpeta de descargas del dispositivo
10. El pack generado es compatible con Icon Packer

## Permisos

La aplicación requiere:

· QUERY_ALL_PACKAGES: Para listar aplicaciones instaladas
· WRITE_EXTERNAL_STORAGE: Para guardar el archivo ZIP (solo hasta Android 10)

## Estructura del Proyecto

```
app/
├── src/main/
│   ├── java/com/romaster/appiconscrapper/
│   │   ├── MainActivity.kt
│   │   ├── AboutActivity.kt
│   │   ├── AppInfo.kt
│   │   ├── IconThemer.kt
│   │   ├── AppAdapter.kt
│   │   ├── ThemeCustomizationActivity.kt
│   │   ├── IconPreviewActivity.kt
│   │   └── IconScraper.kt
│   ├── res/
│   │   ├── layout/*.xml
│   │   ├── drawable/*.xml *.png
│   │   ├── mipmap-*/iconos de app (png) (xml)
│   │   ├── values/*.xml
│   │   ├── drawable-(m;h;xh)dpi/*.png
│   │   ├── xml/file_paths.xml
│   │   └── menu/main_menu.xml
│   └── AndroidManifest.xml
├── xproguard-rules.pro
└── build.gradle

```

## Desarrollador

**Romaster**

· Email: roman.ignacio.romero@gmail.com
· GitHub: [Romaster1985](https://github.com/Romaster1985)

## Agradecimientos

Esta aplicación fue diseñada con la ayuda de DeepSeek.

## Licencia

MIT License - ver archivo LICENSE para más detalles.

# 📲 Descarga de la aplicación APK

* Desde [**RELEASES**](https://github.com/Romaster1985/App-Icon-Scraper-Themed/releases) (Recomendado)
* Desde GitHub Actions, entra en el último workflow y descarga el Artifact generado

## 🔗 Enlaces Útiles

- 🐛 [Reportar Issues](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) - ¿Encontraste un error?
- 💡 [Sugerir Features](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) - Ideas para mejorar la app
- 🫂 [Apóyame en Patreon **Romaster Android Tuning**](https://www.patreon.com/romasterdroidtuning?utm_campaign=creatorshare_creator)

---

⭐ **¿Te gustó este proyecto? Dale una estrella en GitHub!**
