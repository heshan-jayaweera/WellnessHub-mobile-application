package com.example.wellnesshub.utils

import android.app.Activity
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

object NotchFlowHelper {
    
    /**
     * Configure content to actually flow around the notch like text around a hole
     */
    fun configureContentFlowAroundNotch(activity: Activity, rootView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Enable edge-to-edge with content flowing into cutout area
            activity.window.attributes.layoutInDisplayCutoutMode = 
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            
            // Make content flow around the notch organically like text around a hole
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
                val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                
                // Only add bottom padding for navigation bar
                // Let content flow organically around notch
                view.setPadding(0, 0, 0, systemBars.bottom)
                
                // Apply notch-aware layout to child views for organic flow
                applyNotchFlowToChildren(view, cutoutInsets)
                
                windowInsets
            }
        }
    }
    
    /**
     * Configure content to flow around notch with bottom navigation awareness
     * This prevents double padding when there's a bottom navigation view
     */
    fun configureContentFlowAroundNotchWithBottomNav(activity: Activity, rootView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Enable edge-to-edge with content flowing into cutout area
            activity.window.attributes.layoutInDisplayCutoutMode = 
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            
            // Make content flow around the notch organically like text around a hole
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
                val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                
                // Don't add bottom padding when there's a bottom navigation
                // The bottom nav will handle its own positioning
                view.setPadding(0, 0, 0, 0)
                
                // Apply notch-aware layout to child views for organic flow
                applyNotchFlowToChildren(view, cutoutInsets)
                
                windowInsets
            }
        }
    }
    
    /**
     * Apply notch flow to child views
     */
    private fun applyNotchFlowToChildren(parent: View, cutoutInsets: Insets) {
        if (parent is ViewGroup) {
            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)
                applyNotchFlowToView(child, cutoutInsets)
            }
        }
    }
    
    /**
     * Apply notch flow to individual view
     */
    private fun applyNotchFlowToView(view: View, cutoutInsets: Insets) {
        when (view) {
            is LinearLayout -> {
                // For LinearLayouts, adjust margins to flow around notch
                val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                if (layoutParams != null) {
                    // Only add left/right margins if cutout affects those areas
                    if (cutoutInsets.left > 0) {
                        layoutParams.leftMargin = maxOf(layoutParams.leftMargin, cutoutInsets.left)
                    }
                    if (cutoutInsets.right > 0) {
                        layoutParams.rightMargin = maxOf(layoutParams.rightMargin, cutoutInsets.right)
                    }
                    view.layoutParams = layoutParams
                }
            }
            is RelativeLayout -> {
                // For RelativeLayouts, adjust positioning
                val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
                if (layoutParams != null) {
                    // Adjust margins to flow around notch
                    layoutParams.leftMargin = maxOf(layoutParams.leftMargin, cutoutInsets.left)
                    layoutParams.rightMargin = maxOf(layoutParams.rightMargin, cutoutInsets.right)
                    view.layoutParams = layoutParams
                }
            }
        }
        
        // Recursively apply to children
        if (view is ViewGroup) {
            applyNotchFlowToChildren(view, cutoutInsets)
        }
    }
    
    /**
     * Get notch dimensions for precise content flow
     */
    fun getNotchBounds(activity: Activity): Rect {
        val notchRect = Rect()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val windowInsets = activity.window.decorView.rootWindowInsets
            val cutout = windowInsets?.displayCutout
            if (cutout != null && cutout.boundingRects.isNotEmpty()) {
                // Get the main cutout (usually at the top)
                notchRect.set(cutout.boundingRects[0])
            }
        }
        return notchRect
    }
    
    /**
     * Check if a view intersects with the notch area
     */
    fun viewIntersectsWithNotch(view: View, activity: Activity): Boolean {
        val notchBounds = getNotchBounds(activity)
        if (notchBounds.isEmpty) return false
        
        val viewLocation = IntArray(2)
        view.getLocationOnScreen(viewLocation)
        val viewRect = Rect(
            viewLocation[0],
            viewLocation[1],
            viewLocation[0] + view.width,
            viewLocation[1] + view.height
        )
        
        return Rect.intersects(notchBounds, viewRect)
    }
    
    /**
     * Adjust view position to flow around notch
     */
    fun adjustViewForNotch(view: View, activity: Activity) {
        val notchBounds = getNotchBounds(activity)
        if (notchBounds.isEmpty) return
        
        val layoutParams = view.layoutParams as? ViewGroup.MarginLayoutParams
        if (layoutParams != null) {
            // Check if view would intersect with notch
            if (viewIntersectsWithNotch(view, activity)) {
                // Adjust margins to flow around notch
                layoutParams.leftMargin = maxOf(layoutParams.leftMargin, notchBounds.right)
                view.layoutParams = layoutParams
            }
        }
    }
    
    /**
     * Configure content to flow around notch while ensuring status bar is not covered
     * This is specifically for pages that have content that might cover the time area
     */
    fun configureContentFlowWithStatusBarProtection(activity: Activity, rootView: View) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // Enable edge-to-edge with content flowing into cutout area
            activity.window.attributes.layoutInDisplayCutoutMode = 
                android.view.WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT
            
            // Make content flow around the notch while protecting status bar area
            ViewCompat.setOnApplyWindowInsetsListener(rootView) { view, windowInsets ->
                val cutoutInsets = windowInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
                val systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())
                
                // Ensure status bar area is always protected while allowing notch flow
                val topPadding = maxOf(systemBars.top, 24) // Minimum 24dp for status bar
                view.setPadding(0, topPadding, 0, systemBars.bottom)
                
                // Apply notch-aware layout to child views
                applyNotchFlowToChildren(view, cutoutInsets)
                
                windowInsets
            }
        }
    }
}
