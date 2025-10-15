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
import androidx.core.content.ContextCompat

class IconPreviewActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private var themedIcons: HashMap<String, android.graphics.Bitmap> = hashMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_icon_preview)

        // Obtener los iconos tematizados del intent
        themedIcons = intent.getSerializableExtra("themed_icons") as? HashMap<String, android.graphics.Bitmap>
            ?: hashMapOf()

        if (themedIcons.isEmpty()) {
            Toast.makeText(this, "No hay iconos para previsualizar", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        initViews()
        setupRecyclerView()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.iconGridRecyclerView)
    }

    private fun setupRecyclerView() {
        // Convertir el mapa a lista para el adapter
        val iconList = themedIcons.entries.map { it.toPair() }
        
        val adapter = IconPreviewAdapter(iconList)
        recyclerView.layoutManager = GridLayoutManager(this, 3) // 3 columnas
        recyclerView.adapter = adapter
    }
}

class IconPreviewAdapter(
    private val icons: List<Pair<String, android.graphics.Bitmap>>
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
        val (_, bitmap) = icons[position]
        holder.iconPreview.setImageBitmap(bitmap)
        
        // Añadir un pequeño margen y fondo para mejor visualización
        holder.itemView.setBackgroundResource(R.drawable.bg_card)
    }

    override fun getItemCount(): Int = icons.size
}
