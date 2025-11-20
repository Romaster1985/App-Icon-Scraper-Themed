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
package com.romaster.iconpacktemplate

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import android.util.Log

class IconEditorDialog(
    context: Context,
    private val iconMapping: MainActivity.IconMapping
) : Dialog(context) {
    
    private lateinit var iconPreview: ImageView
    private lateinit var friendlyName: TextView
    private lateinit var packageName: TextView
    private lateinit var activityName: TextView
    private lateinit var additionalInfo: TextView
    private lateinit var btnClose: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_icon_editor)
        
        initViews()
        setupDialogContent()
    }
    
    private fun initViews() {
        iconPreview = findViewById(R.id.iconPreview)
        friendlyName = findViewById(R.id.friendlyName)
        packageName = findViewById(R.id.packageName)
        activityName = findViewById(R.id.activityName)
        additionalInfo = findViewById(R.id.additionalInfo)
        btnClose = findViewById(R.id.btnClose)
        
        Log.d("IconEditorDialog", "Mostrando información para: ${iconMapping.packageName}")
    }
    
    private fun setupDialogContent() {
        // Cargar el ícono visual
        val resourceId = context.resources.getIdentifier(
            iconMapping.drawableName, 
            "drawable", 
            context.packageName
        )
        if (resourceId != 0) {
            iconPreview.setImageResource(resourceId)
        }
        
        // Mostrar información (SIN EMOJIS)
        friendlyName.text = iconMapping.friendlyName
        packageName.text = "Package: ${iconMapping.packageName}"
        activityName.text = "Actividad: ${iconMapping.currentActivity}"
        
        // Información adicional (SIN EMOJIS)
        additionalInfo.text = "Configuración desde appfilter.xml"
        
        // Configurar botón de cerrar
        btnClose.setOnClickListener {
            dismiss()
        }
        
        Log.d("IconEditorDialog", "Información cargada:")
        Log.d("IconEditorDialog", "   App: ${iconMapping.friendlyName}")
        Log.d("IconEditorDialog", "   Package: ${iconMapping.packageName}")
        Log.d("IconEditorDialog", "   Actividad: ${iconMapping.currentActivity}")
    }
}