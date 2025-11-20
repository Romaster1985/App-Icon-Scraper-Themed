[***👉🏻 README SP 🇪🇸 👈🏻***](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/main/README.md)
# 🎨 App Icon Scraper & Themer

[![Apache License 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg)](https://kotlinlang.org)
[![Android SDK](https://img.shields.io/badge/Android%20SDK-34-green.svg)](https://developer.android.com)
[![GitHub Releases](https://img.shields.io/github/v/release/Romaster1985/App-Icon-Scraper-Themed)](https://github.com/Romaster1985/App-Icon-Scraper-Themed/releases)

A complete Android application to extract, customize, and export professional multi-platform icon packs.

![image alt](https://github.com/Romaster1985/App-Icon-Scraper-Themed/blob/d7f3478c74f9ae569d34b729bcb57e338cdfb96b/app/src/main/res/ic_launcher-web.png?raw=true)

## ✨ Key Features

### 🎯 Scanning & Selection
* 📱 **Modern interface** - Material Design 3 with smooth user experience
* 🔍 **Smart scanning** - Detects and lists all installed applications
* 🎯 **Advanced filtering** - System, User, Google Apps, and more
* ✅ **Multi-selection** - Individual selection or "Select All" with real-time counters

### 🎨 Professional Customization
* 🎭 **Unique multilayer system** - Smart icon layer selection (default, round, foreground, background)
* 📐 **Independent rescaling** - Separate control for foreground layers with transparent-area auto-detection
* 🎛️ **Advanced adjustments** - Position, scale, transparency, tint, saturation, brightness, and contrast
* 🎨 **Color control** - Adjustable intensity with smooth blending into preset color
* 👁️ **Real-time preview** - Instant preview + full gallery of all generated icons

### 📦 Professional Export
* 🗜️ **Pack generation** - Creates custom icon packs ready to use
* 🤖 **Wide compatibility** - Works with Nova Launcher, Lawnchair, Smart Launcher, and more
* 🔧 **Auto alignment** - Native integration with zipalign-android for perfect APKs
* 📁 **Multiple formats** - Export as ZIP and APK (Recommended)

### 🛡️ Robustness & Quality
* 🤖 **Crash Guard Logger** - Self-diagnostics system for maximum stability
* 💾 **Smart cache** - Efficient memory and resource management
* 🌐 **Multilanguage support** - Spanish and English included
* 📊 **Optimized processing** - Efficient handling of large icon batches

## 🚀 What's New in This Version

### ✨ New Features
* **🎭 Upper Layer (iconupon)** - New layer for top-level custom effects
* **🔧 Native ZipAlign** - Professional integration with the official library by Muntashir Al-Islam
* **📄 Embedded licenses** - Full Apache 2.0 compliance
* **🎨 Reorganized UI** - More intuitive and efficient interface

### 🛠️ Technical Improvements
* **📦 Perfectly aligned APKs** - Guaranteed compatibility with all launchers
* **🎯 Centralized preview** - Bigger and clearer result view
* **⚡ Optimized performance** - Faster and more stable processing
* **🔧 Professional code** - Clean, maintainable architecture

## 🛠️ Technologies

* **Kotlin** - Main language with coroutines for async operations
* **Android SDK 34** - Modern APIs and extended compatibility
* **Material Design 3** - Modern and accessible UI
* **Architecture Components** - ViewModel, LiveData for robust architecture
* **RecyclerView** - Efficient lists and smooth scrolling
* **Gradle** - Modern build system with wrapper included
* **Active Internal Cache** - Smart memory management

## 📦 Used Dependencies

This project uses the following open-source libraries:

* **[zipalign-android](https://github.com/MuntashirAkon/zipalign-android)** by Muntashir Al-Islam - Licensed under Apache 2.0
    * Integrated via JitPack
    * Provides native APK alignment

## 🏗️ Building

### System Requirements
- **Android Studio** Hedgehog or newer
- **Android SDK** 34 (Android 14)
- **Java** 17 or newer
- **Gradle** 8.0+ (wrapper included)

### Build with Gradle (Wrapper included)

```
bash
git clone https://github.com/Romaster1985/App-Icon-Scraper-Themed.git
cd App-Icon-Scraper-Themed
# Grant execute permissions
chmod +x ./gradlew
## Option 1: Basic build debugging
./gradlew assembleDebug
## Option 2: With pre-cleanup
#./gradlew clean assembleDebug
## Option 3: With more detailed debugging
#./gradlew clean assembleDebug --stacktrace --info
# The APK is located in:
ls -la app/build/outputs/apk/debug/app-debug.apk
# To see the size and confirm successful build:
file app/build/outputs/apk/debug/app-debug.apk

```

GitHub Actions 🤖

The application is automatically compiled on each commit and pushed to the main/master branches using GitHub Actions.

Integration tests to ensure quality.

Artifacts are downloadable from the Actions tab.

# 📖 User Guide

## 🎯 Main Flow

📱 Scan Apps - Tap "Scan Apps" to list all applications

🎯 Filter and Select - Use the filters (All/System/User/Google Apps) and select applications

🔄 Theme - Tap "Theme" to access the professional editor

🎨 Customize - Configure masks, colors, and advanced settings

👁️ Preview - Use "Preview All" to see the complete result

📦 Export - Generate your pack as a ZIP or APK (ZIP file ready to import into Icon Packer)

## 🎨 Advanced Editor

🎭 Three Layers - Background (iconback), Mask (iconmask), Top Layer (iconupon)

🎛️ Precise Controls - Seek bars for all settings Real-time values

👁️ Interactive Preview - Click on the main preview to cycle through apps

⚡ Bulk Application - "Apply to All" processes all selected icons

## 🔐 Permissions

The application requires the following minimum permissions for optimal performance:

* QUERY_ALL_PACKAGES - To list installed applications

* WRITE_EXTERNAL_STORAGE - To save ZIP files (only up to Android 10)

* READ_EXTERNAL_STORAGE - To load custom images

* REQUEST_INSTALL_PACKAGES - To install generated APKs (optional)

## 📁 Project Structure

```
App-Icon-Scraper-Themed/
├── .github/workflows/ 		# 🤖 CI/CD with GitHub Actions
│ ├── android.yml 			# 🚀 Workflow App Main
│ └── build-base-apk.yml 	# 🚀 Workflow Template for the APK Pack
├── app/src/main/
│ ├── java/com/romaster/appiconscrapper/
│ │ ├── MainActivity.kt 				# 🏠 Main Activity
│ │ ├── ThemeCustomizationActivity.kt 	# 🎨 Advanced Editor
│ │ ├── IconPackGenerator.kt			# 📦 Pack Generator
│ │ ├── NativeZipAlign.kt 				# 🔧 APK Aligner
│ │ ├── IconThemer.kt 					# 🎭 Theming Engine
│ │ ├── IconScraper.kt					# 🔍 Icon Extractor
│ │ ├── IconPreviewActivity.kt 			# 👁️ Preview Gallery
│ │ ├── LicensesActivity.kt 			# 📄 Open-Source Licenses
│ │ ├── App.kt 							# 🤖 Crash Guardian
│ │ └── [other files] .kt] 				# 🛠️ Additional Components
│ ├── res/
│ │ ├── layout/ 						# 🎨 Layout Files
│ │ ├── values/ 						# 🌐 Strings and Resources
│ │ ├── values-en/ 						# 🏴󠁧󠁢󠁥󠁮󠁧󠁿 English Strings
│ │ └── assets/							# 📁 Licenses and Static Resources
│ └── AndroidManifest.xml 				# 📄 App Configuration
├── gradle/ 							# 🏗️ Build Configuration
├── [Configuration Files] 				# ⚙️ Project Configuration
└── IconPackBase/ 						# 📂 Working Folder for APK Template
```

## 👨🏻‍💻 Developer

**Román Ignacio Romero (Romaster)** 🇦🇷
Android Developer & System Tuning Enthusiast

📧 Email: roman.ignacio.romero@gmail.com

🐙 GitHub: [Romaster1985](https://github.com/Romaster1985)

💼 Portfolio: Developer specializing in system applications and customization

## 🙏 Acknowledgments

🤝 **Open Source Contributors**

**Muntashir Al-Islam** - For the excellent zipalign-android library

**Kotlin Community** - For the robust ecosystem and excellent documentation

🧠 **Development Assistance**
This application was developed with the assistance of **DeepSeek** for research and resolution of complex technical challenges.

📄 **License**

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
Note: This project includes zipalign-android licensed under Apache 2.0.

# 📲 Download the APK

* From [**RELEASES**](https://github.com/Romaster1985/App-Icon-Scraper-Themed/releases) (Recommended)
* From GitHub Actions, go to the latest workflow and download the generated artifact

## 🔗 Useful Links

- 🐛 [Report Issues](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) - Found a bug?
- 💡 [Suggest Features](https://github.com/Romaster1985/App-Icon-Scraper-Themed/issues) - Ideas for improving the app
- 💙 [Support me on Patreon **Romaster Android Tuning**](https://www.patreon.com/romasterdroidtuning?utm_campaign=creatorshare_creator)
- ☕ [You can also support me with a coffee](https://buymeacoffee.com/romaster)

---

⭐ **Do you like this project? Give it a star on GitHub to support its development!**