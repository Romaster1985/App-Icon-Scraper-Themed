package com.romaster.appiconscrapper

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import org.json.JSONObject
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object ThemingSerializer {

    data class ThemingConfig(
        val basicSettings: BasicSettings,
        val advancedFilters: AdvancedFilters,
        val iconbackBitmap: Bitmap? = null,
        val iconmaskBitmap: Bitmap? = null,
        val iconuponBitmap: Bitmap? = null,
        val iconclippingmaskBitmap: Bitmap? = null
    )

    data class BasicSettings(
        val offsetX: Int,
        val offsetY: Int,
        val scalePercentage: Int,
        val foregroundScalePercentage: Int,
        val alphaPercentage: Int,
        val colorIntensity: Int,
        val selectedColor: Int,
        val hue: Float,
        val saturation: Float,
        val brightness: Float,
        val contrast: Float,
        val useDefaultIcon: Boolean,
        val useRoundIcon: Boolean,
        val useForegroundLayer: Boolean,
        val useBackgroundLayer: Boolean
    )

    data class AdvancedFilters(
        val advancedFiltersEnabled: Boolean,
        val edgeEnhanceEnabled: Boolean,
        val edgeEnhanceIntensity: Float,
        val chromaticAberrationEnabled: Boolean,
        val chromaticIntensity: Float,
        val chromaticRedOffset: Int,
        val chromaticGreenOffset: Int,
        val chromaticBlueOffset: Int,
        val sphereEffectEnabled: Boolean,
        val sphereStrength: Float,
        val embossEffectEnabled: Boolean,
        val embossIntensity: Float,
        val embossAzimuth: Float,
        val glowEffectEnabled: Boolean,
        val glowIntensity: Float,
        val glowRadius: Int,
        val softMaskEnabled: Boolean,
        val softMaskIntensity: Float,
        val rotationEnabled: Boolean,
        val rotationAngle: Float,
        val shadowEnabled: Boolean,
        val shadowIntensity: Float,
        val shadowRadius: Int,
        val shadowOffsetX: Int,
        val shadowOffsetY: Int,
        val borderEnabled: Boolean,
        val borderInnerWidth: Int,
        val borderOuterWidth: Int,
        val borderColor: Int,
        val pixelateEnabled: Boolean,
        val pixelateSize: Int,
        val cartoonEnabled: Boolean,
        val cartoonIntensity: Float,
        val noiseEnabled: Boolean,
        val noiseIntensity: Float,
        val fisheyeEnabled: Boolean,
        val fisheyeStrength: Float,
        val maskEnabled: Boolean,
        val maskScalePercentage: Int,
        val imageScaleEnabled: Boolean,
        val imageScalePercentage: Int,
        val iconColorizationEnabled: Boolean,
        val iconColorizationColor: Int,
        val iconColorizationIntensity: Int,
        val iconmaskShapeEnabled: Boolean
    )

    // ✅ EXPORTAR tematización a ZIP
    fun exportTheming(
        viewModel: ThemeCustomizationViewModel,
        iconback: Bitmap?,
        iconmask: Bitmap?,
        iconupon: Bitmap?,
        iconclippingmask: Bitmap?,
        name: String = "Tematización Personalizada",
        author: String = "Usuario", 
        description: String = "Configuración exportada desde App Icon Scraper & Themer",
        outputStream: OutputStream
    ): Boolean {
        return try {
            ZipOutputStream(outputStream).use { zos ->
                // 1. Exportar configuración JSON CON METADATOS
                val configJson = createConfigJson(viewModel, name, author, description)
                val configEntry = ZipEntry("config.json")
                zos.putNextEntry(configEntry)
                zos.write(configJson.toByteArray())
                zos.closeEntry()
    
                // 2. Exportar imágenes (igual que antes)
                exportBitmapToZip(iconback, "iconback.png", zos)
                exportBitmapToZip(iconmask, "iconmask.png", zos)
                exportBitmapToZip(iconupon, "iconupon.png", zos)
                exportBitmapToZip(iconclippingmask, "iconclippingmask.png", zos)
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    // ✅ IMPORTAR tematización desde ZIP
    fun importTheming(inputStream: InputStream): ThemingConfig? {
        return try {
            val zipInputStream = ZipInputStream(inputStream)
            var entry: ZipEntry?
            val bitmaps = mutableMapOf<String, Bitmap>()
            var configJson: JSONObject? = null

            while (zipInputStream.nextEntry.also { entry = it } != null) {
                when (entry!!.name) {
                    "config.json" -> {
                        configJson = readJsonFromZip(zipInputStream)
                    }
                    "iconback.png", "iconmask.png", "iconupon.png", "iconclippingmask.png" -> {
                        val bitmap = readBitmapFromZip(zipInputStream)
                        if (bitmap != null) {
                            bitmaps[entry!!.name] = bitmap
                        }
                    }
                }
                zipInputStream.closeEntry()
            }
            zipInputStream.close()

            if (configJson != null) {
                parseConfigJson(configJson!!, bitmaps)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createConfigJson(
        viewModel: ThemeCustomizationViewModel, 
        name: String, 
        author: String, 
        description: String
    ): String {
        val jsonObject = JSONObject().apply {
            put("version", "1.0")
            put("name", name)
            put("author", author)
            put("description", description)
            put("created_date", SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()))
            
            // Configuración básica
            putJSONObject("basic_settings") {
                put("offsetX", viewModel.offsetX)
                put("offsetY", viewModel.offsetY)
                put("scalePercentage", viewModel.scalePercentage)
                put("foregroundScalePercentage", viewModel.foregroundScalePercentage)
                put("alphaPercentage", viewModel.alphaPercentage)
                put("colorIntensity", viewModel.colorIntensity)
                put("selectedColor", viewModel.selectedColor)
                put("hue", viewModel.hue.toDouble())
                put("saturation", viewModel.saturation.toDouble())
                put("brightness", viewModel.brightness.toDouble())
                put("contrast", viewModel.contrast.toDouble())
                put("useDefaultIcon", viewModel.useDefaultIcon)
                put("useRoundIcon", viewModel.useRoundIcon)
                put("useForegroundLayer", viewModel.useForegroundLayer)
                put("useBackgroundLayer", viewModel.useBackgroundLayer)
            }
            
            // Filtros avanzados
            putJSONObject("advanced_filters") {
                put("advancedFiltersEnabled", viewModel.advancedFiltersEnabled)
                put("edgeEnhanceEnabled", viewModel.edgeEnhanceEnabled)
                put("edgeEnhanceIntensity", viewModel.edgeEnhanceIntensity.toDouble())
                put("chromaticAberrationEnabled", viewModel.chromaticAberrationEnabled)
                put("chromaticIntensity", viewModel.chromaticIntensity.toDouble())
                put("chromaticRedOffset", viewModel.chromaticRedOffset)
                put("chromaticGreenOffset", viewModel.chromaticGreenOffset)
                put("chromaticBlueOffset", viewModel.chromaticBlueOffset)
                put("sphereEffectEnabled", viewModel.sphereEffectEnabled)
                put("sphereStrength", viewModel.sphereStrength.toDouble())
                put("embossEffectEnabled", viewModel.embossEffectEnabled)
                put("embossIntensity", viewModel.embossIntensity.toDouble())
                put("embossAzimuth", viewModel.embossAzimuth.toDouble())
                put("glowEffectEnabled", viewModel.glowEffectEnabled)
                put("glowIntensity", viewModel.glowIntensity.toDouble())
                put("glowRadius", viewModel.glowRadius)
                put("softMaskEnabled", viewModel.softMaskEnabled)
                put("softMaskIntensity", viewModel.softMaskIntensity.toDouble())
                put("rotationEnabled", viewModel.rotationEnabled)
                put("rotationAngle", viewModel.rotationAngle.toDouble())
                put("shadowEnabled", viewModel.shadowEnabled)
                put("shadowIntensity", viewModel.shadowIntensity.toDouble())
                put("shadowRadius", viewModel.shadowRadius)
                put("shadowOffsetX", viewModel.shadowOffsetX)
                put("shadowOffsetY", viewModel.shadowOffsetY)
                put("borderEnabled", viewModel.borderEnabled)
                put("borderInnerWidth", viewModel.borderInnerWidth)
                put("borderOuterWidth", viewModel.borderOuterWidth)
                put("borderColor", viewModel.borderColor)
                put("pixelateEnabled", viewModel.pixelateEnabled)
                put("pixelateSize", viewModel.pixelateSize)
                put("cartoonEnabled", viewModel.cartoonEnabled)
                put("cartoonIntensity", viewModel.cartoonIntensity.toDouble())
                put("noiseEnabled", viewModel.noiseEnabled)
                put("noiseIntensity", viewModel.noiseIntensity.toDouble())
                put("fisheyeEnabled", viewModel.fisheyeEnabled)
                put("fisheyeStrength", viewModel.fisheyeStrength.toDouble())
                put("maskEnabled", viewModel.maskEnabled)
                put("maskScalePercentage", viewModel.maskScalePercentage)
                put("imageScaleEnabled", viewModel.imageScaleEnabled)
                put("imageScalePercentage", viewModel.imageScalePercentage)
                put("iconColorizationEnabled", viewModel.iconColorizationEnabled)
                put("iconColorizationColor", viewModel.iconColorizationColor)
                put("iconColorizationIntensity", viewModel.iconColorizationIntensity)
                put("iconmaskShapeEnabled", viewModel.iconmaskShapeEnabled)
            }
        }
        
        // ✅ FORMATEAR JSON PARA LEGIBILIDAD
        return jsonObject.toString(4) // Indentación de 4 espacios
    }

    private fun JSONObject.putJSONObject(key: String, block: JSONObject.() -> Unit) {
        put(key, JSONObject().apply(block))
    }

    private fun exportBitmapToZip(bitmap: Bitmap?, filename: String, zos: ZipOutputStream) {
        bitmap?.let {
            val entry = ZipEntry(filename)
            zos.putNextEntry(entry)
            val byteArrayOutputStream = ByteArrayOutputStream()
            it.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            zos.write(byteArrayOutputStream.toByteArray())
            zos.closeEntry()
            byteArrayOutputStream.close()
        }
    }

    private fun readJsonFromZip(zipInputStream: ZipInputStream): JSONObject {
        val byteArrayOutputStream = ByteArrayOutputStream()
        val buffer = ByteArray(1024)
        var length: Int
        while (zipInputStream.read(buffer).also { length = it } > 0) {
            byteArrayOutputStream.write(buffer, 0, length)
        }
        val jsonString = byteArrayOutputStream.toString()
        byteArrayOutputStream.close()
        return JSONObject(jsonString)
    }

    private fun readBitmapFromZip(zipInputStream: ZipInputStream): Bitmap? {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            val buffer = ByteArray(1024)
            var length: Int
            while (zipInputStream.read(buffer).also { length = it } > 0) {
                byteArrayOutputStream.write(buffer, 0, length)
            }
            val bitmapBytes = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()
            BitmapFactory.decodeByteArray(bitmapBytes, 0, bitmapBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun parseConfigJson(json: JSONObject, bitmaps: Map<String, Bitmap>): ThemingConfig {
        val basicJson = json.getJSONObject("basic_settings")
        val advancedJson = json.getJSONObject("advanced_filters")

        val basicSettings = BasicSettings(
            offsetX = basicJson.getInt("offsetX"),
            offsetY = basicJson.getInt("offsetY"),
            scalePercentage = basicJson.getInt("scalePercentage"),
            foregroundScalePercentage = basicJson.getInt("foregroundScalePercentage"),
            alphaPercentage = basicJson.getInt("alphaPercentage"),
            colorIntensity = basicJson.getInt("colorIntensity"),
            selectedColor = basicJson.getInt("selectedColor"),
            hue = basicJson.getDouble("hue").toFloat(),
            saturation = basicJson.getDouble("saturation").toFloat(),
            brightness = basicJson.getDouble("brightness").toFloat(),
            contrast = basicJson.getDouble("contrast").toFloat(),
            useDefaultIcon = basicJson.getBoolean("useDefaultIcon"),
            useRoundIcon = basicJson.getBoolean("useRoundIcon"),
            useForegroundLayer = basicJson.getBoolean("useForegroundLayer"),
            useBackgroundLayer = basicJson.getBoolean("useBackgroundLayer")
        )

        val advancedFilters = AdvancedFilters(
            advancedFiltersEnabled = advancedJson.getBoolean("advancedFiltersEnabled"),
            edgeEnhanceEnabled = advancedJson.getBoolean("edgeEnhanceEnabled"),
            edgeEnhanceIntensity = advancedJson.getDouble("edgeEnhanceIntensity").toFloat(),
            chromaticAberrationEnabled = advancedJson.getBoolean("chromaticAberrationEnabled"),
            chromaticIntensity = advancedJson.getDouble("chromaticIntensity").toFloat(),
            chromaticRedOffset = advancedJson.getInt("chromaticRedOffset"),
            chromaticGreenOffset = advancedJson.getInt("chromaticGreenOffset"),
            chromaticBlueOffset = advancedJson.getInt("chromaticBlueOffset"),
            sphereEffectEnabled = advancedJson.getBoolean("sphereEffectEnabled"),
            sphereStrength = advancedJson.getDouble("sphereStrength").toFloat(),
            embossEffectEnabled = advancedJson.getBoolean("embossEffectEnabled"),
            embossIntensity = advancedJson.getDouble("embossIntensity").toFloat(),
            embossAzimuth = advancedJson.getDouble("embossAzimuth").toFloat(),
            glowEffectEnabled = advancedJson.getBoolean("glowEffectEnabled"),
            glowIntensity = advancedJson.getDouble("glowIntensity").toFloat(),
            glowRadius = advancedJson.getInt("glowRadius"),
            softMaskEnabled = advancedJson.getBoolean("softMaskEnabled"),
            softMaskIntensity = advancedJson.getDouble("softMaskIntensity").toFloat(),
            rotationEnabled = advancedJson.getBoolean("rotationEnabled"),
            rotationAngle = advancedJson.getDouble("rotationAngle").toFloat(),
            shadowEnabled = advancedJson.getBoolean("shadowEnabled"),
            shadowIntensity = advancedJson.getDouble("shadowIntensity").toFloat(),
            shadowRadius = advancedJson.getInt("shadowRadius"),
            shadowOffsetX = advancedJson.getInt("shadowOffsetX"),
            shadowOffsetY = advancedJson.getInt("shadowOffsetY"),
            borderEnabled = advancedJson.getBoolean("borderEnabled"),
            borderInnerWidth = advancedJson.getInt("borderInnerWidth"),
            borderOuterWidth = advancedJson.getInt("borderOuterWidth"),
            borderColor = advancedJson.getInt("borderColor"),
            pixelateEnabled = advancedJson.getBoolean("pixelateEnabled"),
            pixelateSize = advancedJson.getInt("pixelateSize"),
            cartoonEnabled = advancedJson.getBoolean("cartoonEnabled"),
            cartoonIntensity = advancedJson.getDouble("cartoonIntensity").toFloat(),
            noiseEnabled = advancedJson.getBoolean("noiseEnabled"),
            noiseIntensity = advancedJson.getDouble("noiseIntensity").toFloat(),
            fisheyeEnabled = advancedJson.getBoolean("fisheyeEnabled"),
            fisheyeStrength = advancedJson.getDouble("fisheyeStrength").toFloat(),
            maskEnabled = advancedJson.getBoolean("maskEnabled"),
            maskScalePercentage = advancedJson.getInt("maskScalePercentage"),
            imageScaleEnabled = advancedJson.getBoolean("imageScaleEnabled"),
            imageScalePercentage = advancedJson.getInt("imageScalePercentage"),
            iconColorizationEnabled = advancedJson.getBoolean("iconColorizationEnabled"),
            iconColorizationColor = advancedJson.getInt("iconColorizationColor"),
            iconColorizationIntensity = advancedJson.getInt("iconColorizationIntensity"),
            iconmaskShapeEnabled = advancedJson.getBoolean("iconmaskShapeEnabled")
        )

        return ThemingConfig(
            basicSettings = basicSettings,
            advancedFilters = advancedFilters,
            iconbackBitmap = bitmaps["iconback.png"],
            iconmaskBitmap = bitmaps["iconmask.png"],
            iconuponBitmap = bitmaps["iconupon.png"],
            iconclippingmaskBitmap = bitmaps["iconclippingmask.png"]
        )
    }
}