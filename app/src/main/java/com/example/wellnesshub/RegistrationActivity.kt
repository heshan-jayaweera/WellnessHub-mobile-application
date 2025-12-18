package com.example.wellnesshub

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wellnesshub.data.AuthPreferences
import com.example.wellnesshub.utils.SystemUIHelper
import com.example.wellnesshub.utils.NotchFlowHelper

class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure system UI for punch hole with bottom navigation awareness
        SystemUIHelper.configureForPunchHoleWithBottomNav(this)
        
        setContentView(R.layout.activity_registration)
        
        // Configure content to flow organically around notch like text around a hole
        NotchFlowHelper.configureContentFlowAroundNotchWithBottomNav(this, findViewById(android.R.id.content))

        val usernameEditText = findViewById<EditText>(R.id.editUsername)
        val emailEditText = findViewById<EditText>(R.id.editEmail)
        val passwordEditText = findViewById<EditText>(R.id.editPassword)
        val confirmEditText = findViewById<EditText>(R.id.editConfirmPassword)
        val registerButton = findViewById<Button>(R.id.btnRegister)
        val signInText = findViewById<TextView>(R.id.sign_in_text)

        val authPrefs = com.example.wellnesshub.data.AuthPreferences(this)

        registerButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString()
            val confirm = confirmEditText.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            if (password != confirm) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (authPrefs.register(username, password)) {
                Toast.makeText(this, "Registered successfully. Please log in.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                Toast.makeText(this, "Username already exists. Please choose a different username.", Toast.LENGTH_SHORT).show()
            }
        }

        signInText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}


