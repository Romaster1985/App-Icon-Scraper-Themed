[***👉🏻 README ENG 🇺🇸 👈🏻***](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/f06470d4cff0f2c8020a53ecd831ef9211af3997/docs/README_EN.md)
# App Icon Scraper & Themer

Una aplicación Android para extraer, personalizar y exportar iconos de aplicaciones instaladas en el dispositivo. Diseñada principalmente para cargar y aplicar las imágenes de los íconos del propio dispositivo en la aplicación Icon Packer sin necesidad de depender de fuentes externas.

![image alt](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/d7f3478c74f9ae569d34b729bcb57e338cdfb96b/app/src/main/res/ic_launcher-web.png?raw=true)

## Características

* 📱 Interfaz de usuario simple y profesional.
* 🔍Escanea y 📋 lista todas las aplicaciones instaladas.
* 🎯 Filtrado inteligente (Sistema, Usuario, Google Apps).
* ✅ Selección múltiple de aplicaciones.
* 🎨 Sistema único de tematización de iconos (v1.2⬆️ agregados iconos multicapa).
* 📍 Ajuste de posición, escala, transparencia, tinte, saturación, brillo y contraste.
* 🎛️ Control de intensidad de color (fundido a color preseteado).
* 👁️ Vista previa en tiempo real + vista previa de todos los íconos (v1.2⬆️).
* 📦 Generación de packs de iconos personalizados.
* 🗜️Exportación a archivo ZIP listo para cargar en Icon Packer.
* 🤖📝 Crash Guard Loger incorporado para autodiagnóstico de errores ⛓️‍💥💥 (v1.2⬆️).

## Tecnologías:

* Kotlin
* Android SDK
* Material Design 3
* RecyclerView
* ViewModel (v1.2 ⬆️)
* Gradle
* Active Internal Cache (v1.2 ⬆️)

## Compilación

### Requisitos
- Android Studio Hedgehog o superior
- Android SDK 34
- Java 17

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

GitHub Actions

La aplicación se compila automáticamente en cada push a las ramas main/master mediante GitHub Actions.

## Uso

1. Abre la aplicación y presiona "Escanear Apps"
2. Usa los filtros para encontrar las aplicaciones deseadas
3. Selecciona las aplicaciones (individualmente o usando "Seleccionar Todo")
4. Presiona "Tematizar"
5. Selecciona la máscara/fondo para los íconos (png)
6. Elige un color, intensidad, transparencia, capas, etc. y ajusta los íconos dentro de la máscara a tu gusto
7. Presiona "Aplicar a Todos Los Íconos" / Opcional Previsualiza Todos los Íconos Seleccionados
8. Presiona "Exportar Pack de Íconos"
9. Los iconos se guardarán en un archivo ZIP en la carpeta de descargas del dispositivo
10. El pack generado es compatible con Icon Packer

## Permisos

La aplicación requiere:

* QUERY_ALL_PACKAGES: Para listar aplicaciones instaladas
* WRITE_EXTERNAL_STORAGE: Para guardar el archivo ZIP (solo hasta Android 10)

## Estructura del Proyecto

```
App-Icon-Scraper-Themed
├── .github
│   └── workflows
│       └── android.yml
├── LICENSE
├── README.md
├── app
│   ├── build.gradle
│   ├── proguard-rules.pro
│   └── src
│       └── main
│           ├── AndroidManifest.xml
│           ├── java
│           │   └── com
│           │       └── romaster
│           │           └── appiconscrapper
│           │               ├── AboutActivity.kt
│           │               ├── App.kt (🤖 Crash Guardian)
│           │               ├── AppAdapter.kt
│           │               ├── AppInfo.kt
│           │               ├── IconCache.kt
│           │               ├── IconPreviewActivity.kt
│           │               ├── IconScraper.kt
│           │               ├── IconThemer.kt
│           │               ├── MainActivity.kt
│           │               ├── MainActivityViewModel.kt
│           │               ├── ThemeCustomizationActivity.kt
│           │               └── ThemeCustomizationViewModel.kt
│           └── res
│               ├── drawable
│               │   ├── bg_card.xml
│               │   ├── bg_card_selected.xml
│               │   ├── button_primary.xml
│               │   ├── ic_about.xml
│               │   ├── ic_export.xml
│               │   ├── ic_filter.xml
│               │   ├── ic_launcher_foreground.xml
│               │   └── logo_romaster.png
│               ├── drawable-hdpi
│               │   ├── badge_google.png
│               │   └── badge_system.png
│               ├── drawable-mdpi
│               │   ├── badge_google.png
│               │   └── badge_system.png
│               ├── drawable-xhdpi
│               │   ├── badge_google.png
│               │   └── badge_system.png
│               ├── ic_launcher-web.png
│               ├── layout
│               │   ├── activity_about.xml
│               │   ├── activity_icon_preview.xml
│               │   ├── activity_main.xml
│               │   ├── activity_theme_customization.xml
│               │   ├── item_app.xml
│               │   ├── item_icon_preview.xml
│               │   └── tab_layout.xml
│               ├── menu
│               │   └── main_menu.xml
│               ├── mipmap-anydpi-v26
│               │   ├── ic_launcher.xml
│               │   └── ic_launcher_round.xml
│               ├── mipmap-hdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_background.png
│               │   ├── ic_launcher_foreground.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-ldpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_background.png
│               │   ├── ic_launcher_foreground.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-mdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_background.png
│               │   ├── ic_launcher_foreground.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-xhdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_background.png
│               │   ├── ic_launcher_foreground.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-xxhdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_background.png
│               │   ├── ic_launcher_foreground.png
│               │   └── ic_launcher_round.png
│               ├── mipmap-xxxhdpi
│               │   ├── ic_launcher.png
│               │   ├── ic_launcher_background.png
│               │   ├── ic_launcher_foreground.png
│               │   └── ic_launcher_round.png
│               ├── playstore-icon.png
│               ├── values
│               │   ├── colors.xml
│               │   ├── ic_launcher_background.xml
│               │   ├── strings.xml
│               │   └── themes.xml
│               └── xml
│                   └── file_paths.xml
├── build.gradle
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradle.properties
├── gradlew
├── gradlew.bat
└── settings.gradle

```

## Desarrollador

**Romaster** 🇦🇷

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
- ☕[También puedes apoyarme con un café](buymeacoffee.com/romaster)

---

⭐ **¿Te gustó este proyecto? Dale una estrella en GitHub!**
