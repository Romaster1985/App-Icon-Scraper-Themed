package com.romaster.appiconscrapper

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class ThemeCustomizationActivity : AppCompatActivity() {

    private var currentPreviewBitmap: Bitmap? = null
    private var isColorApplied = false
    private lateinit var maskPreview: ImageView
    private lateinit var iconPreview: ImageView
    private lateinit var selectMaskButton: Button
    private lateinit var colorPickerButton: Button
    private lateinit var seekBarX: SeekBar
    private lateinit var seekBarY: SeekBar
    private lateinit var seekBarScale: SeekBar
    private lateinit var seekBarAlpha: SeekBar
    private lateinit var seekBarColorIntensity: SeekBar
    private lateinit var applyButton: Button
    private lateinit var exportButton: Button
    private lateinit var previewAllButton: Button
    private lateinit var xValueText: TextView
    private lateinit var yValueText: TextView
    private lateinit var scaleValueText: TextView
    private lateinit var alphaValueText: TextView
    private lateinit var colorIntensityValueText: TextView
    private lateinit var progressText: TextView

    // Controles de ajuste de imagen
    private lateinit var seekBarHue: SeekBar
    private lateinit var seekBarSaturation: SeekBar
    private lateinit var seekBarBrightness: SeekBar
    private lateinit var seekBarContrast: SeekBar
    private lateinit var hueValueText: TextView
    private lateinit var saturationValueText: TextView
    private lateinit var brightnessValueText: TextView
    private lateinit var contrastValueText: TextView

    // Controles de capas mejorados
    private lateinit var useDefaultIconCheckbox: CheckBox
    private lateinit var useRoundIconCheckbox: CheckBox
    private lateinit var foregroundLayerCheckbox: CheckBox
    private lateinit var backgroundLayerCheckbox: CheckBox
    private lateinit var layerInfoText: TextView

    private var selectedMask: Bitmap? = null
    private var selectedColor: Int = android.graphics.Color.CYAN
    private var offsetX: Int = 0
    private var offsetY: Int = 0
    private var scalePercentage: Int = 100
    private var alphaPercentage: Int = 100
    private var colorIntensity: Int = 100
    
    // Parámetros de ajuste de imagen
    private var hue: Float = 0f
    private var saturation: Float = 1f
    private var brightness: Float = 0f
    private var contrast: Float = 1f

    // Configuración de capas
    private var useDefaultIcon: Boolean = true
    private var useRoundIcon: Boolean = false
    private var useForegroundLayer: Boolean = true
    private var useBackgroundLayer: Boolean = true

    private lateinit var selectedApps: List<AppInfo>
    private var sampleIcon: Bitmap? = null
    private var sampleAppPackage: String = ""
    private val themedIcons = mutableMapOf<String, Bitmap>()
    private val previewIconsList = mutableListOf<Bitmap>()

    // Para la secuencia de preview
    private var currentPreviewIndex = 0
    private var currentPreviewApp: AppInfo? = null

    private lateinit var viewModel: ThemeCustomizationViewModel
    
    private lateinit var seekBarForegroundScale: SeekBar
    private lateinit var foregroundScaleValueText: TextView
    private var foregroundScalePercentage: Int = 100
    private var isForegroundPreprocessed: Boolean = false

    companion object {
        private const val PICK_MASK_REQUEST = 1001
        private const val TAG = "ThemeCustomization"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_customization)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_customization)

        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(ThemeCustomizationViewModel::class.java)

        // Obtener apps seleccionadas solo si es la primera vez
        if (viewModel.selectedApps.isEmpty()) {
            viewModel.selectedApps = intent.getParcelableArrayListExtra<AppInfo>("selected_apps") ?: emptyList()
        }

        selectedApps = viewModel.selectedApps

        // Restaurar estado desde ViewModel
        restoreStateFromViewModel()

        // Verificar si venimos de pre-procesamiento
        isForegroundPreprocessed = intent.getBooleanExtra("foreground_preprocessed", false)

        initViews()
        setupListeners()
        loadSampleIcon()
        setupPreviewCycle()
        updateLayerInfo()
        updateButtonStyles()

        // Restaurar máscara si existe
        if (selectedMask != null) {
            maskPreview.setImageBitmap(selectedMask)
        }

        // Restaurar color del botón
        colorPickerButton.setBackgroundColor(selectedColor)

        // Si el procesamiento ya estaba completo, habilitar botones
        if (viewModel.isProcessingComplete) {
            exportButton.isEnabled = true
            previewAllButton.isEnabled = true
            updateButtonStyles()
        }
        
        // Actualizar controles con valores guardados
        updateSeekBars()
        updatePreview()
        
        // Forzar actualización de visibilidad
        updateForegroundScaleVisibility()
    }
    
    private fun updateForegroundScaleVisibility() {
        val shouldEnable = isForegroundPreprocessed && 
                        useForegroundLayer && 
                        !useBackgroundLayer && 
                        !useDefaultIcon && 
                        !useRoundIcon
        
        if (shouldEnable) {
            // Habilitar controles de foreground
            seekBarForegroundScale.isEnabled = true
            seekBarForegroundScale.alpha = 1.0f
            foregroundScaleValueText.alpha = 1.0f
            
            // Habilitar controles de escala normal
            seekBarScale.isEnabled = true
            seekBarScale.alpha = 1.0f
            scaleValueText.alpha = 1.0f
        } else {
            // Deshabilitar controles de foreground
            seekBarForegroundScale.isEnabled = false
            seekBarForegroundScale.alpha = 0.5f
            foregroundScaleValueText.alpha = 0.5f
            
            // Habilitar controles de escala normal
            seekBarScale.isEnabled = true
            seekBarScale.alpha = 1.0f
            scaleValueText.alpha = 1.0f
        }
    }

    private fun restoreStateFromViewModel() {
        selectedMask = viewModel.selectedMask
        selectedColor = viewModel.selectedColor
        offsetX = viewModel.offsetX
        offsetY = viewModel.offsetY
        scalePercentage = viewModel.scalePercentage
        alphaPercentage = viewModel.alphaPercentage
        colorIntensity = viewModel.colorIntensity
        hue = viewModel.hue
        saturation = viewModel.saturation
        brightness = viewModel.brightness
        contrast = viewModel.contrast
        useDefaultIcon = viewModel.useDefaultIcon
        useRoundIcon = viewModel.useRoundIcon
        useForegroundLayer = viewModel.useForegroundLayer
        useBackgroundLayer = viewModel.useBackgroundLayer
        
        // Restaurar íconos procesados
        themedIcons.clear()
        themedIcons.putAll(viewModel.themedIcons)
        previewIconsList.clear()
        previewIconsList.addAll(viewModel.previewIconsList)
    }

    private fun saveStateToViewModel() {
        viewModel.selectedMask = selectedMask
        viewModel.selectedColor = selectedColor
        viewModel.offsetX = offsetX
        viewModel.offsetY = offsetY
        viewModel.scalePercentage = scalePercentage
        viewModel.alphaPercentage = alphaPercentage
        viewModel.colorIntensity = colorIntensity
        viewModel.hue = hue
        viewModel.saturation = saturation
        viewModel.brightness = brightness
        viewModel.contrast = contrast
        viewModel.useDefaultIcon = useDefaultIcon
        viewModel.useRoundIcon = useRoundIcon
        viewModel.useForegroundLayer = useForegroundLayer
        viewModel.useBackgroundLayer = useBackgroundLayer
        
        // Guardar íconos procesados
        viewModel.themedIcons.clear()
        viewModel.themedIcons.putAll(themedIcons)
        viewModel.previewIconsList.clear()
        viewModel.previewIconsList.addAll(previewIconsList)
    }

    private fun initViews() {
        maskPreview = findViewById(R.id.maskPreview)
        iconPreview = findViewById(R.id.iconPreview)
        selectMaskButton = findViewById(R.id.selectMaskButton)
        colorPickerButton = findViewById(R.id.colorPickerButton)
        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)
        seekBarScale = findViewById(R.id.seekBarScale)
        seekBarAlpha = findViewById(R.id.seekBarAlpha)
        seekBarColorIntensity = findViewById(R.id.seekBarColorIntensity)
        applyButton = findViewById(R.id.applyButton)
        exportButton = findViewById(R.id.exportButton)
        previewAllButton = findViewById(R.id.previewAllButton)
        xValueText = findViewById(R.id.xValueText)
        yValueText = findViewById(R.id.yValueText)
        scaleValueText = findViewById(R.id.scaleValueText)
        alphaValueText = findViewById(R.id.alphaValueText)
        colorIntensityValueText = findViewById(R.id.colorIntensityValueText)
        progressText = findViewById(R.id.progressText)

        // Inicializar controles de ajuste de imagen
        seekBarHue = findViewById(R.id.seekBarHue)
        seekBarSaturation = findViewById(R.id.seekBarSaturation)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        seekBarContrast = findViewById(R.id.seekBarContrast)
        hueValueText = findViewById(R.id.hueValueText)
        saturationValueText = findViewById(R.id.saturationValueText)
        brightnessValueText = findViewById(R.id.brightnessValueText)
        contrastValueText = findViewById(R.id.contrastValueText)

        // Inicializar controles de capas
        useDefaultIconCheckbox = findViewById(R.id.useDefaultIconCheckbox)
        useRoundIconCheckbox = findViewById(R.id.useRoundIconCheckbox)
        foregroundLayerCheckbox = findViewById(R.id.foregroundLayerCheckbox)
        backgroundLayerCheckbox = findViewById(R.id.backgroundLayerCheckbox)
        layerInfoText = findViewById(R.id.layerInfoText)

        // Inicializar controles de foreground scale
        seekBarForegroundScale = findViewById(R.id.seekBarForegroundScale)
        foregroundScaleValueText = findViewById(R.id.foregroundScaleValueText)
        
        // Configurar rangos existentes
        seekBarX.max = 200
        seekBarY.max = 200
        seekBarScale.max = 200
        seekBarAlpha.max = 100
        seekBarColorIntensity.max = 100
        
        // Configurar rangos de nuevos controles
        seekBarHue.max = 360
        seekBarSaturation.max = 200
        seekBarBrightness.max = 200
        seekBarContrast.max = 200
        
        // Configurar seekbar de foreground
        seekBarForegroundScale.max = 200
        seekBarForegroundScale.progress = foregroundScalePercentage
        
        seekBarForegroundScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                foregroundScalePercentage = progress
                foregroundScaleValueText.text = getString(R.string.foreground_scale, foregroundScalePercentage)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        // Inicialmente deshabilitar botones
        previewAllButton.isEnabled = false
        exportButton.isEnabled = false
    }

    private fun updateSeekBars() {
        seekBarX.progress = offsetX + 100
        seekBarY.progress = offsetY + 100
        seekBarScale.progress = scalePercentage
        seekBarAlpha.progress = alphaPercentage
        seekBarColorIntensity.progress = colorIntensity
        seekBarHue.progress = (hue + 180).toInt()
        seekBarSaturation.progress = (saturation * 100).toInt()
        seekBarBrightness.progress = (brightness + 100).toInt()
        seekBarContrast.progress = (contrast * 100).toInt()
        
        useDefaultIconCheckbox.isChecked = useDefaultIcon
        useRoundIconCheckbox.isChecked = useRoundIcon
        foregroundLayerCheckbox.isChecked = useForegroundLayer
        backgroundLayerCheckbox.isChecked = useBackgroundLayer
    }

    private fun setupListeners() {
        selectMaskButton.setOnClickListener {
            selectMaskImage()
        }

        colorPickerButton.setOnClickListener {
            showColorPickerDialog()
        }

        // Listeners para seekbars existentes
        setupSeekBarListeners()
        
        // Listeners para configuración de capas
        useDefaultIconCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useDefaultIcon = isChecked
            viewModel.useDefaultIcon = useDefaultIcon
            if (isChecked) {
                useRoundIconCheckbox.isChecked = false
                useRoundIcon = false
                viewModel.useRoundIcon = useRoundIcon
            }
            updateForegroundScaleVisibility()
            updatePreview()
            updateLayerInfo()
        }

        useRoundIconCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useRoundIcon = isChecked
            viewModel.useRoundIcon = useRoundIcon
            if (isChecked) {
                useDefaultIconCheckbox.isChecked = false
                useDefaultIcon = false
                viewModel.useDefaultIcon = useDefaultIcon
            }
            updateForegroundScaleVisibility()
            updatePreview()
            updateLayerInfo()
        }

        foregroundLayerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useForegroundLayer = isChecked
            viewModel.useForegroundLayer = useForegroundLayer
            updateForegroundScaleVisibility()
            updatePreview()
            updateLayerInfo()
        }

        backgroundLayerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useBackgroundLayer = isChecked
            viewModel.useBackgroundLayer = useBackgroundLayer
            updateForegroundScaleVisibility()
            updatePreview()
            updateLayerInfo()
        }

        previewAllButton.setOnClickListener {
            if (viewModel.hasConfigChanged() || previewIconsList.isEmpty()) {
                Toast.makeText(this, "Aplicando cambios antes de previsualizar...", Toast.LENGTH_SHORT).show()
                processAllIcons {
                    previewAllIconsAfterProcessing()
                }
            } else {
                previewAllIconsAfterProcessing()
            }
        }

        applyButton.setOnClickListener {
            processAllIcons()
        }

        exportButton.setOnClickListener {
            exportThemedIcons()
        }
    }

    private fun previewAllIconsAfterProcessing() {
        try {
            IconCache.iconsProcessed = previewIconsList
            val intent = Intent(this, IconPreviewActivity::class.java)
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error al abrir la previsualización: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupSeekBarListeners() {
        seekBarX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                offsetX = progress - 100
                viewModel.offsetX = offsetX
                xValueText.text = getString(R.string.position_x, offsetX)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                offsetY = progress - 100
                viewModel.offsetY = offsetY
                yValueText.text = getString(R.string.position_y, offsetY)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scalePercentage = progress
                viewModel.scalePercentage = scalePercentage
                scaleValueText.text = getString(R.string.scale, scalePercentage)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                alphaPercentage = progress
                viewModel.alphaPercentage = alphaPercentage
                alphaValueText.text = getString(R.string.transparency, alphaPercentage)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarColorIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                colorIntensity = progress
                viewModel.colorIntensity = colorIntensity
                colorIntensityValueText.text = getString(R.string.color_intensity, colorIntensity)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Listeners para ajustes de imagen
        seekBarHue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hue = (progress - 180).toFloat()
                viewModel.hue = hue
                hueValueText.text = getString(R.string.hue, hue)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                saturation = progress / 100.0f
                viewModel.saturation = saturation
                saturationValueText.text = getString(R.string.saturation, (saturation * 100).toInt())
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightness = (progress - 100).toFloat()
                viewModel.brightness = brightness
                brightnessValueText.text = getString(R.string.brightness, brightness)
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contrast = progress / 100.0f
                viewModel.contrast = contrast
                contrastValueText.text = getString(R.string.contrast, (contrast * 100).toInt())
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun updateButtonStyles() {
        if (previewAllButton.isEnabled) {
            previewAllButton.setBackgroundResource(R.drawable.button_primary)
            previewAllButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            previewAllButton.setBackgroundResource(R.drawable.bg_card)
            previewAllButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        if (exportButton.isEnabled) {
            exportButton.setBackgroundResource(R.drawable.button_primary)
            exportButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            exportButton.setBackgroundResource(R.drawable.bg_card)
            exportButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    private fun updateLayerInfo() {
        if (sampleAppPackage.isEmpty()) return

        try {
            val layers = IconScraper.getIconLayers(packageManager, sampleAppPackage)
            val info = StringBuilder()
            info.append("Capas disponibles:\n")
            
            if (layers.defaultIcon != null) info.append("• Icono por defecto\n")
            if (layers.roundIcon != null) info.append("• Icono redondo\n")
            if (layers.foregroundIcon != null) info.append("• Capa frontal\n")
            if (layers.backgroundIcon != null) info.append("• Capa de fondo\n")
            if (layers.adaptiveIcon != null) info.append("• Icono adaptativo\n")
            
            layerInfoText.text = info.toString()
        } catch (e: Exception) {
            layerInfoText.text = "No se pudieron detectar las capas"
        }
    }

    private fun setupPreviewCycle() {
        iconPreview.setOnClickListener {
            if (selectedApps.isNotEmpty() && selectedMask != null) {
                currentPreviewIndex = (currentPreviewIndex + 1) % selectedApps.size
                val nextApp = selectedApps[currentPreviewIndex]
                currentPreviewApp = nextApp
                
                Thread {
                    try {
                        val iconToProcess = if (shouldUsePreprocessedForegroundForApp(nextApp.packageName)) {
                            ForegroundCache.getForegroundIcon(nextApp.packageName)
                        } else {
                            val layers = IconScraper.getIconLayers(packageManager, nextApp.packageName)
                            val composedIcon = IconScraper.composeIconFromLayers(
                                layers = layers,
                                useDefault = useDefaultIcon,
                                useRound = useRoundIcon,
                                useForeground = useForegroundLayer,
                                useBackground = useBackgroundLayer
                            )
                            
                            if (composedIcon != null) {
                                IconThemer.drawableToNormalizedBitmap(composedIcon)
                            } else {
                                val appInfo = packageManager.getApplicationInfo(nextApp.packageName, 0)
                                val defaultDrawable = appInfo.loadIcon(packageManager)
                                IconThemer.drawableToNormalizedBitmap(defaultDrawable)
                            }
                        }
                        
                        if (iconToProcess != null) {
                            currentPreviewBitmap = iconToProcess
                            
                            val config = IconThemer.ThemeConfig(
                                mask = selectedMask!!,
                                color = selectedColor,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                scalePercentage = if (shouldUsePreprocessedForegroundForApp(nextApp.packageName)) foregroundScalePercentage else scalePercentage,
                                alphaPercentage = alphaPercentage,
                                colorIntensity = colorIntensity,
                                hue = hue,
                                saturation = saturation,
                                brightness = brightness,
                                contrast = contrast,
                                useDefaultIcon = useDefaultIcon,
                                useRoundIcon = useRoundIcon,
                                useForegroundLayer = useForegroundLayer,
                                useBackgroundLayer = useBackgroundLayer
                            )
                            
                            val themedIcon = IconThemer.applyTheme(iconToProcess, config)
                            
                            runOnUiThread {
                                iconPreview.setImageBitmap(themedIcon)
                                Toast.makeText(this, "Icono ${currentPreviewIndex + 1} de ${selectedApps.size}: ${nextApp.name}", 
                                    Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        runOnUiThread {
                            Toast.makeText(this, "Error al cargar el icono de ${nextApp.name}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            } else {
                if (selectedMask == null) {
                    Toast.makeText(this, "Primero selecciona una máscara", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun processAllIcons(onComplete: (() -> Unit)? = null) {
        if (selectedMask == null) {
            Toast.makeText(this, "Primero selecciona una máscara", Toast.LENGTH_SHORT).show()
            onComplete?.invoke()
            return
        }
    
        Thread {
            val totalIcons = selectedApps.size
            var processedCount = 0
    
            runOnUiThread {
                progressText.text = "Procesando: 0/$totalIcons"
                applyButton.isEnabled = false
            }
    
            cleanUpCurrentIcons()
    
            selectedApps.forEach { app ->
                try {
                    val iconToProcess = if (shouldUsePreprocessedForegroundForApp(app.packageName)) {
                        // Usar foreground pre-procesado con su escala específica
                        ForegroundCache.getForegroundIcon(app.packageName)
                    } else {
                        // Flujo normal
                        val layers = IconScraper.getIconLayers(packageManager, app.packageName)
                        val composedIcon = IconScraper.composeIconFromLayers(
                            layers = layers,
                            useDefault = useDefaultIcon,
                            useRound = useRoundIcon,
                            useForeground = useForegroundLayer,
                            useBackground = useBackgroundLayer
                        )
                        
                        if (composedIcon != null) {
                            IconThemer.drawableToNormalizedBitmap(composedIcon)
                        } else {
                            val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
                            val defaultDrawable = appInfo.loadIcon(packageManager)
                            IconThemer.drawableToNormalizedBitmap(defaultDrawable)
                        }
                    }
                    
                    if (iconToProcess != null) {
                        val config = IconThemer.ThemeConfig(
                            mask = selectedMask!!,
                            color = selectedColor,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            scalePercentage = if (shouldUsePreprocessedForegroundForApp(app.packageName)) foregroundScalePercentage else scalePercentage,
                            alphaPercentage = alphaPercentage,
                            colorIntensity = colorIntensity,
                            hue = hue,
                            saturation = saturation,
                            brightness = brightness,
                            contrast = contrast,
                            useDefaultIcon = useDefaultIcon,
                            useRoundIcon = useRoundIcon,
                            useForegroundLayer = useForegroundLayer,
                            useBackgroundLayer = useBackgroundLayer
                        )
                        
                        val themedIcon = IconThemer.applyTheme(iconToProcess, config)
                        
                        themedIcons[app.packageName] = themedIcon
                        previewIconsList.add(themedIcon)
                        processedCount++
                        
                        runOnUiThread {
                            progressText.text = "Procesando: $processedCount/$totalIcons"
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
    
            runOnUiThread {
                progressText.text = "¡Procesamiento completado!"
                applyButton.isEnabled = true
                exportButton.isEnabled = true
                previewAllButton.isEnabled = true
                viewModel.isProcessingComplete = true
                viewModel.updateConfigHash()
                
                saveStateToViewModel()
                
                updateButtonStyles()
                Toast.makeText(this, "$processedCount de ${selectedApps.size} iconos procesados exitosamente", Toast.LENGTH_SHORT).show()
                
                onComplete?.invoke()
            }
        }.start()
    }

    private fun cleanUpCurrentIcons() {
        try {
            themedIcons.values.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            previewIconsList.forEach { bitmap ->
                if (!bitmap.isRecycled) {
                    bitmap.recycle()
                }
            }
            themedIcons.clear()
            previewIconsList.clear()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun exportThemedIcons() {
        if (themedIcons.isEmpty()) {
            Toast.makeText(this, "Primero procesa los iconos", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                val timestamp = System.currentTimeMillis()
                val fileName = "themed_icons_$timestamp.zip"
                
                val success = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveZipToDownloads(fileName)
                } else {
                    saveZipToExternalStorage(fileName)
                }
                
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "Iconos tematizados exportados exitosamente a la carpeta Downloads", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Error al exportar iconos tematizados", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveZipToDownloads(fileName: String): Boolean {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS) // CORREGIDO: era RELICAL_PATH
            }
            
            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            
            uri?.let {
                resolver.openOutputStream(it)?.use { outputStream ->
                    val tempFile = File(cacheDir, "temp_$fileName")
                    val success = IconScraper.createThemedZipFile(themedIcons, tempFile)
                    
                    if (success) {
                        FileInputStream(tempFile).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                        tempFile.delete()
                        true
                    } else {
                        false
                    }
                }
            } ?: false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Suppress("DEPRECATION")
    private fun saveZipToExternalStorage(fileName: String): Boolean {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val outputFile = File(downloadsDir, fileName)
            IconScraper.createThemedZipFile(themedIcons, outputFile)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_MASK_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        loadMaskFromUri(uri)
                    }
                }
            }
        }
    }

    private fun selectMaskImage() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png"))
        }
        startActivityForResult(intent, PICK_MASK_REQUEST)
    }

    private fun showColorPickerDialog() {
        val colors = intArrayOf(
            android.graphics.Color.RED, 
            android.graphics.Color.GREEN, 
            android.graphics.Color.BLUE, 
            android.graphics.Color.CYAN, 
            android.graphics.Color.MAGENTA, 
            android.graphics.Color.YELLOW,
            android.graphics.Color.WHITE, 
            android.graphics.Color.BLACK, 
            android.graphics.Color.GRAY, 
            android.graphics.Color.parseColor("#FF5722"),
            android.graphics.Color.parseColor("#9C27B0"), 
            android.graphics.Color.parseColor("#2196F3"), 
            android.graphics.Color.parseColor("#4CAF50")
        )
    
        val colorNames = arrayOf(
            getString(R.string.color_red),
            getString(R.string.color_green),
            getString(R.string.color_blue),
            getString(R.string.color_cyan),
            getString(R.string.color_magenta),
            getString(R.string.color_yellow),
            getString(R.string.color_white),
            getString(R.string.color_black),
            getString(R.string.color_gray),
            getString(R.string.color_orange),
            getString(R.string.color_purple),
            getString(R.string.color_light_blue),
            getString(R.string.color_light_green)
        )
    
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_color))
            .setItems(colorNames) { dialog, which ->
                selectedColor = colors[which]
                viewModel.selectedColor = selectedColor
                colorPickerButton.setBackgroundColor(selectedColor)
                isColorApplied = true
                updatePreview() // Este método SÍ existe en tu código
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun loadMaskFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            selectedMask = BitmapFactory.decodeStream(inputStream)
            viewModel.selectedMask = selectedMask
            viewModel.maskUri = uri
            inputStream?.close()
            maskPreview.setImageBitmap(selectedMask)
            updatePreview()
        } catch (e: IOException) {
            Toast.makeText(this, "Error cargando máscara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun updatePreview() {
        if (selectedMask != null) {
            val currentApp = currentPreviewApp ?: selectedApps.getOrNull(0)
            val appPackage = currentApp?.packageName ?: sampleAppPackage
            
            val iconToUse = if (shouldUsePreprocessedForegroundForApp(appPackage)) {
                ForegroundCache.getForegroundIcon(appPackage) ?: sampleIcon
            } else {
                currentPreviewBitmap ?: sampleIcon
            }
            
            if (iconToUse != null) {
                val config = IconThemer.ThemeConfig(
                    mask = selectedMask!!,
                    color = selectedColor,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    scalePercentage = if (shouldUsePreprocessedForegroundForApp(appPackage)) foregroundScalePercentage else scalePercentage,
                    alphaPercentage = alphaPercentage,
                    colorIntensity = colorIntensity,
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    contrast = contrast,
                    useDefaultIcon = useDefaultIcon,
                    useRoundIcon = useRoundIcon,
                    useForegroundLayer = useForegroundLayer,
                    useBackgroundLayer = useBackgroundLayer
                )
                
                val processedIcon = IconThemer.applyTheme(iconToUse, config)
                iconPreview.setImageBitmap(processedIcon)
            }
        }
    }
    
    private fun shouldUsePreprocessedForegroundForApp(packageName: String): Boolean {
        return isForegroundPreprocessed && 
               useForegroundLayer && 
               !useBackgroundLayer && 
               !useDefaultIcon && 
               !useRoundIcon &&
               ForegroundCache.containsForegroundIcon(packageName)
    }

    private fun loadSampleIcon() {
        if (selectedApps.isNotEmpty()) {
            try {
                sampleAppPackage = selectedApps[0].packageName
                currentPreviewApp = selectedApps[0]
                
                if (isForegroundPreprocessed && ForegroundCache.containsForegroundIcon(sampleAppPackage)) {
                    sampleIcon = ForegroundCache.getForegroundIcon(sampleAppPackage)
                } else {
                    val layers = IconScraper.getIconLayers(packageManager, sampleAppPackage)
                    
                    val composedIcon = IconScraper.composeIconFromLayers(
                        layers = layers,
                        useDefault = useDefaultIcon,
                        useRound = useRoundIcon,
                        useForeground = useForegroundLayer,
                        useBackground = useBackgroundLayer
                    )
                    
                    sampleIcon = if (composedIcon != null) {
                        IconThemer.drawableToNormalizedBitmap(composedIcon)
                    } else {
                        val defaultDrawable = IconScraper.getSimpleIcon(packageManager, sampleAppPackage)
                        defaultDrawable?.let { IconThemer.drawableToNormalizedBitmap(it) } 
                            ?: IconThemer.normalizeIconSize(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                sampleIcon = IconThemer.normalizeIconSize(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            }
        } else {
            sampleIcon = IconThemer.normalizeIconSize(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
        }
        updatePreview()
        updateLayerInfo()
    }

    override fun onPause() {
        super.onPause()
        saveStateToViewModel()
    }
}