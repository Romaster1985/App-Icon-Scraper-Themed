[***👉🏻 README SP 🇪🇸 👈🏻***](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/main/README.md)
# App Icon Scraper & Themer

An Android application to extract, customize, and export icons from installed apps on your device. It is mainly designed to load and apply icons from your own device into the **Icon Packer** app without relying on external sources.

![image alt](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/d7f3478c74f9ae569d34b729bcb57e338cdfb96b/app/src/main/res/ic_launcher-web.png?raw=true)

## Features

* 📱 Simple and professional user interface.
* 🔍📋 Scans and lists all installed applications.
* 🎯 Smart filtering (System, User, Google Apps).
* ✅ Multiple app selection.
* 🎨 Unique icon theming system with multi-layer icon selection.
* 📐✂️ Independent rescaling and centering of front layers with intelligent transparent area detection and cropping.
* 📍 Position, scale, transparency, tint, saturation, brightness, and contrast adjustment (independent scaling for front layers).
* 🎛️ Color intensity control (fade to preset color).
* 👁️ Real-time preview + preview of all icons.
* 📦 Custom icon pack generation.
* 🗜️ Export to ZIP file ready for loading in Icon Packer.
* 🤖📝 Built-in Crash Guard Logger for automatic error diagnosis ⛓️‍💥💥.

## Technologies

* Kotlin
* Android SDK
* Material Design 3
* RecyclerView
* ViewModel
* Gradle
* Active Internal Cache

## Build Instructions

### Requirements
- Android Studio Hedgehog or higher
- Android SDK 34
- Java 17

### Build with Gradle (Wrapper Included)

```
bash
git clone https://github.com/Romaster1985/App-Icon-Scraper-Themed.git
cd App-Icon-Scraper-Themed
# Grant execution permissions
chmod +x ./gradlew
## Option 1: Basic debug build
./gradlew assembleDebug
## Option 2: Clean and rebuild
#./gradlew clean assembleDebug
## Option 3: With detailed debug output
#./gradlew clean assembleDebug --stacktrace --info
# The APK will be located at:
ls -la app/build/outputs/apk/debug/app-debug.apk
# To verify size and confirm proper generation
file app/build/outputs/apk/debug/app-debug.apk
```

### GitHub Actions

The app is automatically built on every push to the main/master branches via GitHub Actions.

## Usage

1. Open the app and press **“Scan Apps”**
2. Use filters to find the desired apps
3. Select the apps (individually or “Select All”)
4. Press **“Theme”**
5. Choose the mask/background for the icons (PNG)
6. Select color, intensity, transparency, layers, etc., and adjust icons inside the mask to your preference
7. Press **“Apply to All Icons”** / Optionally preview all themed icons
8. Press **“Export Icon Pack”**
9. Icons will be saved as a ZIP file in your device’s Downloads folder
10. The generated pack is fully compatible with **Icon Packer**

## Permissions

The app requires:

* `QUERY_ALL_PACKAGES`: To list installed apps
* `WRITE_EXTERNAL_STORAGE`: To save the ZIP file (up to Android 10 only)

## Project Structure

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

## Developer

**Romaster** 🇦🇷

* Email: roman.ignacio.romero@gmail.com
* GitHub: [Romaster1985](https://github.com/Romaster1985)

## Acknowledgements

This application was designed with the help of **DeepSeek**.

## License

MIT License – see the LICENSE file for details.

# 📲 APK Download

* From [**RELEASES**](https://github.com/Romaster1985/App-Icon-Scraper-Themed/releases) (Recommended)
* From GitHub Actions — open the latest workflow and download the generated artifact

## 🔗 Useful Links

- 🐛 [Report Issues](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) – Found a bug?
- 💡 [Suggest Features](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) – Share your ideas for improvements
- 🫂 [Support me on Patreon **Romaster Android Tuning**](https://www.patreon.com/romasterdroidtuning?utm_campaign=creatorshare_creator)
- ☕ [Support me with a cofee](buymeacoffee.com/romaster)

---

⭐ **Did you like this project? Give it a star on GitHub!**