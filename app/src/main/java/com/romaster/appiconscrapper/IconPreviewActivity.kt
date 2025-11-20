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

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class IconPreviewActivity : BaseActivity() {

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
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = IconAdapter(icons)
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
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
            val bitmap = icons[position]
            if (!bitmap.isRecycled) {
                holder.imageView.setImageBitmap(bitmap)
            } else {
                holder.imageView.setImageResource(R.mipmap.ic_launcher)
            }
        }

        override fun getItemCount() = icons.size

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val imageView: ImageView = view.findViewById(R.id.iconPreviewImage)
        }
    }
}
