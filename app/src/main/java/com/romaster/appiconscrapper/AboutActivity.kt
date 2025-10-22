package com.romaster.appiconscrapper

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class AboutActivity : AppCompatActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)
        
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