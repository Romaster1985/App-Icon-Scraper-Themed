/*
 * Copyright 2025 Román Ignacio Romero (Romaster)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.romaster.appiconscrapper

import android.content.Context
import android.content.pm.PackageManager
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.os.Environment
import android.util.Log
import com.android.apksig.ApkSigner
import java.io.*
import java.security.KeyStore
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

object IconPackGenerator {
    
    private const val TAG = "IconPackGenerator"
    private const val MAX_ICONS = 2000
    
    data class IconPackConfig(
        val appName: String,
        val packageName: String = "com.romaster.iconpack.${System.currentTimeMillis()}"
    )
    
    interface ExportProgressListener {
        fun onProgressUpdate(step: String, progress: Int)
    }
    
    // Cache para máscaras
    private var selectedIconback: Bitmap? = null
    private var selectedIconmask: Bitmap? = null
    private var selectedIconupon: Bitmap? = null
    
    fun setSelectedIconback(iconback: Bitmap?) {
        selectedIconback = iconback?.copy(Bitmap.Config.ARGB_8888, false)
    }
    
    fun setSelectedIconmask(iconmask: Bitmap?) {
        selectedIconmask = iconmask?.copy(Bitmap.Config.ARGB_8888, false)
    }
    
    fun setSelectedIconupon(iconupon: Bitmap?) {
        selectedIconupon = iconupon?.copy(Bitmap.Config.ARGB_8888, false)
    }
    
    fun generateIconPackAPK(
        context: Context,
        themedIcons: Map<String, Bitmap>,
        config: IconPackConfig,
        listener: ExportProgressListener?
    ): File? {
        Log.d(TAG, "🚀 INICIANDO generación de APK con ${themedIcons.size} iconos")
        
        return try {
            listener?.onProgressUpdate("Iniciando generación...", 5)
            
            // PASO 1: Extraer template APK
            val templateAPK = getTemplateAPK(context)
            if (templateAPK == null) {
                Log.e(TAG, "❌ No se pudo cargar el template APK")
                return null
            }
            
            listener?.onProgressUpdate("Creando pack...", 20)
            val finalAPK = createIconPackAPK(context, templateAPK, config, themedIcons, listener)
            
            // Limpiar template temporal
            templateAPK.delete()
            finalAPK
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 ERROR generando APK", e)
            null
        }
    }
    
    private fun createIconPackAPK(
        context: Context,
        templateAPK: File,
        config: IconPackConfig,
        themedIcons: Map<String, Bitmap>,
        listener: ExportProgressListener?
    ): File? {
        return try {
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "${config.appName.replace(" ", "_")}_$timestamp.apk"
            
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) downloadsDir.mkdirs()
            
            val tempDir = File(context.cacheDir, "iconpack_temp_${System.currentTimeMillis()}")
            val unsignedAPK = File(context.cacheDir, "unsigned_$fileName")
            val alignedAPK = File(context.cacheDir, "aligned_$fileName")
            val finalAPK = File(downloadsDir, fileName)
    
            // PASO 1: Extraer template APK a directorio temporal
            listener?.onProgressUpdate("Extrayendo template...", 10)
            Log.d(TAG, "📦 Extrayendo template APK...")
            if (!extractTemplateAPK(templateAPK, tempDir)) {
                Log.e(TAG, "❌ Error extrayendo template APK")
                return null
            }
            Thread.sleep(500) // ⏰ DELAY ESTRATÉGICO 1
    
            // PASO 2: Copiar íconos procesados
            listener?.onProgressUpdate("Copiando íconos...", 30)
            Log.d(TAG, "🎨 Copiando ${themedIcons.size} íconos procesados...")
            if (!copyProcessedIcons(tempDir, themedIcons, listener)) {
                Log.e(TAG, "❌ Error copiando íconos procesados")
                return null
            }
            Thread.sleep(500) // ⏰ DELAY ESTRATÉGICO 2
    
            // PASO 3: Generar y copiar archivos XML
            listener?.onProgressUpdate("Generando configuraciones...", 60)
            Log.d(TAG, "📝 Generando archivos de configuración...")
            if (!generateConfigFiles(tempDir, themedIcons, config, context)) {
                Log.e(TAG, "❌ Error generando archivos de configuración")
                return null
            }
            
            // PASO 3.5: Generar appnofilter.xml con TODAS las actividades
            listener?.onProgressUpdate("Generando lista completa...", 65)
            Log.d(TAG, "📝 Generando appnofilter.xml...")
            if (!generateAppNoFilterXml(tempDir, themedIcons, config, context)) {
                Log.w(TAG, "⚠️ Error generando appnofilter.xml, continuando...")
            }
            Thread.sleep(500) // ⏰ DELAY ESTRATÉGICO 3
    
            // PASO 4: Comprimir a APK sin compresión (STORED)
            listener?.onProgressUpdate("Comprimiendo APK...", 80)
            Log.d(TAG, "📦 Comprimiendo a APK (STORED)...")
            if (!compressToAPK(tempDir, unsignedAPK)) {
                Log.e(TAG, "❌ Error comprimiendo APK")
                return null
            }
            Thread.sleep(300) // ⏰ DELAY ESTRATÉGICO 4
    
            // PASO 5: Alinear APK
            listener?.onProgressUpdate("Alineando APK...", 85)
            Log.d(TAG, "🔧 Alineando APK...")
            if (!alignAPK(unsignedAPK, alignedAPK)) {
                Log.w(TAG, "⚠️ No se pudo alinear APK, usando sin alinear")
                unsignedAPK.copyTo(alignedAPK, overwrite = true)
            }
            Thread.sleep(300) // ⏰ DELAY ESTRATÉGICO 5
    
            // PASO 6: Firmar APK
            listener?.onProgressUpdate("Firmando APK...", 90)
            Log.d(TAG, "✍️ Firmando APK...")
            if (!signAPKWithRealSignature(context, alignedAPK, finalAPK)) {
                Log.e(TAG, "❌ Error firmando APK")
                return null
            }
            Thread.sleep(300) // ⏰ DELAY ESTRATÉGICO 6
    
            // Limpiar archivos temporales
            tempDir.deleteRecursively()
            unsignedAPK.delete()
            alignedAPK.delete()
    
            // PASO 7: Verificar APK final
            if (finalAPK.exists() && finalAPK.length() > 102400) {
                Log.d(TAG, "✅ APK generado exitosamente: ${finalAPK.length()} bytes")
                listener?.onProgressUpdate("✅ Pack creado!", 100)
                finalAPK
            } else {
                Log.e(TAG, "❌ APK final inválido")
                null
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error en createIconPackAPK", e)
            null
        }
    }

    // PASO 1: Extraer template APK a directorio temporal
    private fun extractTemplateAPK(templateAPK: File, tempDir: File): Boolean {
        return try {
            if (tempDir.exists()) {
                tempDir.deleteRecursively()
            }
            tempDir.mkdirs()

            ZipFile(templateAPK).use { zip ->
                val entries = zip.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val outputFile = File(tempDir, entry.name)
                    
                    if (entry.isDirectory) {
                        outputFile.mkdirs()
                    } else {
                        outputFile.parentFile?.mkdirs()
                        FileOutputStream(outputFile).use { fos ->
                            zip.getInputStream(entry).copyTo(fos)
                        }
                    }
                }
            }
            
            Log.d(TAG, "✅ Template APK extraído a: ${tempDir.absolutePath}")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error extrayendo template APK", e)
            false
        }
    }

    // PASO 2: Copiar íconos procesados
    private fun copyProcessedIcons(
        tempDir: File,
        themedIcons: Map<String, Bitmap>,
        listener: ExportProgressListener?
    ): Boolean {
        return try {
            val iconsDir = File(tempDir, "res/drawable-nodpi-v4")
            if (!iconsDir.exists()) {
                iconsDir.mkdirs()
            }

            var progress = 0
            val total = minOf(themedIcons.size, MAX_ICONS)
            
            // Copiar íconos temáticos
            themedIcons.entries.take(MAX_ICONS).forEachIndexed { index, (packageName, bitmap) ->
                try {
                    if (bitmap.isRecycled) {
                        Log.w(TAG, "⚠️ Bitmap reciclado para: $packageName")
                        return@forEachIndexed
                    }
                    
                    val sequentialName = "ic_${"%04d".format(index + 1)}.png"
                    val iconFile = File(iconsDir, sequentialName)
                    
                    FileOutputStream(iconFile).use { fos ->
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    
                    progress++
                    if (progress % 10 == 0) {
                        Log.d(TAG, "📦 Progreso íconos: $progress/$total")
                        listener?.onProgressUpdate("Icono $progress/$total...", 30 + (progress * 30 / total))
                    }
                    
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error copiando ícono: $packageName", e)
                }
            }
            
            // Copiar máscaras personalizadas si existen
            copyCustomMasks(iconsDir)
            
            Log.d(TAG, "✅ Íconos copiados: $progress/$total")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en copyProcessedIcons", e)
            false
        }
    }

    // Copiar máscaras personalizadas
    private fun copyCustomMasks(iconsDir: File) {
        try {
            // Iconback
            selectedIconback?.let { iconback ->
                if (!iconback.isRecycled) {
                    File(iconsDir, "iconback.png").outputStream().use { fos ->
                        iconback.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    Log.d(TAG, "✅ Iconback personalizado copiado")
                }
            }
            
            // Iconmask
            selectedIconmask?.let { iconmask ->
                if (!iconmask.isRecycled) {
                    File(iconsDir, "iconmask.png").outputStream().use { fos ->
                        iconmask.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    Log.d(TAG, "✅ Iconmask personalizado copiado")
                }
            }
            
            // ✅ NUEVO: Iconupon
            selectedIconupon?.let { iconupon ->
                if (!iconupon.isRecycled) {
                    File(iconsDir, "iconupon.png").outputStream().use { fos ->
                        iconupon.compress(Bitmap.CompressFormat.PNG, 100, fos)
                    }
                    Log.d(TAG, "✅ Iconupon personalizado copiado")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error copiando máscaras personalizadas", e)
        }
    }

    // PASO 3: Generar archivos de configuración
    private fun generateConfigFiles(
        tempDir: File,
        themedIcons: Map<String, Bitmap>,
        config: IconPackConfig,
        context: Context
    ): Boolean {
        return try {
            val assetsDir = File(tempDir, "assets")
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }
            
            // Generar appfilter.xml
            val appfilterFile = File(assetsDir, "appfilter.xml")
            appfilterFile.writeText(generateAppFilterXml(themedIcons, config.appName, context))
            
            // Generar drawable.xml
            val drawableFile = File(assetsDir, "drawable.xml")
            drawableFile.writeText(generateDrawableXml(themedIcons, config.appName, context))
            
            Log.d(TAG, "✅ Archivos de configuración generados")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generando archivos de configuración", e)
            false
        }
    }

    // PASO 4: Comprimir a APK sin compresión (STORED)
    private fun compressToAPK(tempDir: File, outputAPK: File): Boolean {
        return try {
            FileOutputStream(outputAPK).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    // Comprimir todo el directorio recursivamente
                    compressDirectoryToZip(tempDir, tempDir, zos)
                }
            }
            
            Log.d(TAG, "✅ APK comprimido: ${outputAPK.length()} bytes")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error comprimiendo APK", e)
            false
        }
    }

    private fun compressDirectoryToZip(rootDir: File, currentDir: File, zos: ZipOutputStream) {
        try {
            currentDir.listFiles()?.forEach { file ->
                if (file.isDirectory) {
                    compressDirectoryToZip(rootDir, file, zos)
                } else {
                    val relativePath = rootDir.toURI().relativize(file.toURI()).path
                    val entry = ZipEntry(relativePath)
                    
                    // ✅ MANTENEMOS STORED pero con mejor manejo
                    entry.method = ZipEntry.STORED
                    entry.size = file.length()
                    entry.compressedSize = file.length()
                    
                    // Calcular CRC con verificación
                    val fileBytes = file.readBytes()
                    if (fileBytes.isEmpty()) {
                        Log.w(TAG, "⚠️ Archivo vacío: ${file.name}")
                    }
                    
                    val crc = java.util.zip.CRC32()
                    crc.update(fileBytes)
                    entry.crc = crc.value
                    
                    zos.putNextEntry(entry)
                    zos.write(fileBytes) // Usar bytes ya leídos
                    zos.closeEntry()
                    
                    Log.d(TAG, "📦 Comprimido: ${file.name} (${fileBytes.size} bytes)")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error comprimiendo directorio: ${currentDir.name}", e)
            throw e
        }
    }

    // PASO 5: Alinear APK
    private fun alignAPK(inputAPK: File, outputAPK: File): Boolean {
        return try {
            Log.d(TAG, "🔧 Iniciando proceso de alineamiento...")
            
            // Verificar si la librería está disponible
            if (!NativeZipAlign.isNativeLibraryAvailable()) {
                Log.w(TAG, "⚠️ Librería zipalign-android no disponible")
                // Fallback: copiar sin alinear
                inputAPK.copyTo(outputAPK, overwrite = true)
                return true
            }
            
            // Verificar si ya está alineado
            if (NativeZipAlign.checkAlignment(inputAPK)) {
                Log.d(TAG, "✅ APK ya está alineado, copiando...")
                inputAPK.copyTo(outputAPK, overwrite = true)
                return true
            }
            
            Log.d(TAG, "🔄 APK necesita alineamiento...")
            
            // Intentar alineamiento simple primero
            var success = NativeZipAlign.simpleAlign(inputAPK, outputAPK)
            
            if (!success) {
                Log.w(TAG, "⚠️ Alineamiento simple falló, intentando robusto...")
                success = NativeZipAlign.robustAlign(inputAPK, outputAPK)
            }
            
            if (success) {
                Log.d(TAG, "✅ Alineamiento completado exitosamente")
                // Verificar el resultado final
                val finalAligned = NativeZipAlign.checkAlignment(outputAPK)
                Log.d(TAG, "🔍 Alineamiento final verificado: $finalAligned")
            } else {
                Log.w(TAG, "⚠️ Todos los métodos de alineamiento fallaron, usando APK sin alinear")
                inputAPK.copyTo(outputAPK, overwrite = true)
            }
            
            true // Siempre retornar true para continuar con la firma
            
        } catch (e: Exception) {
            Log.e(TAG, "💥 Error en alignAPK", e)
            // Fallback seguro: copiar sin alinear
            try {
                inputAPK.copyTo(outputAPK, overwrite = true)
                true
            } catch (fallbackE: Exception) {
                Log.e(TAG, "💥 Error incluso en fallback", fallbackE)
                false
            }
        }
    }

    // Métodos auxiliares para generar XML
    private fun generateAppFilterXml(
        themedIcons: Map<String, Bitmap>,
        appName: String,
        context: Context
    ): String {
        val xmlBuilder = StringBuilder()
        
        xmlBuilder.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n")
        xmlBuilder.append("<resources>\n\n")
        
        // Icono del pack propio
        xmlBuilder.append("    <!-- Icono del pack $appName -->\n")
        xmlBuilder.append("    <item component=\"ComponentInfo{com.romaster.iconpacktemplate/.MainActivity}\" drawable=\"ic_themedpack\" />\n\n")
        
        // Iconos personalizados
        xmlBuilder.append("    <!-- Iconos personalizados ($appName) -->\n")
        
        themedIcons.entries.take(MAX_ICONS).forEachIndexed { index, (packageName, _) ->
            val sequentialName = "ic_${"%04d".format(index + 1)}"
            val appDisplayName = getAppDisplayName(packageName, context)
            val activities = getAppActivities(packageName, context)
            
            if (activities.isEmpty()) {
                Log.e(TAG, "❌ No hay actividades para $packageName, se omitirá")
                return@forEachIndexed
            }
            
            xmlBuilder.append("    <!-- $appDisplayName -->\n")
            
            activities.forEach { activity ->
                val component = if (activity == "*") {
                    "ComponentInfo{$packageName/*}"
                } else {
                    "ComponentInfo{$packageName/$activity}"
                }
                xmlBuilder.append("    <item component=\"$component\" drawable=\"$sequentialName\" />\n")
            }
            
            xmlBuilder.append("\n")
        }
        
        // Configuraciones
        xmlBuilder.append("    <scale factor=\"1.0\" />\n\n")
        xmlBuilder.append("    <!-- Configuración de máscaras -->\n")
        xmlBuilder.append("    <iconback img=\"iconback\" />\n")
        xmlBuilder.append("    <iconmask img=\"iconmask\" />\n")
        xmlBuilder.append("    <iconupon img=\"iconupon\" />\n")
        
        xmlBuilder.append("</resources>")
        
        return xmlBuilder.toString()
    }

    private fun generateDrawableXml(
        themedIcons: Map<String, Bitmap>,
        appName: String,
        context: Context
    ): String {
        val xmlBuilder = StringBuilder()
        
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n")
        xmlBuilder.append("<resources>\n")
        
        xmlBuilder.append("    <item name=\"ic_themedpack\" type=\"string\">Icon Pack</item>\n")
        xmlBuilder.append("    <item name=\"iconback\" type=\"string\">Fondo</item>\n")
        xmlBuilder.append("    <item name=\"iconmask\" type=\"string\">Máscara</item>\n")
        
        themedIcons.entries.take(MAX_ICONS).forEachIndexed { index, (packageName, _) ->
            val sequentialName = "ic_${"%04d".format(index + 1)}"
            val friendlyName = getAppDisplayName(packageName, context)
            
            xmlBuilder.append("    <item name=\"$sequentialName\" type=\"string\">$friendlyName</item>\n")
        }
        
        xmlBuilder.append("</resources>")
        
        return xmlBuilder.toString()
    }

    // Métodos auxiliares existentes
    private fun getAppDisplayName(packageName: String, context: Context): String {
        return try {
            val packageManager = context.packageManager
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val rawName = packageManager.getApplicationLabel(appInfo).toString()
            sanitizeForXml(rawName)
        } catch (e: Exception) {
            sanitizeForXml(generateDisplayNameFromPackage(packageName))
        }
    }

    private fun sanitizeForXml(text: String): String {
        return try {
            var sanitized = text
                .replace("&", " and ")
                .replace("[^a-zA-Z0-9\\s\\+\\-\\.:_|]".toRegex(), " ")
                .replace("\\s+".toRegex(), " ")
                .trim()

            if (sanitized.length > 35) {
                sanitized = sanitized.substring(0, 32) + "..."
            }

            if (sanitized.isEmpty() || sanitized.isBlank()) {
                "App"
            } else {
                sanitized
            }
        } catch (e: Exception) {
            "App"
        }
    }

    private fun generateDisplayNameFromPackage(packageName: String): String {
        return try {
            val parts = packageName.split('.')
            val mainName = parts.lastOrNull { 
                it.isNotEmpty() && it != "android" && it != "com" && it != "org" 
            } ?: parts.lastOrNull() ?: packageName
            
            mainName.replaceFirstChar { it.uppercase() }
        } catch (e: Exception) {
            packageName
        }
    }

    private fun getAppActivities(packageName: String, context: Context): List<String> {
        // ✅ NUEVO: CASOS ESPECIALES AGREGADOS AL INICIO
        val specialCases = mapOf(
            "com.teslacoilsw.launcherclientproxy" to listOf(
                "com.teslacoilsw.launcherclientproxy.BlankActivity",
                "com.teslacoilsw.launcherclientproxy.*",
                "*"
            ),
            "org.kustom.unread" to listOf(
                "org.kustom.unread.FirstActivity", 
                "org.kustom.unread.*",
                "*"
            ),
            "com.teslacoilsw.launcher" to listOf(
                "com.teslacoilsw.launcher.NovaShortcutHandler",
                "com.teslacoilsw.launcher.preferences.SettingsActivity",
                "com.teslacoilsw.launcher.*",
                "*"
            )
        )
        
        // Si es uno de nuestros casos especiales, usar estrategia agresiva
        specialCases[packageName]?.let { specialActivities ->
            Log.d(TAG, "🎯 Usando estrategia ESPECIAL para: $packageName")
            return specialActivities
        }
        
        // ✅ TODO EL RESTO DEL CÓDIGO ORIGINAL (QUE YA FUNCIONA PERFECTO)
        return try {
            val packageManager = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                `package` = packageName
            }
    
            // 1. OBTENER LA ACTIVIDAD LAUNCHER PRINCIPAL (igual que los launchers)
            val resolveInfo = packageManager.queryIntentActivities(mainIntent, 0)
            
            val launcherActivities = resolveInfo.mapNotNull { info ->
                info.activityInfo?.name
            }.distinct()
    
            // 2. SI ENCONTRAMOS ACTIVIDADES LAUNCHER, USARLAS
            if (launcherActivities.isNotEmpty()) {
                Log.d(TAG, "🎯 Actividades LAUNCHER encontradas para $packageName: $launcherActivities")
                
                val result = mutableListOf<String>()
                // Agregar actividades específicas encontradas
                result.addAll(launcherActivities.take(3)) // Máximo 3
                
                // Agregar comodín como respaldo
                result.add("*")
                
                return result
            }
    
            // 3. FALLBACK: USAR MAPA DE ACTIVIDADES ESPECÍFICAS
            val specificActivities = mapOf(
                "com.motorola.camera3" to "com.motorola.camera.Camera",
                "com.google.android.dialer" to "com.google.android.dialer.extensions.GoogleDialtactsActivity",
                "com.whatsapp" to "com.whatsapp.Main",
                "com.gbwhatsapp" to "com.gbwhatsapp.Icon0",
                "com.google.android.youtube" to "com.google.android.youtube.app.honeycomb.Shell*",
                "ginlemon.flowerfree" to "ginlemon.flower.HomeScreen",
                "com.teslacoilsw.launcherclientproxy" to "com.teslacoilsw.tesladirect.ChangeLogDialog",
                "org.kustom.unread" to "org.kustom.unread.FirstActivity",
                "com.google.android.googlequicksearchbox" to "com.google.android.googlequicksearchbox.SearchActivity",
                "com.android.vending" to "com.android.vending.AssetBrowserActivity",
                "com.google.android.deskclock" to "com.android.deskclock.DeskClock",
                "com.spotify.music" to "com.spotify.music.MainActivity",
                "com.twitter.android" to "com.twitter.android.StartActivity",
                "com.utorrent.client" to "com.bittorrent.app.main.MainActivity",
                "com.google.android.gm" to "com.google.android.gm.ConversationListActivityGmail",
                "com.google.android.apps.tachyon" to "com.google.android.apps.tachyon.MainActivity",
                "com.google.android.calculator" to "com.android.calculator2.Calculator",
                "com.google.android.street" to "com.google.android.libraries.streetview.main.StreetViewActivity",
                "com.google.android.videos" to "com.google.android.videos.GoogleTvEntryPoint",
                "io.github.muntashirakon.AppManager" to "io.github.muntashirakon.AppManager.main.SplashActivity",
                "com.google.android.apps.photos" to "com.google.android.apps.photos.home.HomeActivity",
                "com.facebook.katana" to "com.facebook.katana.LoginActivity",
                "nextapp.fx" to "nextapp.fx.ui.ExplorerActivity",
                "org.kustom.widget" to "org.kustom.widget.picker.WidgetPicker",
                "com.roamingsquirrel.android.calculator" to "com.roamingsquirrel.android.calculator.Calculate",
                "com.legend.hiwatchpro.app" to "xfkj.fitpro.activity.SplashActivity",
                "org.kivy.pygame" to "org.renpy.android.ProjectChooser",
                "org.greh.leftrightearnsoundtest" to "org.greh.leftrightearnsoundtest.LeftRightSndTest",
                "it.demi.elettronica.db.avr" to "it.demi.elettronica.db.mcu.Lista",
                "it.android.demi.elettronica.db.pic" to "it.demi.elettronica.db.mcu.Lista"
            )
    
            specificActivities[packageName]?.let { specificActivity ->
                Log.d(TAG, "📋 Usando actividad específica para $packageName: $specificActivity")
                return listOf(specificActivity, "*")
            }
    
            // 4. ÚLTIMO FALLBACK: Buscar en todas las actividades
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            val allActivities = packageInfo.activities?.map { it.name } ?: emptyList()
    
            if (allActivities.isEmpty()) {
                Log.w(TAG, "⚠️ No hay actividades para $packageName")
                return listOf("MainActivity", "*")
            }
    
            // Buscar actividad más probable
            val fallbackActivity = allActivities.firstOrNull { activityName ->
                activityName.contains("Main", ignoreCase = true) ||
                activityName.contains("Home", ignoreCase = true) ||
                activityName.contains("Launcher", ignoreCase = true) ||
                activityName.contains("Start", ignoreCase = true) ||
                !activityName.contains("settings", ignoreCase = true) &&
                !activityName.contains("debug", ignoreCase = true) &&
                !activityName.contains("test", ignoreCase = true) &&
                !activityName.contains("admin", ignoreCase = true)
            } ?: allActivities.first()
    
            Log.w(TAG, "🔄 Usando fallback heurístico para $packageName: $fallbackActivity")
            return listOf(fallbackActivity, "*")
    
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error obteniendo actividades para $packageName", e)
            // Fallback seguro que siempre funciona
            return listOf("MainActivity", "*")
        }
    }

    // Métodos existentes que mantienes
    private fun getTemplateAPK(context: Context): File? {
        return try {
            val templateFile = File(context.cacheDir, "template_${System.currentTimeMillis()}.apk")
            context.assets.open("base_icon_pack.apk").use { input ->
                FileOutputStream(templateFile).use { output ->
                    input.copyTo(output)
                }
            }
            Log.d(TAG, "✅ Template APK cargado: ${templateFile.length()} bytes")
            templateFile
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error cargando template APK", e)
            null
        }
    }

    private fun signAPKWithRealSignature(context: Context, inputApk: File, outputApk: File): Boolean {
        return try {
            val keyStore = KeyStore.getInstance("PKCS12")
            context.assets.open("iconpack.p12").use { input ->
                keyStore.load(input, "android".toCharArray())
            }
            val privateKey = keyStore.getKey("iconpack", "android".toCharArray()) as java.security.PrivateKey
            val certificate = keyStore.getCertificate("iconpack") as java.security.cert.X509Certificate
            
            val signerConfig = ApkSigner.SignerConfig.Builder("iconpack", privateKey, listOf(certificate)).build()
            val apkSigner = ApkSigner.Builder(listOf(signerConfig))
                .setV1SigningEnabled(true)
                .setV2SigningEnabled(true)
                .setV3SigningEnabled(true)
                .setMinSdkVersion(21)
                .setOutputApk(outputApk)
                .setInputApk(inputApk)
                .build()
            
            apkSigner.sign()
            Log.d(TAG, "✅ APK firmado exitosamente")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error en firma", e)
            false
        }
    }
    // Funciones para generar appnofilter.xml
    private fun generateAppNoFilterXml(
        tempDir: File,
        themedIcons: Map<String, Bitmap>,
        config: IconPackConfig,
        context: Context
    ): Boolean {
        return try {
            val assetsDir = File(tempDir, "assets")
            if (!assetsDir.exists()) {
                assetsDir.mkdirs()
            }
            
            val appNoFilterFile = File(assetsDir, "appnofilter.xml")
            appNoFilterFile.writeText(generateAppNoFilterXmlContent(themedIcons, config.appName, context))
            
            Log.d(TAG, "✅ appnofilter.xml generado con TODAS las actividades")
            true
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generando appnofilter.xml", e)
            false
        }
    }
    
    private fun generateAppNoFilterXmlContent(
        themedIcons: Map<String, Bitmap>,
        appName: String,
        context: Context
    ): String {
        val xmlBuilder = StringBuilder()
        
        xmlBuilder.append("<?xml version='1.0' encoding='UTF-8' standalone='yes' ?>\n")
        xmlBuilder.append("<resources>\n\n")
        
        xmlBuilder.append("    <!-- Todas las actividades disponibles para $appName -->\n")
        
        themedIcons.entries.take(MAX_ICONS).forEachIndexed { index, (packageName, _) ->
            val sequentialName = "ic_${"%04d".format(index + 1)}"
            val appDisplayName = getAppDisplayName(packageName, context)
            
            // ✅ OBTENER TODAS LAS ACTIVIDADES REALES (sin filtro)
            val allActivities = getAllActivitiesForPackageUnfiltered(packageName, context)
            
            xmlBuilder.append("    <!-- $appDisplayName - ${allActivities.size} actividades -->\n")
            
            allActivities.forEach { activity ->
                xmlBuilder.append("    <item component=\"ComponentInfo{$packageName/$activity}\" drawable=\"$sequentialName\" />\n")
            }
            
            xmlBuilder.append("\n")
        }
        
        xmlBuilder.append("</resources>")
        
        return xmlBuilder.toString()
    }
    
    // Función para obtener TODAS las actividades sin filtro
    private fun getAllActivitiesForPackageUnfiltered(packageName: String, context: Context): List<String> {
        return try {
            val packageManager = context.packageManager
            val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            packageInfo.activities?.map { it.name }?.distinct() ?: emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "Error obteniendo actividades para $packageName", e)
            emptyList()
        }
    }
}