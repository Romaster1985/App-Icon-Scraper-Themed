package com.romaster.appiconscrapper

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IconPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var icons: List<Bitmap> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_preview)

        recyclerView = findViewById(R.id.iconGridRecyclerView)

        // Recuperar íconos desde el caché global
        icons = IconCache.iconsProcessed ?: emptyList()

        if (icons.isEmpty()) {
            finish()
            return
        }

        // Configurar GridLayoutManager para el RecyclerView
        recyclerView.layoutManager = GridLayoutManager(this, 4) // 4 columnas
        recyclerView.adapter = IconAdapter(icons)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        // No limpiar aquí para permitir volver a ThemeCustomizationActivity
    }

    override fun onDestroy() {
        super.onDestroy()
        // NO reciclamos los bitmaps aquí - el ViewModel de ThemeCustomizationActivity es el dueño
        // Solo limpiamos el cache si la actividad se está finalizando completamente
        if (isFinishing) {
            IconCache.clear()
        }
    }

    private class IconAdapter(private val icons: List<Bitmap>) : RecyclerView.Adapter<IconAdapter.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_icon_preview, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            // CORREGIDO: Verificar que el bitmap no esté reciclado antes de mostrarlo
            val bitmap = icons[position]
            if (!bitmap.isRecycled) {
                holder.imageView.setImageBitmap(bitmap)
            } else {
                // Si está reciclado, mostrar un placeholder o manejar el error
                holder.imageView.setImageResource(R.mipmap.ic_launcher)
            }
        }

        override fun getItemCount() = icons.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.iconPreviewImage)
        }
    }
}