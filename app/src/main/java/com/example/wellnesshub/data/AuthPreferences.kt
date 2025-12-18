package com.example.wellnesshub.data

import android.content.Context
import android.content.SharedPreferences

class AuthPreferences(context: Context) {
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveUser(username: String, email: String, password: String) {
        sharedPreferences.edit().apply {
            putString("username", username)
            putString("email", email)
            putString("password", password)
            putBoolean("isLoggedIn", true)
            apply()
        }
    }

    fun login(username: String, password: String): Boolean {
        val savedUsername = sharedPreferences.getString("username", "")
        val savedPassword = sharedPreferences.getString("password", "")
        return username == savedUsername && password == savedPassword
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean("isLoggedIn", false)
    }

    fun logout() {
        sharedPreferences.edit().apply {
            putBoolean("isLoggedIn", false)
            apply()
        }
    }

    fun getUsername(): String {
        return sharedPreferences.getString("username", "") ?: ""
    }

    fun getEmail(): String {
        return sharedPreferences.getString("email", "") ?: ""
    }

    fun register(username: String, password: String): Boolean {
        // Check if user already exists
        val existingUsername = sharedPreferences.getString("username", "")
        if (existingUsername == username) {
            return false // User already exists
        }
        
        // Save new user
        saveUser(username, username, password) // Using username as email for simplicity
        return true
    }
}
