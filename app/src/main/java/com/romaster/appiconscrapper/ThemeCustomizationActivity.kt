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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.view.LayoutInflater
import com.google.android.material.switchmaterial.SwitchMaterial
import android.util.TypedValue
import com.google.android.material.button.MaterialButton
import androidx.core.content.res.ResourcesCompat
import android.content.res.ColorStateList
import android.view.ViewGroup

class ThemeCustomizationActivity : BaseActivity() {

    private var currentPreviewBitmap: Bitmap? = null
    private var isColorApplied = false
    
    // ✅ VISTAS PARA LOS TRES PREVIEWS
    private lateinit var iconbackPreview: ImageView
    private lateinit var iconmaskPreview: ImageView
    private lateinit var iconuponPreview: ImageView
    private lateinit var iconPreview: ImageView
    
    // ✅ BOTONES CON COLOR DE COLORPICKER
    private lateinit var colorizationButton: MaterialButton
    private lateinit var borderColorButton: MaterialButton
    
    // ✅ BOTONES - TODOS SON MaterialButton AHORA
    private lateinit var selectIconbackButton: MaterialButton
    private lateinit var selectIconmaskButton: MaterialButton
    private lateinit var selectIconuponButton: MaterialButton
    private lateinit var colorPickerButton: MaterialButton
    private lateinit var clearMasksButton: MaterialButton

    // SeekBars existentes
    private lateinit var seekBarX: SeekBar
    private lateinit var seekBarY: SeekBar
    private lateinit var seekBarScale: SeekBar
    private lateinit var seekBarAlpha: SeekBar
    private lateinit var seekBarColorIntensity: SeekBar
    private lateinit var applyButton: MaterialButton
    private lateinit var previewAllButton: MaterialButton
    private lateinit var exportButton: MaterialButton
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
    private var selectedColor: Int = Color.CYAN
    
    // Variables existentes
    private var offsetX: Int = 0
    private var offsetY: Int = 0
    private var scalePercentage: Int = 100
    private var alphaPercentage: Int = 100
    private var colorIntensity: Int = 0
    
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
    
    private lateinit var maskPickerLauncher: ActivityResultLauncher<String>
    
    private var themingName: String = ""
    private var themingAuthor: String = ""
    private var themingDescription: String = ""
    
    // ✅ FUNCIÓN PARA DETERMINAR SI UN COLOR ES OSCURO
    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
    
    // ✅ FUNCIÓN PARA ACTUALIZAR BOTÓN CON COLOR Y TEXTO APROPIADO (COMPATIBLE CON SISTEMA DE TEMAS)
    private fun updateButtonWithColor(button: MaterialButton, color: Int) {
        Log.d(TAG, "Actualizando botón ${button.text} con color: #${color.toString(16)}")
        
        // Guardar estado temporal para evitar interferencia del sistema de temas
        button.setTag(R.id.button_custom_color, color)
        
        // Determinar el color del texto basado en el brillo del fondo
        val textColor = if (isColorDark(color)) {
            Color.WHITE
        } else {
            Color.BLACK
        }
        
        // ✅ IMPORTANTE: Forzar la actualización del color de fondo y texto
        button.setBackgroundColor(color)
        button.setTextColor(textColor)
        
        // Para MaterialButton, mantener funcionalidad de borde
        if (button.strokeWidth > 0) {
            button.strokeColor = ColorStateList.valueOf(color)
        }
        
        // Configurar icono si existe
        button.iconTint = ColorStateList.valueOf(textColor)
        
        // Marcar que este botón tiene color personalizado
        button.setTag(R.id.has_custom_color, true)
        
        // ✅ Forzar redibujado del botón
        button.invalidate()
        button.requestLayout()
        
        Log.d(TAG, "Color de texto establecido: ${if (isColorDark(color)) "BLANCO" else "NEGRO"}")
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
              
        setContentView(R.layout.activity_theme_customization)
        
        supportActionBar?.title = getString(R.string.theme_customization_title)
    
        // ✅ VERIFICAR INTEGRIDAD DEL SISTEMA DE TEMAS
        ThemeManager.checkThemeIntegrity(this)
        
        // Inicializar ViewModel
        viewModel = ViewModelProvider(this).get(ThemeCustomizationViewModel::class.java)

        // Obtener apps seleccionadas solo si es la primera vez
        if (viewModel.selectedApps.isEmpty()) {
            viewModel.selectedApps = intent.getParcelableArrayListExtra<AppInfo>("selected_apps") ?: emptyList()
        }

        selectedApps = viewModel.selectedApps

        // INICIALIZAR VISTAS PRIMERO
        initViews()
        
        // AHORA restaurar estado desde ViewModel (después de initViews)
        restoreStateFromViewModel()
        
        // ✅ APLICAR SISTEMA CENTRALIZADO DE TEMAS A TODOS LOS COMPONENTES
        applyThemeToAllComponents()
        
        // ✅ CONFIGURAR FILTROS AVANZADOS
        setupAdvancedFiltersSection()
        
        // Verificar si venimos de pre-procesamiento
        isForegroundPreprocessed = intent.getBooleanExtra("foreground_preprocessed", false)
    
        setupListeners()
        loadSampleIcon()
        setupPreviewCycle()
        updateLayerInfo()
        
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

        // Si el procesamiento ya estaba completo, habilitar botones
        if (viewModel.isProcessingComplete) {
            exportButton.isEnabled = true
            previewAllButton.isEnabled = true
        }
        
        // Actualizar controles con valores guardados
        updateSeekBars()
        updatePreview()
        
        // Forzar actualización de visibilidad
        updateForegroundScaleVisibility()
        
        maskPickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                viewModel.maskBitmap = bitmap
                // ✅ CORRECCIÓN: Habilitar el escalado cuando se carga una máscara
                viewModel.maskScaleEnabled = viewModel.maskEnabled
                updateMaskScaleVisibility(viewModel.maskEnabled)
                updatePreview()
            }
        }
        
        // Botones para exportacion/importacion de Tematizacion
        val importThemingButton = findViewById<MaterialButton>(R.id.importThemingButton)
        val exportThemingButton = findViewById<MaterialButton>(R.id.exportThemingButton)
        
        importThemingButton.setOnClickListener {
            importTheming()
        }
        
        exportThemingButton.setOnClickListener {
            exportTheming()
        }
        
        // ✅ ACTUALIZAR ESTADOS INICIALES DE BOTONES
        refreshButtonStates()
        
// *****************************************************************************************************************
        
        // Método 1 para aplicar video a cards: Individual con sufijos específicos
        // METODOS PARA VIDEOS CON INTRO+LOOP
        applyThemeVideoToCard(findViewById(R.id.customizationCardExport), "_customization_export", 500)
        applyThemeVideoToCard(findViewById(R.id.customizationCardControlsAdjust), "_customization_adjust", 550)

        applyThemeVideoWithSeamlessIntro(
            cardView = findViewById(R.id.customizationCardControlsBasic),
            introSuffix = "_customization_basic_intro",   // Video de apertura  
            loopSuffix = "_customization_basic_loop",     // Video en bucle
            delayMs = 600,
            videoAlpha = 0.8f
        )
        applyThemeVideoWithSeamlessIntro(
            cardView = findViewById(R.id.customizationCardLayers),
            introSuffix = "_customization_layers_intro",   // Video de apertura  
            loopSuffix = "_customization_layers_loop",     // Video en bucle
            delayMs = 650,
            videoAlpha = 0.8f
        )
        applyThemeVideoWithSeamlessIntro(
            cardView = findViewById(R.id.customizationCardPreview),
            introSuffix = "_customization_preview_intro",  // Video de apertura
            loopSuffix = "_customization_preview_loop",    // Video en bucle
            delayMs = 700,
            videoAlpha = 0.8f
        )
                        
// *****************************************************************************************************************
        
    }
    
    // ✅ NUEVO: Aplicar sistema de temas a todos los componentes
    private fun applyThemeToAllComponents() {
        // Aplicar tema a todos los componentes de la vista raíz
        ThemeSystem.applyThemeToViewTree(this, window.decorView.rootView)
        
        val switches = listOf<SwitchMaterial>(
            findViewById(R.id.advancedFiltersToggle),
            findViewById(R.id.maskToggle),
            findViewById(R.id.edgeEnhanceToggle),
            findViewById(R.id.chromaticToggle),
            findViewById(R.id.sphereToggle),
            findViewById(R.id.embossToggle),
            findViewById(R.id.glowToggle),
            findViewById(R.id.softMaskToggle),
            findViewById(R.id.rotationToggle),
            findViewById(R.id.shadowToggle),
            findViewById(R.id.borderToggle),
            findViewById(R.id.pixelateToggle),
            findViewById(R.id.cartoonToggle),
            findViewById(R.id.noiseToggle),
            findViewById(R.id.fisheyeToggle),
            findViewById(R.id.imageScaleToggle),
            findViewById(R.id.iconColorizationToggle),
            findViewById(R.id.iconmaskShapeToggle)
        )
        
        switches.forEach { switch ->
            ThemeSystem.applyThemeToComponent(this, switch, ThemeSystem.ComponentType.SWITCH)
        }
        
        Log.d(TAG, "✅ Sistema de temas aplicado a todos los componentes")
    }
    
    // ✅ NUEVO: Refrescar estados de botones basados en su estado enabled/disabled
    private fun refreshButtonStates() {
        // Los botones YA tienen sus estilos definidos en XML (?attr/materialButtonStyle y ?attr/materialButtonOutlinedStyle)
        // Solo necesitamos asegurar que los estados enabled/disabled estén reflejados
        
        // previewAllButton y exportButton usan outlined cuando están deshabilitados
        // Esto se maneja AUTOMÁTICAMENTE por el sistema de temas de Android
        
        Log.d(TAG, "🔄 Refrescando estados de botones:")
        Log.d(TAG, "  - previewAllButton: enabled=${previewAllButton.isEnabled}")
        Log.d(TAG, "  - exportButton: enabled=${exportButton.isEnabled}")
        Log.d(TAG, "  - applyButton: enabled=${applyButton.isEnabled}")
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
        // ✅ CORREGIDO: Actualizar texto de intensidad de color con el valor del ViewModel
        colorIntensityValueText.text = getString(R.string.color_intensity, colorIntensity)
    
        hue = viewModel.hue
        saturation = viewModel.saturation
        brightness = viewModel.brightness
        contrast = viewModel.contrast
        useDefaultIcon = viewModel.useDefaultIcon
        useRoundIcon = viewModel.useRoundIcon
        useForegroundLayer = viewModel.useForegroundLayer
        useBackgroundLayer = viewModel.useBackgroundLayer
        foregroundScalePercentage = viewModel.foregroundScalePercentage
        
        // Restaurar íconos procesados
        themedIcons.clear()
        themedIcons.putAll(viewModel.themedIcons)
        previewIconsList.clear()
        previewIconsList.addAll(viewModel.previewIconsList)
        
        // ✅ ACTUALIZAR COLOR DEL BOTÓN DE COLOR PICKER (solo color de fondo)
        colorPickerButton.setBackgroundColor(selectedColor)
        
        // ✅ ACTUALIZAR COLOR DEL BOTÓN DE COLOR PICKER
        updateButtonWithColor(colorPickerButton, selectedColor)
        
        // Actualizar colores de botones de colorización y borde
        //val colorizationButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
        //val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
        
        colorizationButton?.setBackgroundColor(viewModel.iconColorizationColor)
        borderColorButton?.setBackgroundColor(viewModel.borderColor)
        
        // ✅ ACTUALIZAR COLOR DEL BOTÓN DE COLOR PICKER
        updateButtonWithColor(colorPickerButton, selectedColor)
        
        // ✅ ACTUALIZAR COLORES DE BOTONES DE COLORIZACIÓN Y BORDE CON COLORES CONTRASTANTES
        //val colorizationButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
        //val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
        
        //if (colorizationButton != null) {
            updateButtonWithColor(colorizationButton, viewModel.iconColorizationColor)
        //}
        
        //if (borderColorButton != null) {
            updateButtonWithColor(borderColorButton, viewModel.borderColor)
        //}
            
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
        
        // ✅ BOTONES - TODOS SON MaterialButton
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
        previewAllButton = findViewById(R.id.previewAllButton)
        exportButton = findViewById(R.id.exportButton)
        xValueText = findViewById(R.id.xValueText)
        yValueText = findViewById(R.id.yValueText)
        scaleValueText = findViewById(R.id.scaleValueText)
        alphaValueText = findViewById(R.id.alphaValueText)
        colorIntensityValueText = findViewById(R.id.colorIntensityValueText)
        progressText = findViewById(R.id.progressText)
        // ✅ CORREGIDO: Inicializar texto de intensidad de color con el valor por defecto
        colorIntensityValueText.text = getString(R.string.color_intensity, colorIntensity)
        
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
        
        // ✅ Configurar rangos existentes
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
        
        // ✅ INICIALIZAR BOTÓN DE COLOR PICKER CON COLOR POR DEFECTO
        colorPickerButton.setBackgroundColor(selectedColor)
        
        // ✅ INICIALIZAR BOTÓN DE COLOR PICKER CON COLOR POR DEFECTO
        updateButtonWithColor(colorPickerButton, selectedColor)
        
        // ✅ ELIMINADO: Todo el código de gestión de estilos hardcodeados
        // Los estilos ahora vienen completamente de XML
        
        // ✅ INICIALIZAR BOTÓN DE COLOR PICKER CON COLOR POR DEFECTO
        updateButtonWithColor(colorPickerButton, selectedColor)
        
        // ✅ INICIALIZAR BOTONES DE COLORIZACIÓN Y BORDE CON COLORES CONTRASTANTES
        colorizationButton = findViewById(R.id.colorizationColorButton)
        borderColorButton = findViewById(R.id.borderColorButton)
        
        // ✅ INICIALIZAR COLORES
        updateButtonWithColor(colorizationButton, viewModel.iconColorizationColor)
        updateButtonWithColor(borderColorButton, viewModel.borderColor)
        
        Log.d(TAG, "✅ Vistas inicializadas - Sistema de temas centralizado activo")
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
        
        // ✅ CORREGIDO: Actualizar todos los textos con los valores actuales
        xValueText.text = getString(R.string.position_x, offsetX)
        yValueText.text = getString(R.string.position_y, offsetY)
        scaleValueText.text = getString(R.string.scale, scalePercentage)
        alphaValueText.text = getString(R.string.transparency, alphaPercentage)
        colorIntensityValueText.text = getString(R.string.color_intensity, colorIntensity)  // ✅ Añadir esta línea
        hueValueText.text = getString(R.string.hue, hue)
        saturationValueText.text = getString(R.string.saturation, (saturation * 100).toInt())
        brightnessValueText.text = getString(R.string.brightness, brightness)
        contrastValueText.text = getString(R.string.contrast, (contrast * 100).toInt())
        
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
            processAllIcons {
                // ✅ SIMPLIFICADO: Solo refrescar estados
                refreshButtonStates()
            }
        }
    
        // MODIFICADO: Unificado para mostrar opciones de exportación
        exportButton.setOnClickListener {
            showExportOptionsDialog()
        }
        
        // Botón para cargar máscara de recorte
        findViewById<MaterialButton>(R.id.loadMaskButton).setOnClickListener {
            openMaskImagePicker()
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
            val originalBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            // ✅ NORMALIZAR INMEDIATAMENTE A 194x194
            val normalizedBitmap = IconThemer.normalizeMaskSize(originalBitmap)
            
            if (isIconback) {
                selectedIconback = normalizedBitmap
                viewModel.selectedIconback = selectedIconback
                iconbackPreview.setImageBitmap(selectedIconback)
                IconPackGenerator.setSelectedIconback(selectedIconback)
                Toast.makeText(this, getString(R.string.background_updated), Toast.LENGTH_SHORT).show()
            } else if (isIconupon) {
                selectedIconupon = normalizedBitmap
                viewModel.selectedIconupon = selectedIconupon
                iconuponPreview.setImageBitmap(selectedIconupon)
                IconPackGenerator.setSelectedIconupon(selectedIconupon)
                Toast.makeText(this, getString(R.string.upper_layer_updated), Toast.LENGTH_SHORT).show()
            } else {
                selectedIconmask = normalizedBitmap
                viewModel.selectedIconmask = selectedIconmask
                viewModel.iconmaskShapeBitmap = normalizedBitmap
                iconmaskPreview.setImageBitmap(selectedIconmask)
                IconPackGenerator.setSelectedIconmask(selectedIconmask)
                
                val statusText = findViewById<TextView>(R.id.iconmaskShapeStatus)
                updateIconmaskShapeStatus(statusText)
                
                Toast.makeText(this, getString(R.string.mask_updated), Toast.LENGTH_SHORT).show()
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
    
    // ✅ ELIMINADO: Todas las funciones de gestión de estilos (updateButtonStyles, etc.)
    // Los estilos ahora vienen completamente de XML
    
    private fun isCyberpunkThemeActive(): Boolean {
        val outValue = TypedValue()
        return theme.resolveAttribute(
            R.attr.isCyberpunkTheme,
            outValue,
            true
        ) && outValue.data == 1
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
                            
                            // ✅ CALCULAR SI ES FOREGOUND PRE-PROCESADO
                            val isForegroundPreprocessed = shouldUsePreprocessedForegroundForApp(nextApp.packageName)
                            
                            val config = IconThemer.ThemeConfig(
                                mask = selectedIconback!!,
                                color = selectedColor,
                                offsetX = offsetX,
                                offsetY = offsetY,
                                scalePercentage = scalePercentage,
                                foregroundScalePercentage = foregroundScalePercentage,
                                alphaPercentage = alphaPercentage,
                                colorIntensity = colorIntensity,
                                hue = hue,
                                saturation = saturation,
                                brightness = brightness,
                                contrast = contrast,
                                useDefaultIcon = useDefaultIcon,
                                useRoundIcon = useRoundIcon,
                                useForegroundLayer = useForegroundLayer,
                                useBackgroundLayer = useBackgroundLayer,
                                isForegroundPreprocessed = isForegroundPreprocessed,
                                viewModel = viewModel
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
                        // ✅ CALCULAR SI ES FOREGOUND PRE-PROCESADO
                        val isForegroundPreprocessed = shouldUsePreprocessedForegroundForApp(app.packageName)
                        
                        val config = IconThemer.ThemeConfig(
                            mask = selectedIconback!!,
                            color = selectedColor,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            scalePercentage = scalePercentage,
                            foregroundScalePercentage = foregroundScalePercentage,
                            alphaPercentage = alphaPercentage,
                            colorIntensity = colorIntensity,
                            hue = hue,
                            saturation = saturation,
                            brightness = brightness,
                            contrast = contrast,
                            useDefaultIcon = useDefaultIcon,
                            useRoundIcon = useRoundIcon,
                            useForegroundLayer = useForegroundLayer,
                            useBackgroundLayer = useBackgroundLayer,
                            isForegroundPreprocessed = isForegroundPreprocessed,
                            viewModel = viewModel
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
                
                // ✅ SIMPLIFICADO: Solo refrescar estados de botones
                refreshButtonStates()
                
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
                // ✅ CALCULAR SI ES FOREGOUND PRE-PROCESADO
                val isForegroundPreprocessed = shouldUsePreprocessedForegroundForApp(appPackage)
                
                val config = IconThemer.ThemeConfig(
                    mask = selectedIconback!!,
                    color = selectedColor,
                    offsetX = offsetX,
                    offsetY = offsetY,
                    scalePercentage = scalePercentage,
                    foregroundScalePercentage = foregroundScalePercentage,
                    alphaPercentage = alphaPercentage,
                    colorIntensity = colorIntensity,
                    hue = hue,
                    saturation = saturation,
                    brightness = brightness,
                    contrast = contrast,
                    useDefaultIcon = useDefaultIcon,
                    useRoundIcon = useRoundIcon,
                    useForegroundLayer = useForegroundLayer,
                    useBackgroundLayer = useBackgroundLayer,
                    isForegroundPreprocessed = isForegroundPreprocessed,
                    viewModel = viewModel
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
            Color.RED, 
            Color.GREEN, 
            Color.BLUE, 
            Color.CYAN, 
            Color.MAGENTA, 
            Color.YELLOW,
            Color.WHITE, 
            Color.BLACK, 
            Color.GRAY, 
            Color.parseColor("#FF5722"),
            Color.parseColor("#9C27B0"), 
            Color.parseColor("#2196F3"), 
            Color.parseColor("#4CAF50")
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
                    // Opción hexadecimal
                    showHexColorPicker(
                        initialColor = selectedColor,
                        onColorSelected = { color: Int ->
                            selectedColor = color
                            viewModel.selectedColor = selectedColor
                            updateButtonWithColor(colorPickerButton, selectedColor)
                            isColorApplied = true
                            updatePreview()
                            Toast.makeText(this, getString(R.string.hex_color_applied), Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    // ← CÓDIGO DIRECTO, SIN ASIGNAR A VARIABLE
                    selectedColor = colors[which]
                    viewModel.selectedColor = selectedColor
                    updateButtonWithColor(colorPickerButton, selectedColor)
                    isColorApplied = true
                    updatePreview()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    // ✅ FUNCIÓN REUTILIZABLE PARA SELECTOR DE COLOR HEXADECIMAL
    private fun showHexColorPicker(
        title: String = getString(R.string.hex_color_picker_title),
        initialColor: Int = Color.CYAN,
        onColorSelected: (Int) -> Unit
    ) {
        val editText = EditText(this).apply {
            hint = getString(R.string.hex_color_hint)
            // Convertir el color inicial a formato hexadecimal
            val hexColor = String.format("#%08X", initialColor)
            setText(hexColor)
            setSelectAllOnFocus(true)
        }
        
        val colorPreview = ImageView(this).apply {
            setBackgroundColor(initialColor)
            setPadding(32, 32, 32, 32)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                100
            )
        }
        
        fun updateColorPreview(hexColor: String) {
            try {
                val color = Color.parseColor(hexColor)
                colorPreview.setBackgroundColor(color)
            } catch (e: Exception) {
                colorPreview.setBackgroundColor(initialColor)
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
        
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok) { dialog, _ ->
                val hexColor = editText.text.toString().trim()
                if (isValidHexColor(hexColor)) {
                    try {
                        val selectedColor = Color.parseColor(hexColor)
                        onColorSelected(selectedColor)
                        Toast.makeText(this, getString(R.string.hex_color_applied), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, getString(R.string.invalid_hex_color), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, getString(R.string.invalid_hex_format), Toast.LENGTH_LONG).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel) { dialog, _ ->
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
    
    // picker para máscara de recorte
    private fun openMaskImagePicker() {
        maskPickerLauncher.launch("image/*")
    }
    
    // ✅ NUEVA SECCIÓN: FILTROS AVANZADOS INTEGRADOS
    private fun setupAdvancedFiltersSection() {
        // Configurar toggle de filtros avanzados
        val advancedFiltersToggle = findViewById<SwitchMaterial>(R.id.advancedFiltersToggle)
        advancedFiltersToggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.advancedFiltersEnabled = isChecked
            updateAdvancedFiltersVisibility(isChecked)
            updatePreview()
        }
        
        // Configurar todos los controles de filtros avanzados
        setupMaskControls()
        setupEdgeEnhancementControls()
        setupChromaticAberrationControls()
        setupSphereEffectControls()
        setupEmbossControls()
        setupGlowControls()
        setupSoftMaskControls()
        setupRotationControls()
        setupShadowControls()
        setupBorderControls()
        setupPixelateControls()
        setupCartoonControls()
        setupNoiseControls()
        setupFisheyeControls()
        setupBorderColorPicker()
        setupImageScaleControls()
        setupIconColorizationControls()
        setupIconmaskShapeControls()
        
        // Configurar botón de reset
        val resetFiltersButton = findViewById<MaterialButton>(R.id.resetFiltersButton)
        resetFiltersButton.setOnClickListener {
            resetAllAdvancedFilters()
        }
        
        // Estado inicial
        updateAdvancedFiltersVisibility(advancedFiltersToggle.isChecked)
    }
    
    private fun updateAdvancedFiltersVisibility(show: Boolean) {
        val filtersContent = findViewById<LinearLayout>(R.id.advancedFiltersContent)
        filtersContent.visibility = if (show) View.VISIBLE else View.GONE
    }
    
    private fun setupIconmaskShapeControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.iconmaskShapeToggle)
        val statusText = findViewById<TextView>(R.id.iconmaskShapeStatus)
        
        // Estado inicial
        toggle.isChecked = viewModel.iconmaskShapeEnabled
        updateIconmaskShapeStatus(statusText)
        
        toggle.setOnCheckedChangeListener { _, enabled ->
            viewModel.iconmaskShapeEnabled = enabled
            updateIconmaskShapeStatus(statusText)
            updatePreview()
        }
    }
    
    private fun updateIconmaskShapeStatus(statusText: TextView) {
        val hasBitmap = viewModel.iconmaskShapeBitmap != null
        val isEnabled = viewModel.iconmaskShapeEnabled
        
        if (!hasBitmap) {
            statusText.text = getString(R.string.shape_mask_status_not_loaded)
            statusText.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        } else if (isEnabled) {
            statusText.text = getString(R.string.shape_mask_status_active)
            statusText.setTextColor(ContextCompat.getColor(this, R.color.success))
        } else {
            statusText.text = getString(R.string.shape_mask_status_ready)
            statusText.setTextColor(ContextCompat.getColor(this, R.color.text_primary))
        }
    }
    
    private fun setupMaskControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.maskToggle)
        val button = findViewById<MaterialButton>(R.id.loadMaskButton)
        val maskScaleControls = findViewById<LinearLayout>(R.id.maskScaleControls)
        val maskScaleSeekBar = findViewById<SeekBar>(R.id.maskScaleSeekBar)
        val maskScaleValue = findViewById<TextView>(R.id.maskScaleValue)
        
        // Estado inicial
        toggle.isChecked = viewModel.maskEnabled
        button.isEnabled = viewModel.maskEnabled
        maskScaleSeekBar.progress = viewModel.maskScalePercentage
        maskScaleValue.text = getString(R.string.mask_scale_percent, maskScaleSeekBar.progress)
        
        // ✅ CORRECCIÓN: Inicializar maskScaleEnabled basado en si hay una máscara cargada
        viewModel.maskScaleEnabled = viewModel.maskBitmap != null && viewModel.maskEnabled
        
        // Mostrar/ocultar controles de escala según el toggle
        updateMaskScaleVisibility(viewModel.maskEnabled && viewModel.maskBitmap != null)
        
        toggle.setOnCheckedChangeListener { _, enabled ->
            viewModel.maskEnabled = enabled
            button.isEnabled = enabled
            // ✅ CORRECCIÓN: Actualizar maskScaleEnabled cuando se activa/desactiva la máscara
            viewModel.maskScaleEnabled = enabled && viewModel.maskBitmap != null
            updateMaskScaleVisibility(enabled && viewModel.maskBitmap != null)
            updatePreview()
        }
        
        button.setOnClickListener {
            openMaskImagePicker()
        }
        
        maskScaleSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.maskScalePercentage = progress
                // ✅ CORRECCIÓN: Activar el escalado cuando se cambia el valor
                viewModel.maskScaleEnabled = viewModel.maskEnabled && progress != 100
                maskScaleValue.text = getString(R.string.mask_scale_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    // ✅ NUEVO: Método para mostrar/ocultar controles de escala
    private fun updateMaskScaleVisibility(show: Boolean) {
        val scaleControls = findViewById<LinearLayout>(R.id.maskScaleControls)
        // ✅ CORRECCIÓN: Mostrar controles solo si hay máscara cargada y está habilitada
        scaleControls.visibility = if (show && viewModel.maskBitmap != null) View.VISIBLE else View.GONE
    }
    
    private fun setupEdgeEnhancementControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.edgeEnhanceToggle)
        val seekBar = findViewById<SeekBar>(R.id.edgeEnhanceSeekBar)
        val valueText = findViewById<TextView>(R.id.edgeEnhanceValue)
        
        toggle.isChecked = viewModel.edgeEnhanceEnabled
        seekBar.progress = (viewModel.edgeEnhanceIntensity * 100).toInt()
        valueText.text = getString(R.string.intensity_percent, seekBar.progress)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.edgeEnhanceEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.edgeEnhanceIntensity = progress / 100f
                valueText.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupBorderColorPicker() {
        // ✅ ELIMINAR la declaración local - usar la variable de clase directamente
        // val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
        
        // ✅ Usar la variable de clase directamente (ya está inicializada en initViews())
        updateButtonWithColor(borderColorButton, viewModel.borderColor)
        
        // ✅ Configurar el click listener usando la variable de clase
        borderColorButton.setOnClickListener {
            showBorderColorPickerDialog()
        }
    }
    
    // ✅ MÉTODO PARA EL DIÁLOGO DE COLORES DEL EFECTO BORDE
    private fun showBorderColorPickerDialog() {
        val colors = intArrayOf(
            Color.RED, 
            Color.GREEN, 
            Color.BLUE, 
            Color.CYAN, 
            Color.MAGENTA, 
            Color.YELLOW,
            Color.WHITE, 
            Color.BLACK, 
            Color.GRAY, 
            Color.parseColor("#FF5722"),
            Color.parseColor("#9C27B0"), 
            Color.parseColor("#2196F3"), 
            Color.parseColor("#4CAF50")
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
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_border_color))
            .setItems(colorNames) { dialog, which ->
                if (which == colorNames.size - 1) {
                    showHexColorPicker(
                        title = getString(R.string.select_border_color),
                        initialColor = viewModel.borderColor,
                        onColorSelected = { color: Int ->
                            viewModel.borderColor = color
                            //val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
                            //if (borderColorButton != null) {
                                updateButtonWithColor(borderColorButton, color)
                                updatePreview()
                            //}
                        }
                    )
                } else {
                    //val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
                    //if (borderColorButton != null) {
                        viewModel.borderColor = colors[which]
                        updateButtonWithColor(borderColorButton, colors[which])
                        updatePreview()
                    //}
                }
                
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun setupChromaticAberrationControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.chromaticToggle)
        val intensitySeekBar = findViewById<SeekBar>(R.id.chromaticIntensitySeekBar)
        val intensityValue = findViewById<TextView>(R.id.chromaticIntensityValue)
        val redSeekBar = findViewById<SeekBar>(R.id.chromaticRedSeekBar)
        val greenSeekBar = findViewById<SeekBar>(R.id.chromaticGreenSeekBar)
        val blueSeekBar = findViewById<SeekBar>(R.id.chromaticBlueSeekBar)
        
        toggle.isChecked = viewModel.chromaticAberrationEnabled
        intensitySeekBar.progress = (viewModel.chromaticIntensity * 100).toInt()
        intensityValue.text = getString(R.string.intensity_percent, intensitySeekBar.progress)
        redSeekBar.progress = viewModel.chromaticRedOffset + 10
        greenSeekBar.progress = viewModel.chromaticGreenOffset + 10
        blueSeekBar.progress = viewModel.chromaticBlueOffset + 10
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.chromaticAberrationEnabled = isChecked
            updatePreview()
        }
        
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.chromaticIntensity = progress / 100f
                intensityValue.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        setupChromaticOffsetSeekBar(redSeekBar) { offset -> viewModel.chromaticRedOffset = offset }
        setupChromaticOffsetSeekBar(greenSeekBar) { offset -> viewModel.chromaticGreenOffset = offset }
        setupChromaticOffsetSeekBar(blueSeekBar) { offset -> viewModel.chromaticBlueOffset = offset }
    }
    
    private fun setupChromaticOffsetSeekBar(seekBar: SeekBar, onOffsetChanged: (Int) -> Unit) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val offset = progress - 10
                onOffsetChanged(offset)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupSphereEffectControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.sphereToggle)
        val seekBar = findViewById<SeekBar>(R.id.sphereSeekBar)
        val valueText = findViewById<TextView>(R.id.sphereValue)
        
        toggle.isChecked = viewModel.sphereEffectEnabled
        seekBar.progress = (viewModel.sphereStrength * 100).toInt()
        valueText.text = getString(R.string.strength_percent, seekBar.progress)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.sphereEffectEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.sphereStrength = progress / 100f
                valueText.text = getString(R.string.strength_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupEmbossControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.embossToggle)
        val intensitySeekBar = findViewById<SeekBar>(R.id.embossIntensitySeekBar)
        val intensityValue = findViewById<TextView>(R.id.embossIntensityValue)
        val azimuthSeekBar = findViewById<SeekBar>(R.id.embossAzimuthSeekBar)
        
        toggle.isChecked = viewModel.embossEffectEnabled
        intensitySeekBar.progress = (viewModel.embossIntensity * 100).toInt()
        intensityValue.text = getString(R.string.intensity_percent, intensitySeekBar.progress)
        azimuthSeekBar.progress = viewModel.embossAzimuth.toInt()
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.embossEffectEnabled = isChecked
            updatePreview()
        }
        
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.embossIntensity = progress / 100f
                intensityValue.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        azimuthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.embossAzimuth = progress.toFloat()
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupGlowControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.glowToggle)
        val intensitySeekBar = findViewById<SeekBar>(R.id.glowIntensitySeekBar)
        val intensityValue = findViewById<TextView>(R.id.glowIntensityValue)
        val radiusSeekBar = findViewById<SeekBar>(R.id.glowRadiusSeekBar)
        
        toggle.isChecked = viewModel.glowEffectEnabled
        intensitySeekBar.progress = (viewModel.glowIntensity * 100).toInt()
        intensityValue.text = getString(R.string.intensity_percent, intensitySeekBar.progress)
        radiusSeekBar.progress = viewModel.glowRadius
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.glowEffectEnabled = isChecked
            updatePreview()
        }
        
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.glowIntensity = progress / 100f
                intensityValue.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.glowRadius = progress
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupSoftMaskControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.softMaskToggle)
        val seekBar = findViewById<SeekBar>(R.id.softMaskSeekBar)
        val valueText = findViewById<TextView>(R.id.softMaskValue)
        
        toggle.isChecked = viewModel.softMaskEnabled
        seekBar.progress = (viewModel.softMaskIntensity * 100).toInt()
        valueText.text = getString(R.string.intensity_percent, seekBar.progress)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.softMaskEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.softMaskIntensity = progress / 100f
                valueText.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupRotationControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.rotationToggle)
        val seekBar = findViewById<SeekBar>(R.id.rotationSeekBar)
        val valueText = findViewById<TextView>(R.id.rotationValue)
        
        toggle.isChecked = viewModel.rotationEnabled
        seekBar.progress = (viewModel.rotationAngle * 100 / 360).toInt()
        valueText.text = getString(R.string.rotation_degrees, viewModel.rotationAngle)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.rotationEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.rotationAngle = progress * 360f / 100f
                valueText.text = getString(R.string.rotation_degrees, viewModel.rotationAngle)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupShadowControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.shadowToggle)
        val intensitySeekBar = findViewById<SeekBar>(R.id.shadowIntensitySeekBar)
        val intensityValue = findViewById<TextView>(R.id.shadowIntensityValue)
        val radiusSeekBar = findViewById<SeekBar>(R.id.shadowRadiusSeekBar)
        val offsetXSeekBar = findViewById<SeekBar>(R.id.shadowOffsetXSeekBar)
        val offsetYSeekBar = findViewById<SeekBar>(R.id.shadowOffsetYSeekBar)
        
        toggle.isChecked = viewModel.shadowEnabled
        intensitySeekBar.progress = (viewModel.shadowIntensity * 100).toInt()
        intensityValue.text = getString(R.string.intensity_percent, intensitySeekBar.progress)
        radiusSeekBar.progress = viewModel.shadowRadius
        offsetXSeekBar.progress = viewModel.shadowOffsetX + 10
        offsetYSeekBar.progress = viewModel.shadowOffsetY + 10
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.shadowEnabled = isChecked
            updatePreview()
        }
        
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.shadowIntensity = progress / 100f
                intensityValue.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        radiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.shadowRadius = progress
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        offsetXSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.shadowOffsetX = progress - 10
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        offsetYSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.shadowOffsetY = progress - 10
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupBorderControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.borderToggle)
        val innerSeek = findViewById<SeekBar>(R.id.borderInnerSeekBar)
        val outerSeek = findViewById<SeekBar>(R.id.borderOuterSeekBar)
        val innerText = findViewById<TextView>(R.id.borderInnerValue)
        val outerText = findViewById<TextView>(R.id.borderOuterValue)
    
        // Estado inicial
        toggle.isChecked = viewModel.borderEnabled
        innerSeek.progress = viewModel.borderInnerWidth
        outerSeek.progress = viewModel.borderOuterWidth
    
        innerText.text = "Interior: ${viewModel.borderInnerWidth}px"
        outerText.text = "Exterior: ${viewModel.borderOuterWidth}px"
    
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.borderEnabled = isChecked
            updatePreview()
        }
    
        innerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                viewModel.borderInnerWidth = value
                innerText.text = "Interior: ${value}px"
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    
        outerSeek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                viewModel.borderOuterWidth = value
                outerText.text = "Exterior: ${value}px"
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
    }
    
    private fun setupPixelateControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.pixelateToggle)
        val seekBar = findViewById<SeekBar>(R.id.pixelateSeekBar)
        val valueText = findViewById<TextView>(R.id.pixelateValue)
        
        toggle.isChecked = viewModel.pixelateEnabled
        seekBar.progress = viewModel.pixelateSize
        valueText.text = getString(R.string.pixel_size, viewModel.pixelateSize)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.pixelateEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.pixelateSize = progress
                valueText.text = getString(R.string.pixel_size, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupCartoonControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.cartoonToggle)
        val seekBar = findViewById<SeekBar>(R.id.cartoonSeekBar)
        val valueText = findViewById<TextView>(R.id.cartoonValue)
        
        toggle.isChecked = viewModel.cartoonEnabled
        seekBar.progress = (viewModel.cartoonIntensity * 100).toInt()
        valueText.text = getString(R.string.intensity_percent, seekBar.progress)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.cartoonEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.cartoonIntensity = progress / 100f
                valueText.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupNoiseControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.noiseToggle)
        val seekBar = findViewById<SeekBar>(R.id.noiseSeekBar)
        val valueText = findViewById<TextView>(R.id.noiseValue)
        
        toggle.isChecked = viewModel.noiseEnabled
        seekBar.progress = (viewModel.noiseIntensity * 100).toInt()
        valueText.text = getString(R.string.intensity_percent, seekBar.progress)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.noiseEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.noiseIntensity = progress / 100f
                valueText.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    private fun setupFisheyeControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.fisheyeToggle)
        val seekBar = findViewById<SeekBar>(R.id.fisheyeSeekBar)
        val valueText = findViewById<TextView>(R.id.fisheyeValue)
        
        toggle.isChecked = viewModel.fisheyeEnabled
        seekBar.progress = (viewModel.fisheyeStrength * 100).toInt()
        valueText.text = getString(R.string.strength_percent, seekBar.progress)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.fisheyeEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.fisheyeStrength = progress / 100f
                valueText.text = getString(R.string.strength_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    // ✅ NUEVO: Configurar controles de Escala IC
    private fun setupImageScaleControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.imageScaleToggle)
        val seekBar = findViewById<SeekBar>(R.id.imageScaleSeekBar)
        val valueText = findViewById<TextView>(R.id.imageScaleValue)
        
        toggle.isChecked = viewModel.imageScaleEnabled
        seekBar.progress = viewModel.imageScalePercentage
        valueText.text = getString(R.string.image_scale_percent, seekBar.progress)
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.imageScaleEnabled = isChecked
            updatePreview()
        }
        
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.imageScalePercentage = progress
                valueText.text = getString(R.string.image_scale_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }
    
    // ✅ NUEVO: Configurar controles de Colorización
    private fun setupIconColorizationControls() {
        val toggle = findViewById<SwitchMaterial>(R.id.iconColorizationToggle)
        val intensitySeekBar = findViewById<SeekBar>(R.id.colorizationIntensitySeekBar)
        val intensityValue = findViewById<TextView>(R.id.colorizationIntensityValue)
        val colorButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
        
        toggle.isChecked = viewModel.iconColorizationEnabled
        intensitySeekBar.progress = viewModel.iconColorizationIntensity
        intensityValue.text = getString(R.string.intensity_percent, intensitySeekBar.progress)
        
        // ✅ CORREGIDO: Usar updateButtonWithColor en lugar de setBackgroundColor
        colorButton?.let {
            updateButtonWithColor(it, viewModel.iconColorizationColor)
        }
        
        toggle.setOnCheckedChangeListener { _, isChecked ->
            viewModel.iconColorizationEnabled = isChecked
            updatePreview()
        }
        
        intensitySeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                viewModel.iconColorizationIntensity = progress
                intensityValue.text = getString(R.string.intensity_percent, progress)
                if (fromUser) updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        
        colorButton?.setOnClickListener {
            showColorizationColorPicker()
        }
    }
    
    // ✅ NUEVO: Selector de color para colorización
    private fun showColorizationColorPicker() {
        val colors = intArrayOf(
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW,
            Color.WHITE, Color.BLACK, Color.GRAY, Color.parseColor("#FF5722"),
            Color.parseColor("#9C27B0"), Color.parseColor("#2196F3"), Color.parseColor("#4CAF50")
        )
        
        val colorNames = arrayOf(
            getString(R.string.color_red), getString(R.string.color_green), getString(R.string.color_blue),
            getString(R.string.color_cyan), getString(R.string.color_magenta), getString(R.string.color_yellow),
            getString(R.string.color_white), getString(R.string.color_black), getString(R.string.color_gray),
            getString(R.string.color_orange), getString(R.string.color_purple), getString(R.string.color_light_blue),
            getString(R.string.color_light_green), getString(R.string.color_hexadecimal)
        )
        
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.select_colorization_color))
            .setItems(colorNames) { dialog, which ->
                if (which == colorNames.size - 1) {
                    showHexColorPicker(
                        title = getString(R.string.select_colorization_color),
                        initialColor = viewModel.iconColorizationColor,
                        onColorSelected = { color: Int ->
                            viewModel.iconColorizationColor = color
                            //val colorizationButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
                            //if (colorizationButton != null) {
                                updateButtonWithColor(colorizationButton, color)
                                updatePreview()
                            //}
                        }
                    )
                } else {
                    viewModel.iconColorizationColor = colors[which]
                    //val colorizationButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
                    //if (colorizationButton != null) {
                        updateButtonWithColor(colorizationButton, colors[which])
                        updatePreview()
                    //}
                }
                dialog.dismiss()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }
    
    private fun resetAllAdvancedFilters() {
        viewModel.resetAdvancedFilters()
        
        // Resetear todos los controles UI
        findViewById<SwitchMaterial>(R.id.advancedFiltersToggle).isChecked = false
        
        // Control de Escala de Máscara
        findViewById<SeekBar>(R.id.maskScaleSeekBar).progress = 100
        findViewById<TextView>(R.id.maskScaleValue).text = getString(R.string.mask_scale_percent, 100)
        updateMaskScaleVisibility(false)
        
        // Edge Enhance
        findViewById<SwitchMaterial>(R.id.edgeEnhanceToggle).isChecked = false
        findViewById<SeekBar>(R.id.edgeEnhanceSeekBar).progress = 100
        findViewById<TextView>(R.id.edgeEnhanceValue).text = getString(R.string.intensity_percent, 100)
        
        // Chromatic Aberration
        findViewById<SwitchMaterial>(R.id.chromaticToggle).isChecked = false
        findViewById<SeekBar>(R.id.chromaticIntensitySeekBar).progress = 50
        findViewById<TextView>(R.id.chromaticIntensityValue).text = getString(R.string.intensity_percent, 50)
        findViewById<SeekBar>(R.id.chromaticRedSeekBar).progress = 12
        findViewById<SeekBar>(R.id.chromaticGreenSeekBar).progress = 10
        findViewById<SeekBar>(R.id.chromaticBlueSeekBar).progress = 8
        
        // Sphere Effect
        findViewById<SwitchMaterial>(R.id.sphereToggle).isChecked = false
        findViewById<SeekBar>(R.id.sphereSeekBar).progress = 30
        findViewById<TextView>(R.id.sphereValue).text = getString(R.string.strength_percent, 30)
        
        // Emboss Effect
        findViewById<SwitchMaterial>(R.id.embossToggle).isChecked = false
        findViewById<SeekBar>(R.id.embossIntensitySeekBar).progress = 50
        findViewById<TextView>(R.id.embossIntensityValue).text = getString(R.string.intensity_percent, 50)
        findViewById<SeekBar>(R.id.embossAzimuthSeekBar).progress = 45
        
        // Glow Effect
        findViewById<SwitchMaterial>(R.id.glowToggle).isChecked = false
        findViewById<SeekBar>(R.id.glowIntensitySeekBar).progress = 30
        findViewById<TextView>(R.id.glowIntensityValue).text = getString(R.string.intensity_percent, 30)
        findViewById<SeekBar>(R.id.glowRadiusSeekBar).progress = 10
        
        // Soft Mask
        findViewById<SwitchMaterial>(R.id.softMaskToggle).isChecked = false
        findViewById<SeekBar>(R.id.softMaskSeekBar).progress = 50
        findViewById<TextView>(R.id.softMaskValue).text = getString(R.string.intensity_percent, 50)
        
        // Rotation
        findViewById<SwitchMaterial>(R.id.rotationToggle).isChecked = false
        findViewById<SeekBar>(R.id.rotationSeekBar).progress = 0
        findViewById<TextView>(R.id.rotationValue).text = getString(R.string.rotation_degrees, 0f)
        
        // Shadow
        findViewById<SwitchMaterial>(R.id.shadowToggle).isChecked = false
        findViewById<SeekBar>(R.id.shadowIntensitySeekBar).progress = 70
        findViewById<TextView>(R.id.shadowIntensityValue).text = getString(R.string.intensity_percent, 70)
        findViewById<SeekBar>(R.id.shadowRadiusSeekBar).progress = 15
        findViewById<SeekBar>(R.id.shadowOffsetXSeekBar).progress = 15
        findViewById<SeekBar>(R.id.shadowOffsetYSeekBar).progress = 15
        
        // Border
        findViewById<SwitchMaterial>(R.id.borderToggle).isChecked = false
        findViewById<SeekBar>(R.id.borderInnerSeekBar).progress = 3
        findViewById<TextView>(R.id.borderInnerValue).text = getString(R.string.border_inner_px, 3)
        findViewById<SeekBar>(R.id.borderOuterSeekBar).progress = 6
        findViewById<TextView>(R.id.borderOuterValue).text = getString(R.string.border_outer_px, 6)
        
        // Pixelate
        findViewById<SwitchMaterial>(R.id.pixelateToggle).isChecked = false
        findViewById<SeekBar>(R.id.pixelateSeekBar).progress = 8
        findViewById<TextView>(R.id.pixelateValue).text = getString(R.string.pixel_size, 8)
        
        // Cartoon
        findViewById<SwitchMaterial>(R.id.cartoonToggle).isChecked = false
        findViewById<SeekBar>(R.id.cartoonSeekBar).progress = 70
        findViewById<TextView>(R.id.cartoonValue).text = getString(R.string.intensity_percent, 70)
        
        // Noise
        findViewById<SwitchMaterial>(R.id.noiseToggle).isChecked = false
        findViewById<SeekBar>(R.id.noiseSeekBar).progress = 10
        findViewById<TextView>(R.id.noiseValue).text = getString(R.string.intensity_percent, 10)
        
        // Fisheye
        findViewById<SwitchMaterial>(R.id.fisheyeToggle).isChecked = false
        findViewById<SeekBar>(R.id.fisheyeSeekBar).progress = 50
        findViewById<TextView>(R.id.fisheyeValue).text = getString(R.string.strength_percent, 50)
        
        // Scale IC
        findViewById<SwitchMaterial>(R.id.imageScaleToggle).isChecked = false
        findViewById<SeekBar>(R.id.imageScaleSeekBar).progress = 100
        findViewById<TextView>(R.id.imageScaleValue).text = getString(R.string.image_scale_percent, 100)
        
        // Colorization IC
        findViewById<SwitchMaterial>(R.id.iconColorizationToggle).isChecked = false
        findViewById<SeekBar>(R.id.colorizationIntensitySeekBar).progress = 100
        findViewById<TextView>(R.id.colorizationIntensityValue).text = getString(R.string.intensity_percent, 100)
        
        // Resetear botones de color
        // ✅ CORREGIDO: Usar updateButtonWithColor en lugar de setBackgroundColor
        //val colorizationButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
        //val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
       
        updateButtonWithColor(colorizationButton, Color.CYAN)
        updateButtonWithColor(borderColorButton, Color.WHITE)
            
        //colorizationButton?.let { updateButtonWithColor(it, Color.CYAN) }
        //borderColorButton?.let { updateButtonWithColor(it, Color.WHITE) }
        
        updateAdvancedFiltersVisibility(false)
        updatePreview()
        
        Toast.makeText(this, "Todos los filtros avanzados reseteados", Toast.LENGTH_SHORT).show()
    }
    
    // ActivityResult Launchers para tematización
    private val importThemingLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importThemingFromUri(it) }
    }
    
    private val exportThemingLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        uri?.let { exportThemingToUri(it) }
    }
    
    // Métodos para exportación/importación
    private fun exportTheming() {
        if (selectedIconback == null) {
            Toast.makeText(this, "Primero carga al menos un fondo (iconback)", Toast.LENGTH_SHORT).show()
            return
        }
        
        // ✅ MOSTRAR DIÁLOGO PARA METADATOS
        showThemingMetadataDialog()
    }
    
    private fun showThemingMetadataDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_theming_metadata, null)
        val editName = dialogView.findViewById<EditText>(R.id.editThemingName)
        val editAuthor = dialogView.findViewById<EditText>(R.id.editThemingAuthor)
        val editDescription = dialogView.findViewById<EditText>(R.id.editThemingDescription)
        
        // Valores por defecto
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        editName.setText("Mi Tematización ${timestamp}")
        editAuthor.setText("Usuario")
        editDescription.setText("Configuración exportada desde App Icon Scraper & Themer")
        
        AlertDialog.Builder(this)
            .setTitle("Información de la Tematización")
            .setView(dialogView)
            .setPositiveButton("Exportar") { dialog, _ ->
                val name = editName.text.toString().trim()
                val author = editAuthor.text.toString().trim()
                val description = editDescription.text.toString().trim()
                
                if (name.isNotEmpty()) {
                    // ✅ PASAR METADATOS A LA EXPORTACIÓN
                    exportThemingWithMetadata(name, author, description)
                } else {
                    Toast.makeText(this, "El nombre es requerido", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }
    
    private fun exportThemingWithMetadata(name: String, author: String, description: String) {
        val fileName = "${name.replace(" ", "_")}_${System.currentTimeMillis()}.zip"
        // ✅ GUARDAR METADATOS TEMPORALMENTE
        this.themingName = name
        this.themingAuthor = author
        this.themingDescription = description
        
        exportThemingLauncher.launch(fileName)
    }
    
    private fun importTheming() {
        importThemingLauncher.launch("application/zip")
    }
    
    private fun exportThemingToUri(uri: Uri) {
        Thread {
            try {
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val success = ThemingSerializer.exportTheming(
                        viewModel = viewModel,
                        iconback = selectedIconback,
                        iconmask = selectedIconmask,
                        iconupon = selectedIconupon,
                        iconclippingmask = viewModel.maskBitmap,
                        name = themingName,
                        author = themingAuthor,
                        description = themingDescription,
                        outputStream = outputStream
                    )
                    
                    // Limpiar metadatos temporales
                    themingName = ""
                    themingAuthor = ""
                    themingDescription = ""
                    
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, getString(R.string.export_theming_success), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, getString(R.string.theming_export_error), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "${getString(R.string.theming_export_error)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    
    private fun importThemingFromUri(uri: Uri) {
        Thread {
            try {
                contentResolver.openInputStream(uri)?.use { inputStream ->
                    val themingConfig = ThemingSerializer.importTheming(inputStream)
                    
                    runOnUiThread {
                        if (themingConfig != null) {
                            applyThemingConfig(themingConfig)
                            Toast.makeText(this, getString(R.string.import_theming_success), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, getString(R.string.theming_file_corrupted), Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "${getString(R.string.theming_import_error)}: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }
    
    private fun applyThemingConfig(config: ThemingSerializer.ThemingConfig) {
        // ✅ Aplicar configuración básica al ViewModel
        with(config.basicSettings) {
            viewModel.offsetX = offsetX
            viewModel.offsetY = offsetY
            viewModel.scalePercentage = scalePercentage
            viewModel.foregroundScalePercentage = foregroundScalePercentage
            viewModel.alphaPercentage = alphaPercentage
            viewModel.colorIntensity = colorIntensity
            viewModel.selectedColor = selectedColor
            viewModel.hue = hue
            viewModel.saturation = saturation
            viewModel.brightness = brightness
            viewModel.contrast = contrast
            viewModel.useDefaultIcon = useDefaultIcon
            viewModel.useRoundIcon = useRoundIcon
            viewModel.useForegroundLayer = useForegroundLayer
            viewModel.useBackgroundLayer = useBackgroundLayer
        }
    
        // ✅ Aplicar configuración básica a variables locales
        with(config.basicSettings) {
            this@ThemeCustomizationActivity.offsetX = offsetX
            this@ThemeCustomizationActivity.offsetY = offsetY
            this@ThemeCustomizationActivity.scalePercentage = scalePercentage
            this@ThemeCustomizationActivity.foregroundScalePercentage = foregroundScalePercentage
            this@ThemeCustomizationActivity.alphaPercentage = alphaPercentage
            this@ThemeCustomizationActivity.colorIntensity = colorIntensity
            this@ThemeCustomizationActivity.selectedColor = selectedColor
            this@ThemeCustomizationActivity.hue = hue
            this@ThemeCustomizationActivity.saturation = saturation
            this@ThemeCustomizationActivity.brightness = brightness
            this@ThemeCustomizationActivity.contrast = contrast
            this@ThemeCustomizationActivity.useDefaultIcon = useDefaultIcon
            this@ThemeCustomizationActivity.useRoundIcon = useRoundIcon
            this@ThemeCustomizationActivity.useForegroundLayer = useForegroundLayer
            this@ThemeCustomizationActivity.useBackgroundLayer = useBackgroundLayer
        }
    
        // ✅ Aplicar filtros avanzados
        with(config.advancedFilters) {
            viewModel.advancedFiltersEnabled = advancedFiltersEnabled
            viewModel.edgeEnhanceEnabled = edgeEnhanceEnabled
            viewModel.edgeEnhanceIntensity = edgeEnhanceIntensity
            viewModel.chromaticAberrationEnabled = chromaticAberrationEnabled
            viewModel.chromaticIntensity = chromaticIntensity
            viewModel.chromaticRedOffset = chromaticRedOffset
            viewModel.chromaticGreenOffset = chromaticGreenOffset
            viewModel.chromaticBlueOffset = chromaticBlueOffset
            viewModel.sphereEffectEnabled = sphereEffectEnabled
            viewModel.sphereStrength = sphereStrength
            viewModel.embossEffectEnabled = embossEffectEnabled
            viewModel.embossIntensity = embossIntensity
            viewModel.embossAzimuth = embossAzimuth
            viewModel.glowEffectEnabled = glowEffectEnabled
            viewModel.glowIntensity = glowIntensity
            viewModel.glowRadius = glowRadius
            viewModel.softMaskEnabled = softMaskEnabled
            viewModel.softMaskIntensity = softMaskIntensity
            viewModel.rotationEnabled = rotationEnabled
            viewModel.rotationAngle = rotationAngle
            viewModel.shadowEnabled = shadowEnabled
            viewModel.shadowIntensity = shadowIntensity
            viewModel.shadowRadius = shadowRadius
            viewModel.shadowOffsetX = shadowOffsetX
            viewModel.shadowOffsetY = shadowOffsetY
            viewModel.borderEnabled = borderEnabled
            viewModel.borderInnerWidth = borderInnerWidth
            viewModel.borderOuterWidth = borderOuterWidth
            viewModel.borderColor = borderColor
            viewModel.pixelateEnabled = pixelateEnabled
            viewModel.pixelateSize = pixelateSize
            viewModel.cartoonEnabled = cartoonEnabled
            viewModel.cartoonIntensity = cartoonIntensity
            viewModel.noiseEnabled = noiseEnabled
            viewModel.noiseIntensity = noiseIntensity
            viewModel.fisheyeEnabled = fisheyeEnabled
            viewModel.fisheyeStrength = fisheyeStrength
            viewModel.maskEnabled = maskEnabled
            viewModel.maskScalePercentage = maskScalePercentage
            viewModel.imageScaleEnabled = imageScaleEnabled
            viewModel.imageScalePercentage = imageScalePercentage
            viewModel.iconColorizationEnabled = iconColorizationEnabled
            viewModel.iconColorizationColor = iconColorizationColor
            viewModel.iconColorizationIntensity = iconColorizationIntensity
            viewModel.iconmaskShapeEnabled = iconmaskShapeEnabled
        }
    
        // ✅ Aplicar imágenes
        config.iconbackBitmap?.let { 
            selectedIconback = it
            viewModel.selectedIconback = it
            iconbackPreview.setImageBitmap(it)
            IconPackGenerator.setSelectedIconback(it)
        }
        
        config.iconmaskBitmap?.let { 
            selectedIconmask = it
            viewModel.selectedIconmask = it
            viewModel.iconmaskShapeBitmap = it
            iconmaskPreview.setImageBitmap(it)
            IconPackGenerator.setSelectedIconmask(it)
            
            // Actualizar estado del recorte de forma
            val statusText = findViewById<TextView>(R.id.iconmaskShapeStatus)
            updateIconmaskShapeStatus(statusText)
        }
        
        config.iconuponBitmap?.let { 
            selectedIconupon = it
            viewModel.selectedIconupon = it
            iconuponPreview.setImageBitmap(it)
            IconPackGenerator.setSelectedIconupon(it)
        }
        
        config.iconclippingmaskBitmap?.let {
            viewModel.maskBitmap = it
        }
    
        // ✅ Actualizar color del botón
        colorPickerButton.setBackgroundColor(selectedColor)
    
        // ✅ Actualizar toda la UI
        updateSeekBars()
        updatePreview()
        
        // ✅ ACTUALIZAR EL TOGGLE PRINCIPAL EN LA UI
        val advancedFiltersToggle = findViewById<SwitchMaterial>(R.id.advancedFiltersToggle)
        advancedFiltersToggle.isChecked = viewModel.advancedFiltersEnabled
        
        // ✅ Forzar actualización de todos los controles avanzados
        setupAdvancedFiltersSection()
        
        // ✅ Aplicar sistema de temas nuevamente
        applyThemeToAllComponents()

        // ✅ Actualizar colores de botones después de aplicar tematización
        colorPickerButton.setBackgroundColor(selectedColor)
        
        // ✅ Actualizar color del botón
        updateButtonWithColor(colorPickerButton, selectedColor)
        
        //val colorizationButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
        //val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
        
        //colorizationButton?.let { updateButtonWithColor(it, viewModel.iconColorizationColor) }
        //borderColorButton?.let { updateButtonWithColor(it, viewModel.borderColor) }
        updateButtonWithColor(borderColorButton, viewModel.borderColor)
        updateButtonWithColor(colorizationButton, viewModel.iconColorizationColor)
        
        /*val colorizationButton = findViewById<MaterialButton>(R.id.colorizationColorButton)
        if (colorizationButton != null) {
            colorizationButton.setBackgroundColor(viewModel.iconColorizationColor)
        }
        
        val borderColorButton = findViewById<MaterialButton>(R.id.borderColorButton)
        if (borderColorButton != null) {
            borderColorButton.setBackgroundColor(viewModel.borderColor)
        }
        */
        
        // ✅ Refrescar estados de botones
        refreshButtonStates()
        
        Toast.makeText(this, "Tematización aplicada exitosamente", Toast.LENGTH_SHORT).show()
    }
}