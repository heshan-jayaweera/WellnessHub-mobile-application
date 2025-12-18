package com.example.wellnesshub

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.wellnesshub.utils.SystemUIHelper
import com.example.wellnesshub.utils.NotchFlowHelper

class LoginActivity : AppCompatActivity() {
    
    private lateinit var logoImageView: ImageView
    private lateinit var welcomeText: TextView
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: Button
    private lateinit var forgotPasswordText: TextView
    private lateinit var signUpText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure system UI for punch hole with bottom navigation awareness
        SystemUIHelper.configureForPunchHoleWithBottomNav(this)
        
        setContentView(R.layout.activity_login)
        
        // Configure content to flow organically around notch like text around a hole
        NotchFlowHelper.configureContentFlowAroundNotchWithBottomNav(this, findViewById(android.R.id.content))
        
        // Initialize views
        logoImageView = findViewById(R.id.logo_image)
        welcomeText = findViewById(R.id.welcome_text)
        emailEditText = findViewById(R.id.email_edit_text)
        passwordEditText = findViewById(R.id.password_edit_text)
        loginButton = findViewById(R.id.login_button)
        forgotPasswordText = findViewById(R.id.forgot_password_text)
        signUpText = findViewById(R.id.sign_up_text)
        
        // Set up animations
        setupAnimations()
        
        // Set up click listeners
        setupClickListeners()
        
        // Set up input styling
        setupInputStyling()
    }
    
    private fun setupAnimations() {
        // Logo animation
        val logoAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in)
        logoAnimation.duration = 1500
        logoImageView.startAnimation(logoAnimation)
        
        // Welcome text animation
        val welcomeAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        welcomeAnimation.duration = 1000
        welcomeAnimation.startOffset = 500
        welcomeText.startAnimation(welcomeAnimation)
        
        // Input fields animation
        val inputAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        inputAnimation.duration = 800
        inputAnimation.startOffset = 800
        emailEditText.startAnimation(inputAnimation)
        passwordEditText.startAnimation(inputAnimation)
        
        // Button animation
        val buttonAnimation = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left)
        buttonAnimation.duration = 800
        buttonAnimation.startOffset = 1000
        loginButton.startAnimation(buttonAnimation)
    }
    
    private fun setupClickListeners() {
        val authPrefs = com.example.wellnesshub.data.AuthPreferences(this)
        loginButton.setOnClickListener {
            val username = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            if (authPrefs.login(username, password)) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Invalid credentials. Please register first.", Toast.LENGTH_SHORT).show()
            }
        }
        
        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Forgot password feature coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        signUpText.setOnClickListener {
            startActivity(Intent(this, RegistrationActivity::class.java))
        }
    }
    
    private fun setupInputStyling() {
        // Set up email field
        emailEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                emailEditText.setBackgroundResource(R.drawable.edit_text_focused_background)
            } else {
                emailEditText.setBackgroundResource(R.drawable.edit_text_background)
            }
        }
        
        // Set up password field
        passwordEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                passwordEditText.setBackgroundResource(R.drawable.edit_text_focused_background)
            } else {
                passwordEditText.setBackgroundResource(R.drawable.edit_text_background)
            }
        }
    }
    
    // Validation removed per requirement; login goes directly to main
}
