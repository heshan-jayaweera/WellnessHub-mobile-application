package com.example.wellnesshub.utils

import android.app.Activity
import android.os.Build
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object SystemUIHelper {
    
    /**
     * Configure the activity to handle system UI and punch hole properly
     * Content will flow around the notch area like text around a hole
     */
    fun configureForPunchHole(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+)
            activity.window.setDecorFitsSystemWindows(false)
            
            // Handle system bars with content flowing around notch
            ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(android.R.id.content)) { view, windowInsets ->
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
                
                // Only add bottom padding for navigation bar
                // Let content flow organically around notch
                view.setPadding(0, 0, 0, systemBars.bottom)
                windowInsets
            }
        } else {
            // For older versions, use traditional approach
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // Enable edge-to-edge display with content flowing around cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
    }
    
    /**
     * Configure the activity to handle system UI with bottom navigation awareness
     * This prevents double padding when there's a bottom navigation view
     */
    fun configureForPunchHoleWithBottomNav(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // For Android 11+ (API 30+)
            activity.window.setDecorFitsSystemWindows(false)
            
            // Handle system bars with content flowing around notch
            ViewCompat.setOnApplyWindowInsetsListener(activity.findViewById(android.R.id.content)) { view, windowInsets ->
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
                
                // Don't add bottom padding when there's a bottom navigation
                // The bottom nav will handle its own positioning
                view.setPadding(0, 0, 0, 0)
                windowInsets
            }
        } else {
            // For older versions, use traditional approach
            @Suppress("DEPRECATION")
            activity.window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            )
        }
        
        // Enable edge-to-edge display with content flowing around cutout
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            activity.window.attributes.layoutInDisplayCutoutMode = 
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
        }
    }
    
    /**
     * Get safe area insets for punch hole and status bar
     */
    fun getSafeAreaInsets(activity: Activity): android.graphics.Rect {
        val insets = android.graphics.Rect()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowInsets = activity.window.decorView.rootWindowInsets
            val systemBars = windowInsets?.getInsets(WindowInsets.Type.systemBars())
            if (systemBars != null) {
                insets.set(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            }
        } else {
            @Suppress("DEPRECATION")
            val decorView = activity.window.decorView
            val resources = activity.resources
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            val statusBarHeight = if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else {
                0
            }
            insets.set(0, statusBarHeight, 0, 0)
        }
        return insets
    }
    
    /**
     * Check if device has punch hole or notch
     */
    fun hasPunchHole(activity: Activity): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowInsets = activity.window.decorView.rootWindowInsets
            windowInsets?.displayCutout != null
        } else {
            false
        }
    }
    
    /**
     * Get additional padding needed for punch hole
     */
    fun getPunchHolePadding(activity: Activity): Int {
        return if (hasPunchHole(activity)) {
            // Add extra padding for punch hole (typically 24-32dp)
            (activity.resources.displayMetrics.density * 28).toInt()
        } else {
            0
        }
    }
    
    /**
     * Configure content to flow around the notch area
     * This makes content wrap around the notch like text around a hole
     */
    fun configureContentFlowAroundNotch(activity: Activity, contentView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            ViewCompat.setOnApplyWindowInsetsListener(contentView) { view, windowInsets ->
                val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                
                // Create padding that flows around the notch
                val leftPadding = maxOf(systemBars.left, cutoutInsets.left)
                val topPadding = maxOf(systemBars.top, cutoutInsets.top)
                val rightPadding = maxOf(systemBars.right, cutoutInsets.right)
                val bottomPadding = systemBars.bottom
                
                // Apply padding to make content flow around notch
                view.setPadding(leftPadding, topPadding, rightPadding, bottomPadding)
                
                windowInsets
            }
        }
    }
    
    /**
     * Get notch dimensions for content flow calculations
     */
    fun getNotchDimensions(activity: Activity): android.graphics.Rect {
        val notchRect = android.graphics.Rect()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowInsets = activity.window.decorView.rootWindowInsets
            val cutout = windowInsets?.displayCutout
            if (cutout != null) {
                // Get the bounding rect of the cutout
                val boundingRect = cutout.boundingRects
                if (boundingRect.isNotEmpty()) {
                    // Use the first (usually top) cutout
                    notchRect.set(boundingRect[0])
                }
            }
        }
        return notchRect
    }
}
