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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : BaseActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        
        supportActionBar?.title = getString(R.string.about_title)
        
        setupViews()
    }
    
    private fun setupViews() {
        val aboutText = findViewById<TextView>(R.id.aboutText)
        val githubButton = findViewById<Button>(R.id.githubButton)
        val emailButton = findViewById<Button>(R.id.emailButton)
        val logoImage = findViewById<ImageView>(R.id.logoImage)
        val logoDHImage = findViewById<ImageView>(R.id.logoDHImage)
        
        aboutText.text = getString(R.string.about_content)
        logoImage.setImageResource(R.drawable.logo_romaster)
        logoDHImage.setImageResource(R.drawable.logo_deephub)

        githubButton.setOnClickListener {
            openUrl("https://github.com/Romaster1985/App-Icon-Scraper-Themed")
        }
        
        emailButton.setOnClickListener {
            sendEmail()
        }
    }
    
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        startActivity(intent)
    }
    
    private fun sendEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:roman.ignacio.romero@gmail.com")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        }
        startActivity(Intent.createChooser(intent, getString(R.string.menu_about)))
    }
}
