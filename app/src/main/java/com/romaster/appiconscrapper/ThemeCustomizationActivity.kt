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
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class ThemeCustomizationActivity : BaseActivity() {

    private var currentPreviewBitmap: Bitmap? = null
    private var isColorApplied = false
    
    // ✅ VISTAS PARA LOS TRES PREVIEWS
    private lateinit var iconbackPreview: ImageView
    private lateinit var iconmaskPreview: ImageView
    private lateinit var iconuponPreview: ImageView
    private lateinit var iconPreview: ImageView
    
    // ✅ BOTONES
    private lateinit var selectIconbackButton: Button
    private lateinit var selectIconmaskButton: Button
    private lateinit var selectIconuponButton: Button
    private lateinit var colorPickerButton: Button
    private lateinit var clearMasksButton: Button

    // SeekBars existentes
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

    // ✅ VARIABLES PARA MÁSCARAS
    private var selectedIconback: Bitmap? = null
    private var selectedIconmask: Bitmap? = null
    private var selectedIconupon: Bitmap? = null
    private var selectedColor: Int = android.graphics.Color.CYAN
    
    // Variables existentes
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
    private var foregroundScalePercentage: Int = 100 // ✅ INICIALIZADO CON 100
    private var isForegroundPreprocessed: Boolean = false

    companion object {
        private const val PICK_ICONBACK_REQUEST = 1001
        private const val PICK_ICONMASK_REQUEST = 1002
        private const val PICK_ICONUPON_REQUEST = 1003
        private const val TAG = "ThemeCustomization"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
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

        // ✅ INICIALIZAR MÁSCARAS CON ESTADO TRANSPARENTE
        initializeTransparentMasks()

        // Restaurar máscaras si existen
        if (selectedIconback != null) {
            iconbackPreview.setImageBitmap(selectedIconback)
        }
        if (selectedIconmask != null) {
            iconmaskPreview.setImageBitmap(selectedIconmask)
        }
        if (selectedIconupon != null) {
            iconuponPreview.setImageBitmap(selectedIconupon)
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

    // ✅ INICIALIZAR MÁSCARAS TRANSPARENTES
    private fun initializeTransparentMasks() {
        try {
            // Crear bitmaps transparentes por defecto
            val transparentBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.TRANSPARENT)
            }
            
            if (selectedIconback == null) {
                selectedIconback = transparentBitmap
                iconbackPreview.setImageBitmap(transparentBitmap)
            }
            
            if (selectedIconmask == null) {
                selectedIconmask = transparentBitmap
                iconmaskPreview.setImageBitmap(transparentBitmap)
            }
            
            if (selectedIconupon == null) {
                selectedIconupon = transparentBitmap
                iconuponPreview.setImageBitmap(transparentBitmap)
            }
            
            // ✅ ACTUALIZAR IconPackGenerator con las TRES máscaras
            IconPackGenerator.setSelectedIconback(selectedIconback)
            IconPackGenerator.setSelectedIconmask(selectedIconmask)
            IconPackGenerator.setSelectedIconupon(selectedIconupon)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando máscaras transparentes", e)
        }
    }
    
    // ✅ LIMPIAR MÁSCARAS (RESTAURAR TRANSPARENTE)
    private fun clearMasks() {
        try {
            // Crear bitmap transparente
            val transparentBitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888).apply {
                eraseColor(Color.TRANSPARENT)
            }
            
            selectedIconback = transparentBitmap
            selectedIconmask = transparentBitmap
            selectedIconupon = transparentBitmap
            
            // Actualizar previews
            iconbackPreview.setImageBitmap(transparentBitmap)
            iconmaskPreview.setImageBitmap(transparentBitmap)
            iconuponPreview.setImageBitmap(transparentBitmap)
            
            // ✅ ACTUALIZAR IconPackGenerator
            IconPackGenerator.setSelectedIconback(selectedIconback)
            IconPackGenerator.setSelectedIconmask(selectedIconmask)
            IconPackGenerator.setSelectedIconupon(selectedIconupon)
            
            // Actualizar preview del resultado
            updatePreview()
            
            Toast.makeText(this, getString(R.string.masks_cleared), Toast.LENGTH_SHORT).show()
            
        } catch (e: Exception) {
            Log.e(TAG, "Error limpiando máscaras", e)
        }
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
        selectedIconback = viewModel.selectedIconback
        selectedIconmask = viewModel.selectedIconmask
        selectedIconupon = viewModel.selectedIconupon
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
        viewModel.selectedIconback = selectedIconback
        viewModel.selectedIconmask = selectedIconmask
        viewModel.selectedIconupon = selectedIconupon
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
        // ✅ VISTAS
        iconbackPreview = findViewById(R.id.iconbackPreview)
        iconmaskPreview = findViewById(R.id.iconmaskPreview)
        iconuponPreview = findViewById(R.id.iconuponPreview)
        iconPreview = findViewById(R.id.iconPreview)
        
        // ✅ BOTONES
        selectIconbackButton = findViewById(R.id.selectIconbackButton)
        selectIconmaskButton = findViewById(R.id.selectIconmaskButton)
        selectIconuponButton = findViewById(R.id.selectIconuponButton)
        colorPickerButton = findViewById(R.id.colorPickerButton)
        clearMasksButton = findViewById(R.id.clearMasksButton)
    
        // Views existentes
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
    
        // Controles de ajuste de imagen
        seekBarHue = findViewById(R.id.seekBarHue)
        seekBarSaturation = findViewById(R.id.seekBarSaturation)
        seekBarBrightness = findViewById(R.id.seekBarBrightness)
        seekBarContrast = findViewById(R.id.seekBarContrast)
        hueValueText = findViewById(R.id.hueValueText)
        saturationValueText = findViewById(R.id.saturationValueText)
        brightnessValueText = findViewById(R.id.brightnessValueText)
        contrastValueText = findViewById(R.id.contrastValueText)
    
        // Controles de capas
        useDefaultIconCheckbox = findViewById(R.id.useDefaultIconCheckbox)
        useRoundIconCheckbox = findViewById(R.id.useRoundIconCheckbox)
        foregroundLayerCheckbox = findViewById(R.id.foregroundLayerCheckbox)
        backgroundLayerCheckbox = findViewById(R.id.backgroundLayerCheckbox)
        layerInfoText = findViewById(R.id.layerInfoText)
    
        // Controles de foreground scale
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
        
        // ✅ CORREGIDO: Configurar seekbar de foreground con valor inicial
        seekBarForegroundScale.max = 200
        seekBarForegroundScale.progress = foregroundScalePercentage
        foregroundScaleValueText.text = getString(R.string.foreground_scale, foregroundScalePercentage)
        
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
        
        // ✅ CORREGIDO: Inicializar foreground scale con valor por defecto
        seekBarForegroundScale.progress = foregroundScalePercentage
        foregroundScaleValueText.text = getString(R.string.foreground_scale, foregroundScalePercentage)
        
        useDefaultIconCheckbox.isChecked = useDefaultIcon
        useRoundIconCheckbox.isChecked = useRoundIcon
        foregroundLayerCheckbox.isChecked = useForegroundLayer
        backgroundLayerCheckbox.isChecked = useBackgroundLayer
    }
    
    private fun setupListeners() {
        // ✅ LISTENERS PARA BOTONES DE MÁSCARAS
        selectIconbackButton.setOnClickListener {
            selectMaskImage(PICK_ICONBACK_REQUEST)
        }
    
        selectIconmaskButton.setOnClickListener {
            selectMaskImage(PICK_ICONMASK_REQUEST)
        }

        selectIconuponButton.setOnClickListener {
            selectMaskImage(PICK_ICONUPON_REQUEST)
        }
    
        colorPickerButton.setOnClickListener {
            showColorPickerDialog()
        }
    
        // ✅ LISTENER PARA LIMPIAR MÁSCARAS
        clearMasksButton.setOnClickListener {
            clearMasks()
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
    
        // MODIFICADO: Unificado para mostrar opciones de exportación
        exportButton.setOnClickListener {
            showExportOptionsDialog()
        }
    }
    
    // ✅ SELECCIONAR MÁSCARA CON TIPO
    private fun selectMaskImage(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/png"))
        }
        startActivityForResult(intent, requestCode)
    }
    
    // ✅ MANEJAR RESULTADO DE SELECCIÓN
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_ICONBACK_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        loadMaskFromUri(uri, isIconback = true)
                    }
                }
            }
            PICK_ICONMASK_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        loadMaskFromUri(uri, isIconback = false)
                    }
                }
            }
            PICK_ICONUPON_REQUEST -> {
                if (resultCode == Activity.RESULT_OK) {
                    data?.data?.let { uri ->
                        loadMaskFromUri(uri, isIconupon = true)
                    }
                }
            }
        }
    }
    
    // ✅ CARGAR MÁSCARA DESDE URI CON TIPO
    private fun loadMaskFromUri(uri: Uri, isIconback: Boolean = false, isIconupon: Boolean = false) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (isIconback) {
                selectedIconback = bitmap
                viewModel.selectedIconback = selectedIconback
                iconbackPreview.setImageBitmap(selectedIconback)
                IconPackGenerator.setSelectedIconback(selectedIconback)
                Toast.makeText(this, "Fondo actualizado", Toast.LENGTH_SHORT).show()
            } else if (isIconupon) {
                selectedIconupon = bitmap
                viewModel.selectedIconupon = selectedIconupon
                iconuponPreview.setImageBitmap(selectedIconupon)
                IconPackGenerator.setSelectedIconupon(selectedIconupon)
                Toast.makeText(this, "Capa superior actualizada", Toast.LENGTH_SHORT).show()
            } else {
                selectedIconmask = bitmap
                viewModel.selectedIconmask = selectedIconmask
                iconmaskPreview.setImageBitmap(selectedIconmask)
                IconPackGenerator.setSelectedIconmask(selectedIconmask)
                Toast.makeText(this, "Máscara actualizada", Toast.LENGTH_SHORT).show()
            }
            
            updatePreview()
            
        } catch (e: IOException) {
            Toast.makeText(this, "Error cargando imagen: ${e.message}", Toast.LENGTH_LONG).show()
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
        
        // ✅ CORREGIDO: Listener para foreground scale
        seekBarForegroundScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                foregroundScalePercentage = progress
                foregroundScaleValueText.text = getString(R.string.foreground_scale, foregroundScalePercentage)
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
            if (selectedApps.isNotEmpty() && selectedIconback != null) {
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
                                mask = selectedIconback!!,
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
                if (selectedIconback == null) {
                    Toast.makeText(this, "Primero selecciona un fondo", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun processAllIcons(onComplete: (() -> Unit)? = null) {
        if (selectedIconback == null) {
            Toast.makeText(this, "Primero procesa los iconos", Toast.LENGTH_SHORT).show()
            onComplete?.invoke()
            return
        }
        
        // ✅ ACTUALIZADO: Guardar las tres máscaras en IconPackGenerator
        IconPackGenerator.setSelectedIconback(selectedIconback)
        IconPackGenerator.setSelectedIconmask(selectedIconmask)
        IconPackGenerator.setSelectedIconupon(selectedIconupon)
    
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
                            mask = selectedIconback!!,
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
    
    private fun updatePreview() {
        if (selectedIconback != null) {
            val currentApp = currentPreviewApp ?: selectedApps.getOrNull(0)
            val appPackage = currentApp?.packageName ?: sampleAppPackage
            
            val iconToUse = if (shouldUsePreprocessedForegroundForApp(appPackage)) {
                ForegroundCache.getForegroundIcon(appPackage) ?: sampleIcon
            } else {
                currentPreviewBitmap ?: sampleIcon
            }
            
            if (iconToUse != null) {
                val config = IconThemer.ThemeConfig(
                    mask = selectedIconback!!,
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
    
    // ✅ MÉTODOS DE EXPORTACIÓN COMPLETOS
    private fun showExportOptionsDialog() {
        if (themedIcons.isEmpty()) {
            Toast.makeText(this, getString(R.string.no_apps_selected), Toast.LENGTH_SHORT).show()
            return
        }
    
        val options = arrayOf(
            getString(R.string.export_as_zip_recommended),
            getString(R.string.export_as_apk_experimental)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.export_format))
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> exportThemedIcons() // ZIP
                    1 -> showIconPackNameDialog(true)  // APK
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun exportThemedIcons() {
        if (themedIcons.isEmpty()) {
            Toast.makeText(this, "Primero procesa los iconos", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Iniciando exportación de ${themedIcons.size} iconos")

        Thread {
            try {
                val timestamp = System.currentTimeMillis()
                val fileName = "themed_icons_$timestamp.zip"
                
                Log.d(TAG, "Creando archivo: $fileName")
                
                val outputFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveZipToDownloads(fileName)
                } else {
                    saveZipToExternalStorage(fileName)
                }
                
                runOnUiThread {
                    if (outputFile != null && outputFile.exists()) {
                        Log.d(TAG, "Exportación exitosa: ${outputFile.absolutePath}")
                        Toast.makeText(this, 
                            "Iconos exportados exitosamente: ${outputFile.name}", 
                            Toast.LENGTH_LONG).show()
                    } else {
                        Log.e(TAG, "Error en la exportación")
                        Toast.makeText(this, 
                            "Error al exportar iconos. Verifica los permisos de almacenamiento.", 
                            Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en exportThemedIcons", e)
                runOnUiThread {
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveZipToDownloads(fileName: String): File? {
        return try {
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/zip")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val resolver = contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            
            uri?.let { 
                resolver.openOutputStream(it)?.use { outputStream ->
                    val success = createThemedZipFile(themedIcons, outputStream)
                    if (success) {
                        getFileFromUri(uri)
                    } else {
                        null
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    @Suppress("DEPRECATION")
    private fun saveZipToExternalStorage(fileName: String): File? {
        return try {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val outputFile = File(downloadsDir, fileName)
            
            val success = IconScraper.createThemedZipFile(themedIcons, outputFile)
            if (success) {
                outputFile
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createThemedZipFile(themedIcons: Map<String, Bitmap>, outputStream: OutputStream): Boolean {
        return try {
            ZipOutputStream(outputStream).use { zos ->
                themedIcons.forEach { (packageName, themedIcon) ->
                    try {
                        val entryName = "$packageName.png"
                        val entry = ZipEntry(entryName)
                        zos.putNextEntry(entry)
                        
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        themedIcon.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
                        zos.write(byteArrayOutputStream.toByteArray())
                        zos.closeEntry()
                    } catch (e: Exception) {
                        Log.e(TAG, "Error adding icon to ZIP: $packageName", e)
                    }
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error creating ZIP file", e)
            false
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    val displayName = it.getString(displayNameIndex)
                    val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    File(downloadsDir, displayName)
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // ✅ MÉTODO PARA GENERAR APK
    private fun generateIconPackAPK(packName: String) {
        showExportProgress(true)
        
        Thread {
            try {
                val config = IconPackGenerator.IconPackConfig(
                    appName = packName,
                    packageName = "com.romaster.iconpack.${System.currentTimeMillis()}"
                )
                
                Log.d("APK_DEBUG", "Iniciando generación de APK para: $packName")
                Log.d("APK_DEBUG", "Número de íconos: ${themedIcons.size}")
                
                val apkFile = IconPackGenerator.generateIconPackAPK(
                    this@ThemeCustomizationActivity, 
                    themedIcons, 
                    config,
                    object : IconPackGenerator.ExportProgressListener {
                        override fun onProgressUpdate(step: String, progress: Int) {
                            runOnUiThread {
                                progressText.text = "$step ($progress%)"
                            }
                            Log.d("APK_DEBUG", "Progreso: $step ($progress%)")
                        }
                    }
                )
                
                runOnUiThread {
                    showExportProgress(false)
                    
                    if (apkFile != null && apkFile.exists() && apkFile.length() > 1024) {
                        val fileSizeMB = apkFile.length() / (1024.0 * 1024.0)
                        val message = """
                            APK generado exitosamente!
                            
                            Tamaño: ${"%.1f".format(fileSizeMB)} MB
                            Archivo: ${apkFile.name}
                            Ubicación: ${apkFile.absolutePath}
                            
                            ✅ El APK debería ser instalable ahora
                        """.trimIndent()
                        
                        AlertDialog.Builder(this@ThemeCustomizationActivity)
                            .setTitle("✅ APK Generado")
                            .setMessage(message)
                            .setPositiveButton("Instalar") { dialog, _ ->
                                installAPK(apkFile)
                                dialog.dismiss()
                            }
                            .setNeutralButton("Ver en Archivos") { dialog, _ ->
                                viewFileInExplorer(apkFile)
                                dialog.dismiss()
                            }
                            .setNegativeButton("OK", null)
                            .show()
                            
                        Log.d("APK_DEBUG", "APK creado exitosamente: ${apkFile.length()} bytes")
                    } else {
                        val errorMsg = if (apkFile == null) {
                            "No se pudo crear el archivo APK"
                        } else if (!apkFile.exists()) {
                            "El archivo APK no existe"
                        } else {
                            "APK vacío o corrupto (${apkFile.length()} bytes)"
                        }
                        
                        AlertDialog.Builder(this@ThemeCustomizationActivity)
                            .setTitle("❌ Error")
                            .setMessage("$errorMsg\n\nPrueba con menos íconos o reinicia la app.")
                            .setPositiveButton("OK", null)
                            .show()
                        
                        Log.e("APK_DEBUG", errorMsg)
                    }
                }
            } catch (e: Exception) {
                Log.e("APK_DEBUG", "Error crítico: ${e.message}", e)
                runOnUiThread {
                    showExportProgress(false)
                    AlertDialog.Builder(this@ThemeCustomizationActivity)
                        .setTitle("❌ Error Crítico")
                        .setMessage("Error: ${e.message}\n\nRevisa los logs para más detalles.")
                        .setPositiveButton("OK", null)
                        .show()
                }
            }
        }.start()
    }
    
    // ✅ MÉTODO PARA EXPORTAR ZIP
    private fun exportIconPackZIP(packName: String) {
        showExportProgress(true)
        
        Thread {
            try {
                val timestamp = System.currentTimeMillis()
                val fileName = "${packName.replace(" ", "_")}_$timestamp.zip"
                
                Log.d(TAG, "Exportando ZIP: $fileName con ${themedIcons.size} iconos")
                
                val zipFile = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    saveZipToDownloads(fileName)
                } else {
                    saveZipToExternalStorage(fileName)
                }
                
                runOnUiThread {
                    showExportProgress(false)
                    if (zipFile != null && zipFile.exists()) {
                        Log.d(TAG, "ZIP creado exitosamente: ${zipFile.absolutePath}")
                        showExportSuccessDialog(zipFile, false)
                    } else {
                        Log.e(TAG, "Error al crear el archivo ZIP")
                        Toast.makeText(this, getString(R.string.zip_error), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error en exportIconPackZIP", e)
                runOnUiThread {
                    showExportProgress(false)
                    Toast.makeText(this, "${getString(R.string.zip_error)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    // ✅ MÉTODO PARA DIÁLOGO DE NOMBRE DEL PACK
    private fun showIconPackNameDialog(isAPK: Boolean = false) {
        val editText = EditText(this).apply {
            hint = getString(R.string.icon_pack_name_hint)
            val suffix = if (isAPK) " APK" else " Pack"
            setText("${getString(R.string.app_name)}${suffix} ${SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())}")
        }
    
        val title = if (isAPK) getString(R.string.apk_name) else getString(R.string.pack_name)
        val buttonText = if (isAPK) getString(R.string.generate_apk) else getString(R.string.generate_zip)
    
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(editText)
            .setPositiveButton(buttonText) { dialog, _ ->
                val packName = editText.text.toString().trim()
                if (packName.isNotEmpty()) {
                    if (isAPK) {
                        generateIconPackAPK(packName)
                    } else {
                        exportIconPackZIP(packName)
                    }
                } else {
                    Toast.makeText(this, getString(R.string.icon_pack_name_hint), Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    // ✅ MÉTODOS AUXILIARES
    private fun showExportProgress(show: Boolean) {
        exportButton.isEnabled = !show
        if (show) {
            exportButton.text = getString(R.string.exporting_apk)
        } else {
            exportButton.text = getString(R.string.export_icon_pack)
        }
        updateButtonStyles()
    }
    
    private fun showExportSuccessDialog(outputFile: File, isAPK: Boolean = false) {
        val message = if (isAPK) {
            """
            ${getString(R.string.apk_export_complete)}
            
            ${getString(R.string.apk_compatible_launchers)}
            
            ${getString(R.string.apk_saved_location, outputFile.absolutePath)}
            
            ⚠️ ${getString(R.string.apk_experimental_warning)}
            """.trimIndent()
        } else {
            """
            ${getString(R.string.zip_export_complete)}
            
            ${getString(R.string.apk_compatible_launchers)}
            
            ${getString(R.string.zip_saved_location, outputFile.absolutePath)}
            
            ✅ ${getString(R.string.zip_working_note)}
            """.trimIndent()
        }
    
        val dialogTitle = if (isAPK) getString(R.string.apk_export_complete) else getString(R.string.zip_export_complete)
    
        AlertDialog.Builder(this)
            .setTitle(dialogTitle)
            .setMessage(message)
            .setPositiveButton(if (isAPK) getString(R.string.install_now) else getString(R.string.share_file)) { dialog, _ ->
                if (isAPK) {
                    installAPK(outputFile)
                } else {
                    shareFile(outputFile)
                }
                dialog.dismiss()
            }
            .setNeutralButton(getString(R.string.view_in_files)) { dialog, _ ->
                viewFileInExplorer(outputFile)
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.ok, null)
            .show()
    }
    
    private fun installAPK(apkFile: File) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    androidx.core.content.FileProvider.getUriForFile(
                        this@ThemeCustomizationActivity,
                        "${packageName}.provider",
                        apkFile
                    ),
                    "application/vnd.android.package-archive"
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error installing APK", Toast.LENGTH_LONG).show()
        }
    }

    private fun shareFile(file: File) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = if (file.name.endsWith(".apk")) "application/vnd.android.package-archive" else "application/zip"
                putExtra(Intent.EXTRA_STREAM, androidx.core.content.FileProvider.getUriForFile(
                    this@ThemeCustomizationActivity,
                    "${packageName}.provider",
                    file
                ))
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(intent, getString(R.string.share_icon_pack)))
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.share_error), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun viewFileInExplorer(file: File) {
        try {
            val parentDir = file.parentFile
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(
                    Uri.parse("file://${parentDir?.absolutePath}"),
                    "resource/folder"
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(
                        androidx.core.content.FileProvider.getUriForFile(
                            this@ThemeCustomizationActivity,
                            "${packageName}.provider",
                            file
                        ),
                        if (file.name.endsWith(".apk")) "application/vnd.android.package-archive" else "application/zip"
                    )
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                startActivity(intent)
            } catch (e2: Exception) {
                Toast.makeText(this, getString(R.string.file_manager_error), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ COLOR PICKER COMPLETO
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
            getString(R.string.color_light_green),
            getString(R.string.color_hexadecimal)
        )
    
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_color))
            .setItems(colorNames) { dialog, which ->
                if (which == colorNames.size - 1) {
                    showHexColorPicker()
                } else {
                    selectedColor = colors[which]
                    viewModel.selectedColor = selectedColor
                    colorPickerButton.setBackgroundColor(selectedColor)
                    isColorApplied = true
                    updatePreview()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun showHexColorPicker() {
        val editText = EditText(this).apply {
            hint = "#RRGGBB o #AARRGGBB"
            setText("#")
            setSelectAllOnFocus(true)
        }
    
        val colorPreview = ImageView(this).apply {
            setBackgroundColor(android.graphics.Color.WHITE)
            setPadding(32, 32, 32, 32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                100
            )
        }
    
        fun updateColorPreview(hexColor: String) {
            try {
                val color = android.graphics.Color.parseColor(hexColor)
                colorPreview.setBackgroundColor(color)
            } catch (e: Exception) {
                colorPreview.setBackgroundColor(android.graphics.Color.WHITE)
            }
        }
    
        editText.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                updateColorPreview(s.toString())
            }
        })
    
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 10)
            addView(editText)
            addView(colorPreview)
        }
    
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Color Hexadecimal")
            .setView(layout)
            .setPositiveButton("Aceptar") { dialog, _ ->
                val hexColor = editText.text.toString().trim()
                if (isValidHexColor(hexColor)) {
                    try {
                        selectedColor = android.graphics.Color.parseColor(hexColor)
                        viewModel.selectedColor = selectedColor
                        colorPickerButton.setBackgroundColor(selectedColor)
                        isColorApplied = true
                        updatePreview()
                        Toast.makeText(this, "Color hexadecimal aplicado", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Color hexadecimal inválido", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Formato inválido. Use #RRGGBB o #AARRGGBB", Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    
        editText.requestFocus()
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.showSoftInput(editText, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun isValidHexColor(hexColor: String): Boolean {
        if (hexColor.isEmpty()) return false
        
        val hexPattern = if (hexColor.length == 7) {
            "^#([A-Fa-f0-9]{6})$"
        } else if (hexColor.length == 9) {
            "^#([A-Fa-f0-9]{8})$"
        } else {
            return false
        }
        
        return hexColor.matches(hexPattern.toRegex())
    }

    override fun onPause() {
        super.onPause()
        saveStateToViewModel()
    }
}