package com.example.wellnesshub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.graphics.Color
import com.example.wellnesshub.data.AuthPreferences
import com.example.wellnesshub.utils.SystemUIHelper
import com.example.wellnesshub.utils.NotchFlowHelper

class ProfileActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure system UI for punch hole with bottom navigation awareness
        SystemUIHelper.configureForPunchHoleWithBottomNav(this)
        
        setContentView(R.layout.activity_profile)
        
        // Configure content to flow organically around notch like text around a hole
        NotchFlowHelper.configureContentFlowAroundNotchWithBottomNav(this, findViewById(android.R.id.content))

        setupBottomNavigation()
        setupProfilePhoto()
        setupUserDetails()
        setupStatistics()
        setupLogoutButton()
        
        animateViews()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
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
                R.id.nav_profile -> true
                else -> false
            }
        }
        bottomNav?.selectedItemId = R.id.nav_profile
    }

    private fun setupProfilePhoto() {
        val profilePhoto = findViewById<ImageView>(R.id.profilePhoto)
        val changePhotoBtn = findViewById<Button>(R.id.btnChangePhoto)

        profilePhoto?.apply {
            setImageResource(R.drawable.ic_profile)
            setColorFilter(ContextCompat.getColor(this@ProfileActivity, R.color.hydration_blue_primary))
            background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.profile_photo_bg)
            elevation = 8f
        }

        changePhotoBtn?.apply {
            background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.button_secondary_gradient)
            setTextColor(Color.WHITE)
            elevation = 4f
            setOnClickListener {
                animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
            }
        }
    }

    private fun setupUserDetails() {
        val nameEditText = findViewById<EditText>(R.id.editTextName)
        val emailEditText = findViewById<EditText>(R.id.editTextEmail)
        val weightEditText = findViewById<EditText>(R.id.editTextWeight)
        val profileNameText = findViewById<TextView>(R.id.profileNameText)

        val authPrefs = AuthPreferences(this)
        val username = authPrefs.getUsername() ?: getString(R.string.dummy_name)
        nameEditText?.setText(username)
        profileNameText?.setText(username)
        emailEditText?.setText(getString(R.string.dummy_email))
        weightEditText?.setText(getString(R.string.dummy_weight))

        emailEditText?.inputType = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        weightEditText?.inputType = android.text.InputType.TYPE_CLASS_NUMBER

        listOf(nameEditText, emailEditText, weightEditText).forEach { editText ->
            editText?.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    editText.background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.edit_text_focused_background)
                } else {
                    editText.background = ContextCompat.getDrawable(this@ProfileActivity, R.drawable.edit_text_background)
                }
            }
        }
    }

    private fun setupStatistics() {
        val avgIntakeText = findViewById<TextView>(R.id.avgIntakeValue)
        val streakText = findViewById<TextView>(R.id.streakValue)
        val totalWaterText = findViewById<TextView>(R.id.totalWaterValue)

        avgIntakeText?.text = getString(R.string.dummy_avg)
        streakText?.text = getString(R.string.dummy_streak)
        totalWaterText?.text = "14.7L"

        listOf(avgIntakeText, streakText, totalWaterText).forEach { textView ->
            textView?.setOnClickListener {
                it.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).withEndAction {
                    it.animate().scaleX(1f).scaleY(1f).setDuration(200)
                }
            }
        }
    }





    private fun setupLogoutButton() {
        val logoutButton = findViewById<Button>(R.id.btnLogout)
        logoutButton?.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_confirmation_title))
            .setMessage(getString(R.string.logout_confirmation_message))
            .setPositiveButton(getString(R.string.logout_confirm)) { _, _ ->
                performLogout()
            }
            .setNegativeButton(getString(R.string.logout_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .setCancelable(true)
            .show()
    }

    private fun performLogout() {
        val authPrefs = AuthPreferences(this)
        
        // Clear only authentication data (preserve user's wellness data)
        authPrefs.logout()
        
        // Show logout message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()
        
        // Navigate to login activity and clear the activity stack
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun animateViews() {
        findViewById<View>(android.R.id.content).alpha = 0f
        findViewById<View>(android.R.id.content).animate().alpha(1f).setDuration(500).start()
    }
}
