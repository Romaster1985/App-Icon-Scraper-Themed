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

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.InputStream
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var titleText: TextView
    private lateinit var countText: TextView
    
    private val iconList = mutableListOf<IconItem>()
    private var iconMappings = mutableListOf<IconMapping>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        loadIconsWithMappings()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.iconsRecyclerView)
        titleText = findViewById(R.id.titleText)
        countText = findViewById(R.id.countText)
        
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.setHasFixedSize(true)
        
        titleText.text = getString(R.string.app_name)
    }
    
    private fun loadIconsWithMappings() {
        Thread {
            try {
                val friendlyNames = parseDrawableXmlFromAssets()
                
                // ✅ CARGAR SOLO appfilter.xml (ya no existe appnofilter.xml)
                val mappings = parseAppFilterXmlFromAssets(friendlyNames)
                
                iconMappings = mappings
                loadIconsDirectly(friendlyNames)
                
                runOnUiThread {
                    updateUI()
                }
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Error cargando iconos", e)
                runOnUiThread {
                    countText.text = "Error: ${e.message}"
                }
            }
        }.start()
    }
    
    private fun parseAppFilterXmlFromAssets(friendlyNames: Map<String, String>): MutableList<IconMapping> {
        val mappings = mutableListOf<IconMapping>()
        
        try {
            val inputStream: InputStream = assets.open("appfilter.xml")
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            
            var eventType = parser.eventType
            var currentPackage: String? = null
            var currentActivity: String? = null
            var currentDrawable: String? = null
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "item") {
                            val component = parser.getAttributeValue(null, "component")
                            val drawable = parser.getAttributeValue(null, "drawable")
                            
                            if (component != null && drawable != null) {
                                val match = """ComponentInfo\{([^}/]+)/([^}"]+)\}""".toRegex().find(component)
                                if (match != null) {
                                    val (pkg, activity) = match.destructured
                                    currentPackage = pkg
                                    currentActivity = activity
                                    currentDrawable = drawable
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && currentPackage != null && currentActivity != null && currentDrawable != null) {
                            val friendlyName = friendlyNames[currentDrawable] ?: currentDrawable
                            
                            mappings.add(IconMapping(
                                packageName = currentPackage,
                                friendlyName = friendlyName,
                                currentActivity = currentActivity,
                                allActivities = listOf(currentActivity),
                                drawableName = currentDrawable
                            ))
                            
                            currentPackage = null
                            currentActivity = null
                            currentDrawable = null
                        }
                    }
                }
                eventType = parser.next()
            }
            
            inputStream.close()
            Log.d("MainActivity", "✅ appfilter.xml: ${mappings.size} mapeos")
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error parseando appfilter.xml", e)
        }
        
        return mappings
    }
    
    private fun parseDrawableXmlFromAssets(): Map<String, String> {
        val friendlyNames = mutableMapOf<String, String>()
        
        try {
            val inputStream: InputStream = assets.open("drawable.xml")
            val factory = XmlPullParserFactory.newInstance()
            val parser = factory.newPullParser()
            parser.setInput(inputStream, "UTF-8")
            
            var eventType = parser.eventType
            var currentName: String? = null
            var currentValue: String? = null
            
            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        if (parser.name == "item") {
                            currentName = null
                            currentValue = null
                            
                            for (i in 0 until parser.attributeCount) {
                                when (parser.getAttributeName(i)) {
                                    "name" -> currentName = parser.getAttributeValue(i)
                                    "type" -> {
                                        if (parser.getAttributeValue(i) != "string") {
                                            currentName = null
                                        }
                                    }
                                }
                            }
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (currentName != null) {
                            currentValue = parser.text
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && currentName != null && currentValue != null) {
                            friendlyNames[currentName] = currentValue
                            currentName = null
                            currentValue = null
                        }
                    }
                }
                eventType = parser.next()
            }
            
            inputStream.close()
            Log.d("MainActivity", "✅ drawable.xml: ${friendlyNames.size} nombres")
            
        } catch (e: Exception) {
            Log.e("MainActivity", "Error parseando drawable.xml", e)
        }
        
        return friendlyNames
    }
    
    private fun loadIconsDirectly(friendlyNames: Map<String, String>) {
        iconList.clear()
        
        friendlyNames.forEach { (iconName, friendlyName) ->
            try {
                val resourceId = resources.getIdentifier(iconName, "drawable", packageName)
                if (resourceId != 0) {
                    val drawable: Drawable? = getDrawable(resourceId)
                    if (drawable != null) {
                        iconList.add(IconItem(iconName, friendlyName, drawable))
                    }
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error cargando icono: $iconName", e)
            }
        }
        
        iconList.sortBy { it.resourceName }
        Log.d("MainActivity", "✅ Iconos cargados: ${iconList.size}")
    }
    
    private fun updateUI() {
        countText.text = "${iconList.size} iconos - ${iconMappings.size} apps"
        
        val adapter = IconAdapter(iconList, iconMappings) { iconItem ->
            showIconEditor(iconItem)
        }
        recyclerView.adapter = adapter
        
        Log.d("MainActivity", "🎉 UI actualizada: ${iconList.size} íconos")
    }
    
    private fun showIconEditor(iconItem: IconItem) {
        val mapping = iconMappings.find { it.drawableName == iconItem.resourceName }
        if (mapping != null) {
            Log.d("MainActivity", "📱 Mostrando información para: ${mapping.packageName}")
            val dialog = IconEditorDialog(this, mapping)
            dialog.show()
        } else {
            Toast.makeText(this, "No se encontró información para este ícono", Toast.LENGTH_SHORT).show()
        }
    }
    
    data class IconItem(
        val resourceName: String,
        val displayName: String,
        val icon: Drawable
    )
    
    data class IconMapping(
        val packageName: String,
        val friendlyName: String,
        val currentActivity: String,
        val allActivities: List<String>,
        val drawableName: String
    )
}