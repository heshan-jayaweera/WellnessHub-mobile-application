package com.example.wellnesshub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import com.example.wellnesshub.utils.SystemUIHelper
import com.example.wellnesshub.utils.NotchFlowHelper

class TipsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure system UI for punch hole with bottom navigation awareness
        SystemUIHelper.configureForPunchHoleWithBottomNav(this)
        
        setContentView(R.layout.activity_tips)
        
        // Configure content to flow organically around notch like text around a hole
        NotchFlowHelper.configureContentFlowAroundNotchWithBottomNav(this, findViewById(android.R.id.content))

        setupBackButton()
        animateViews()
    }

    private fun setupBackButton() {
        findViewById<android.widget.ImageButton>(R.id.btnBack)?.setOnClickListener {
            // Add animation effect
            it.animate().scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(100)
            }
            // Navigate back to HomeActivity
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }
    }



    private fun animateViews() {
        findViewById<View>(android.R.id.content).alpha = 0f
        findViewById<View>(android.R.id.content).animate().alpha(1f).setDuration(500).start()
    }
}
