package com.romaster.appiconscrapper

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import java.io.ByteArrayOutputStream
import java.io.Serializable

class IconPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_preview)

        initViews()
        
        // Obtener los datos de forma segura
        val iconData = intent.getSerializableExtra("icon_data") as? IconData
        if (iconData == null || iconData.icons.isEmpty()) {
            Toast.makeText(this, "No hay iconos para previsualizar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView(iconData.icons)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.iconGridRecyclerView)
    }

    private fun setupRecyclerView(icons: List<IconItem>) {
        val adapter = IconPreviewAdapter(icons)
        recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.adapter = adapter
    }
}

// Clase para pasar datos de forma segura entre actividades
data class IconData(val icons: List<IconItem>) : Serializable

data class IconItem(val packageName: String, val iconBytes: ByteArray) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as IconItem

        if (packageName != other.packageName) return false
        if (!iconBytes.contentEquals(other.iconBytes)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = packageName.hashCode()
        result = 31 * result + iconBytes.contentHashCode()
        return result
    }
}

class IconPreviewAdapter(
    private val icons: List<IconItem>
) : RecyclerView.Adapter<IconPreviewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val iconPreview: ImageView = view.findViewById(R.id.iconPreviewImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon_preview, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val iconItem = icons[position]
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(iconItem.iconBytes, 0, iconItem.iconBytes.size)
        holder.iconPreview.setImageBitmap(bitmap)
        
        holder.itemView.setBackgroundResource(R.drawable.bg_card)
    }

    override fun getItemCount(): Int = icons.size
}