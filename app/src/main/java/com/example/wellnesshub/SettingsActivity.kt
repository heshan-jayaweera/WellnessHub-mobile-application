package com.example.wellnesshub

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private var isNavigatingBack: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val aboutItem = findViewById<LinearLayout>(R.id.itemAbout)
        aboutItem?.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
        }

        findViewById<android.widget.ImageButton>(R.id.btnBack)?.setOnClickListener {
            if (isNavigatingBack) return@setOnClickListener
            isNavigatingBack = true
            val intent = Intent(this, ProfileActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            startActivity(intent)
            finish()
        }
    }
}


