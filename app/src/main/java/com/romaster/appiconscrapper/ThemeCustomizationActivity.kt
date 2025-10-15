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
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStream

class ThemeCustomizationActivity : AppCompatActivity() {

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
    private var selectedColor: Int = Color.CYAN
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

    // Para la secuencia de preview
    private var currentPreviewIndex = 0
    private var previewIconsList = mutableListOf<Bitmap>()

    companion object {
        private const val PICK_MASK_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_customization)

        selectedApps = intent.getParcelableArrayListExtra<AppInfo>("selected_apps") ?: emptyList()

        initViews()
        setupListeners()
        loadSampleIcon()
        setupPreviewCycle()
        updateLayerInfo()
        updateButtonStyles() // NUEVO: Actualizar estilos de botones
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

        // Configurar rangos existentes
        seekBarX.max = 200
        seekBarX.progress = 100
        seekBarY.max = 200
        seekBarY.progress = 100
        seekBarScale.max = 200
        seekBarScale.progress = 100
        seekBarAlpha.max = 100
        seekBarAlpha.progress = 100
        seekBarColorIntensity.max = 100
        seekBarColorIntensity.progress = 100
        
        // Configurar rangos de nuevos controles
        seekBarHue.max = 360
        seekBarHue.progress = 180
        seekBarSaturation.max = 200
        seekBarSaturation.progress = 100
        seekBarBrightness.max = 200
        seekBarBrightness.progress = 100
        seekBarContrast.max = 200
        seekBarContrast.progress = 100
        
        // Configurar color inicial del botón
        colorPickerButton.setBackgroundColor(selectedColor)
        
        // NUEVO: Inicialmente deshabilitar botones
        previewAllButton.isEnabled = false
        exportButton.isEnabled = false
    }

    private fun setupListeners() {
        selectMaskButton.setOnClickListener {
            selectMaskImage()
        }

        colorPickerButton.setOnClickListener {
            showColorPickerDialog()
        }

        // Listeners para seekbars existentes
        seekBarX.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                offsetX = progress - 100
                xValueText.text = "X: $offsetX"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarY.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                offsetY = progress - 100
                yValueText.text = "Y: $offsetY"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarScale.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                scalePercentage = progress
                scaleValueText.text = "Escala: $scalePercentage%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarAlpha.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                alphaPercentage = progress
                alphaValueText.text = "Transparencia: $alphaPercentage%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarColorIntensity.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                colorIntensity = progress
                colorIntensityValueText.text = "Intensidad de Color: $colorIntensity%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Listeners para ajustes de imagen
        seekBarHue.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                hue = (progress - 180).toFloat()
                hueValueText.text = "Tinte: ${hue}°"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarSaturation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                saturation = progress / 100.0f
                saturationValueText.text = "Saturación: ${progress}%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarBrightness.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                brightness = (progress - 100).toFloat()
                brightnessValueText.text = "Brillo: ${brightness}%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        seekBarContrast.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                contrast = progress / 100.0f
                contrastValueText.text = "Contraste: ${progress}%"
                updatePreview()
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Listeners para configuración de capas
        useDefaultIconCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useDefaultIcon = isChecked
            if (isChecked) {
                useRoundIconCheckbox.isChecked = false
                useRoundIcon = false
            }
            updatePreview()
            updateLayerInfo()
        }

        useRoundIconCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useRoundIcon = isChecked
            if (isChecked) {
                useDefaultIconCheckbox.isChecked = false
                useDefaultIcon = false
            }
            updatePreview()
            updateLayerInfo()
        }

        foregroundLayerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useForegroundLayer = isChecked
            updatePreview()
            updateLayerInfo()
        }

        backgroundLayerCheckbox.setOnCheckedChangeListener { _, isChecked ->
            useBackgroundLayer = isChecked
            updatePreview()
            updateLayerInfo()
        }

        previewAllButton.setOnClickListener {
            previewAllIcons()
        }

        applyButton.setOnClickListener {
            processAllIcons()
        }

        exportButton.setOnClickListener {
            exportThemedIcons()
        }
    }

    // NUEVO: Método para actualizar estilos de botones
    private fun updateButtonStyles() {
        // Botón Previsualizar Todos
        if (previewAllButton.isEnabled) {
            previewAllButton.setBackgroundResource(R.drawable.button_primary)
            previewAllButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            previewAllButton.setBackgroundResource(R.drawable.bg_card)
            previewAllButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }

        // Botón Exportar
        if (exportButton.isEnabled) {
            exportButton.setBackgroundResource(R.drawable.button_primary)
            exportButton.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            exportButton.setBackgroundResource(R.drawable.bg_card)
            exportButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
        }
    }

    // Actualizar información de capas disponibles
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
                // Avanzar al siguiente índice
                currentPreviewIndex = (currentPreviewIndex + 1) % selectedApps.size
                val nextApp = selectedApps[currentPreviewIndex]
                
                // Procesar el icono de nextApp con la configuración actual
                Thread {
                    try {
                        val layers = IconScraper.getIconLayers(packageManager, nextApp.packageName)
                        val composedIcon = IconScraper.composeIconFromLayers(
                            layers = layers,
                            useDefault = useDefaultIcon,
                            useRound = useRoundIcon,
                            useForeground = useForegroundLayer,
                            useBackground = useBackgroundLayer
                        )
                        
                        val originalIcon = if (composedIcon != null) {
                            IconThemer.drawableToNormalizedBitmap(composedIcon)
                        } else {
                            val appInfo = packageManager.getApplicationInfo(nextApp.packageName, 0)
                            val defaultDrawable = appInfo.loadIcon(packageManager)
                            IconThemer.drawableToNormalizedBitmap(defaultDrawable)
                        }
                        
                        val config = IconThemer.ThemeConfig(
                            mask = selectedMask!!,
                            color = selectedColor,
                            offsetX = offsetX,
                            offsetY = offsetY,
                            scalePercentage = scalePercentage,
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
                        
                        val themedIcon = IconThemer.applyTheme(originalIcon, config)
                        
                        runOnUiThread {
                            iconPreview.setImageBitmap(themedIcon)
                            Toast.makeText(this, "Icono ${currentPreviewIndex + 1} de ${selectedApps.size}: ${nextApp.name}", 
                                Toast.LENGTH_SHORT).show()
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

    private fun previewAllIcons() {
        if (themedIcons.isEmpty()) {
            Toast.makeText(this, "Primero procesa los iconos", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, IconPreviewActivity::class.java).apply {
            putExtra("themed_icons", HashMap(themedIcons))
        }
        startActivity(intent)
    }

    private fun loadSampleIcon() {
        if (selectedApps.isNotEmpty()) {
            try {
                sampleAppPackage = selectedApps[0].packageName
                val layers = IconScraper.getIconLayers(packageManager, sampleAppPackage)
                
                // Obtener el icono compuesto según la configuración actual
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
                    // Fallback al icono por defecto
                    val defaultDrawable = IconScraper.getSimpleIcon(packageManager, sampleAppPackage)
                    defaultDrawable?.let { IconThemer.drawableToNormalizedBitmap(it) } 
                        ?: IconThemer.normalizeIconSize(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
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
            Color.RED, Color.GREEN, Color.BLUE, Color.CYAN, Color.MAGENTA, Color.YELLOW,
            Color.WHITE, Color.BLACK, Color.GRAY, Color.parseColor("#FF5722"),
            Color.parseColor("#9C27B0"), Color.parseColor("#2196F3"), Color.parseColor("#4CAF50")
        )

        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("Seleccionar Color")
        
        val colorNames = arrayOf("Rojo", "Verde", "Azul", "Cian", "Magenta", "Amarillo", 
                               "Blanco", "Negro", "Gris", "Naranja", "Púrpura", "Azul Claro", "Verde Claro")
        
        builder.setItems(colorNames) { _, which ->
            selectedColor = colors[which]
            colorPickerButton.setBackgroundColor(selectedColor)
            updatePreview()
        }
        
        builder.show()
    }

    private fun updatePreview() {
        if (selectedMask != null && sampleIcon != null) {
            val config = IconThemer.ThemeConfig(
                mask = selectedMask!!,
                color = selectedColor,
                offsetX = offsetX,
                offsetY = offsetY,
                scalePercentage = scalePercentage,
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
            
            val processedIcon = IconThemer.applyTheme(sampleIcon!!, config)
            iconPreview.setImageBitmap(processedIcon)
        }
    }

    private fun processAllIcons() {
        if (selectedMask == null) {
            Toast.makeText(this, "Primero selecciona una máscara", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            val totalIcons = selectedApps.size
            var processedCount = 0

            runOnUiThread {
                progressText.text = "Procesando: 0/$totalIcons"
                applyButton.isEnabled = false
            }

            themedIcons.clear()
            previewIconsList.clear()

            val config = IconThemer.ThemeConfig(
                mask = selectedMask!!,
                color = selectedColor,
                offsetX = offsetX,
                offsetY = offsetY,
                scalePercentage = scalePercentage,
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

            selectedApps.forEach { app ->
                try {
                    // Obtener las capas del icono
                    val layers = IconScraper.getIconLayers(packageManager, app.packageName)
                    
                    // Componer el icono según la configuración
                    val composedIcon = IconScraper.composeIconFromLayers(
                        layers = layers,
                        useDefault = useDefaultIcon,
                        useRound = useRoundIcon,
                        useForeground = useForegroundLayer,
                        useBackground = useBackgroundLayer
                    )
                    
                    val originalIcon = if (composedIcon != null) {
                        IconThemer.drawableToNormalizedBitmap(composedIcon)
                    } else {
                        // Fallback: obtener el icono por defecto directamente
                        val appInfo = packageManager.getApplicationInfo(app.packageName, 0)
                        val defaultDrawable = appInfo.loadIcon(packageManager)
                        IconThemer.drawableToNormalizedBitmap(defaultDrawable)
                    }
                    
                    val themedIcon = IconThemer.applyTheme(originalIcon, config)
                    
                    themedIcons[app.packageName] = themedIcon
                    previewIconsList.add(themedIcon)
                    processedCount++
                    
                    runOnUiThread {
                        progressText.text = "Procesando: $processedCount/$totalIcons"
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
                updateButtonStyles() // NUEVO: Actualizar estilos después de procesar
                Toast.makeText(this, "$processedCount de ${selectedApps.size} iconos procesados exitosamente", Toast.LENGTH_SHORT).show()
                
                currentPreviewIndex = 0
                if (previewIconsList.isNotEmpty()) {
                    iconPreview.setImageBitmap(previewIconsList[0])
                }
            }
        }.start()
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
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
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

    private fun loadMaskFromUri(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            selectedMask = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            maskPreview.setImageBitmap(selectedMask)
            updatePreview()
        } catch (e: IOException) {
            Toast.makeText(this, "Error cargando máscara: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
