package com.romaster.appiconscrapper

import android.app.Activity
import android.content.Intent
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import java.io.ByteArrayOutputStream
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
    private lateinit var applyButton: Button
    private lateinit var exportButton: Button
    private lateinit var xValueText: TextView
    private lateinit var yValueText: TextView
    private lateinit var scaleValueText: TextView
    private lateinit var progressText: TextView

    private var selectedMask: Bitmap? = null
    private var selectedColor: Int = Color.CYAN
    private var offsetX: Int = 0
    private var offsetY: Int = 0
    private var scalePercentage: Int = 100
    private lateinit var selectedApps: ArrayList<AppInfo>
    private var sampleIcon: Bitmap? = null

    companion object {
        private const val PICK_MASK_REQUEST = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme_customization)

        selectedApps = intent.getSerializableExtra("selected_apps") as ArrayList<AppInfo>

        initViews()
        setupListeners()
        loadSampleIcon()
    }

    private fun initViews() {
        maskPreview = findViewById(R.id.maskPreview)
        iconPreview = findViewById(R.id.iconPreview)
        selectMaskButton = findViewById(R.id.selectMaskButton)
        colorPickerButton = findViewById(R.id.colorPickerButton)
        seekBarX = findViewById(R.id.seekBarX)
        seekBarY = findViewById(R.id.seekBarY)
        seekBarScale = findViewById(R.id.seekBarScale)
        applyButton = findViewById(R.id.applyButton)
        exportButton = findViewById(R.id.exportButton)
        xValueText = findViewById(R.id.xValueText)
        yValueText = findViewById(R.id.yValueText)
        scaleValueText = findViewById(R.id.scaleValueText)
        progressText = findViewById(R.id.progressText)

        seekBarX.max = 200
        seekBarX.progress = 100
        seekBarY.max = 200
        seekBarY.progress = 100
        seekBarScale.max = 200
        seekBarScale.progress = 100
    }

    private fun setupListeners() {
        selectMaskButton.setOnClickListener {
            selectMaskImage()
        }

        colorPickerButton.setOnClickListener {
            showColorPickerDialog()
        }

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

        applyButton.setOnClickListener {
            processAllIcons()
        }

        exportButton.setOnClickListener {
            exportThemedIcons()
        }
    }

    private fun loadSampleIcon() {
        if (selectedApps.isNotEmpty()) {
            val drawable = selectedApps[0].icon
            sampleIcon = drawable.toBitmap(drawable.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
        } else {
            sampleIcon = BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher)
        }
        updatePreview()
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
        
        val colorAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1)
        colorAdapter.addAll("Rojo", "Verde", "Azul", "Cian", "Magenta", "Amarillo", 
                           "Blanco", "Negro", "Gris", "Naranja", "Púrpura", "Azul Claro", "Verde Claro")
        
        builder.setAdapter(colorAdapter) { dialog, which ->
            selectedColor = colors[which]
            colorPickerButton.setBackgroundColor(selectedColor)
            updatePreview()
        }
        
        builder.show()
    }

    private fun updatePreview() {
        if (selectedMask != null && sampleIcon != null) {
            val processedIcon = IconThemer.applyTheme(
                sampleIcon!!, 
                selectedMask!!, 
                selectedColor, 
                offsetX, 
                offsetY, 
                scalePercentage
            )
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

            selectedApps.forEach { app ->
                try {
                    val originalIcon = app.icon.toBitmap(
                        app.icon.intrinsicWidth, 
                        app.icon.intrinsicHeight, 
                        Bitmap.Config.ARGB_8888
                    )
                    
                    val themedIcon = IconThemer.applyTheme(
                        originalIcon, 
                        selectedMask!!, 
                        selectedColor, 
                        offsetX, 
                        offsetY, 
                        scalePercentage
                    )
                    
                    app.themedIcon = themedIcon
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
                Toast.makeText(this, "$processedCount iconos procesados", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    private fun exportThemedIcons() {
        val appsWithThemedIcons = selectedApps.filter { it.themedIcon != null }
        if (appsWithThemedIcons.isEmpty()) {
            Toast.makeText(this, "Primero procesa los iconos", Toast.LENGTH_SHORT).show()
            return
        }

        Thread {
            try {
                val success = IconScraper.createThemedZipFile(appsWithThemedIcons, filesDir, "themed_icons")
                
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "Iconos tematizados exportados exitosamente", Toast.LENGTH_LONG).show()
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