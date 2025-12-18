package com.example.wellnesshub

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupBottomNavigation()

        // Start with HomeActivity by default with smooth transition
        if (savedInstanceState == null) {
            // Add a small delay for smooth transition
            findViewById<View>(android.R.id.content).postDelayed({
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            }, 500)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        bottomNav.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_mood -> {
                    startActivity(Intent(this, MoodJournalActivity::class.java))
                    true
                }
                R.id.nav_habits -> {
                    startActivity(Intent(this, HabitTrackerActivity::class.java))
                    true
                }
                R.id.nav_hydration -> {
                    startActivity(Intent(this, HydrationReminderActivity::class.java))
                    true
                }
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }

        // Set mood as selected by default
        bottomNav.selectedItemId = R.id.nav_mood
    }
}
