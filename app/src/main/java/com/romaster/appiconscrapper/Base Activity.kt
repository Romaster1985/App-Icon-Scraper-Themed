package com.romaster.appiconscrapper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

open class BaseActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar configuración ANTES de super.onCreate()
        super.onCreate(savedInstanceState)
    }
    
    override fun attachBaseContext(newBase: android.content.Context) {
        super.attachBaseContext(ConfigManager.applyLanguageToContext(newBase))
    }
}