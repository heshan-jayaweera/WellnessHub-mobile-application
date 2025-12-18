package com.example.wellnesshub

import android.content.Intent
import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import android.graphics.Color
import android.view.animation.DecelerateInterpolator
import com.example.wellnesshub.data.AuthPreferences
import com.example.wellnesshub.data.WellnessPreferences
import com.example.wellnesshub.utils.SystemUIHelper
import com.example.wellnesshub.utils.NotchFlowHelper
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.content.Context

class HomeActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lastAccel: Float = 0f
    private var currentAccel: Float = 0f
    private var accel: Float = 0f
    private var stepCount: Int = 0
    private var lastStepTimeMs: Long = 0
    private var totalStepSensorInitial: Int? = null
    private var lastMinuteWindowStart: Long = 0
    private var stepsInCurrentMinute: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure system UI for punch hole with bottom navigation awareness
        SystemUIHelper.configureForPunchHoleWithBottomNav(this)
        
        setContentView(R.layout.activity_home)
        
        // Configure content to flow organically around notch like text around a hole
        NotchFlowHelper.configureContentFlowAroundNotchWithBottomNav(this, findViewById(android.R.id.content))

        setupBottomNavigation()
        setupTodayProgress()
        setupWeeklyProgress()
        setupQuickActions()
        setupWelcome()
        setupEmojiSelector()
        setupSensors()
        
        // Add entrance animations
        animateViews()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> true
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
        bottomNav?.selectedItemId = R.id.nav_home
    }

    private fun setupTodayProgress() {
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSteps)
        val progressText = findViewById<TextView>(R.id.progressTextSteps)
        val stepsCountText = findViewById<TextView>(R.id.stepsCountText)
        val stepsRemainingText = findViewById<TextView>(R.id.stepsRemainingText)
        val achievementBadge = findViewById<LinearLayout>(R.id.stepsAchievementBadge)

        // Update steps display
        updateStepsDisplay()
        
        progressBar?.let { bar ->
            // Set max to 8000 steps
            bar.max = 8000
            // Animate progress from 0 to current step count
            val animator = ObjectAnimator.ofInt(bar, "progress", 0, stepCount)
            animator.duration = 1500
            animator.interpolator = DecelerateInterpolator()
            animator.start()
        }
        
        // Update progress text and remaining steps
        updateStepsProgressText()
        
        // Show achievement badge if goal is reached
        if (stepCount >= 8000) {
            achievementBadge?.visibility = View.VISIBLE
        } else {
            achievementBadge?.visibility = View.GONE
        }
    }

    private fun updateStepsDisplay() {
        findViewById<TextView>(R.id.stepsCountText)?.text = stepCount.toString()
    }

    private fun updateStepsProgressText() {
        val progressText = findViewById<TextView>(R.id.progressTextSteps)
        val stepsRemainingText = findViewById<TextView>(R.id.stepsRemainingText)
        
        val percentage = if (stepCount > 0) (stepCount * 100) / 8000 else 0
        val remaining = maxOf(0, 8000 - stepCount)
        
        progressText?.text = "$percentage%"
        stepsRemainingText?.text = "$remaining remaining"
    }

    private fun updateAchievementBadge() {
        val achievementBadge = findViewById<LinearLayout>(R.id.stepsAchievementBadge)
        if (stepCount >= 8000) {
            achievementBadge?.visibility = View.VISIBLE
        } else {
            achievementBadge?.visibility = View.GONE
        }
    }

    // Enhanced real-time update methods
    private fun detectStep(accel: Float, delta: Float, now: Long): Boolean {
        // Multiple criteria for better step detection
        val timeSinceLastStep = now - lastStepTimeMs
        val accelerationThreshold = SensorManager.GRAVITY_EARTH + 1.5f
        val deltaThreshold = 0.5f
        val minTimeBetweenSteps = 200L // Minimum 200ms between steps
        
        return (accel > accelerationThreshold && 
                kotlin.math.abs(delta) > deltaThreshold && 
                timeSinceLastStep > minTimeBetweenSteps)
    }

    private fun updateStepsDisplayRealtime() {
        val stepsCountText = findViewById<TextView>(R.id.stepsCountText)
        stepsCountText?.text = stepCount.toString()
        
        // Add subtle animation for real-time updates
        stepsCountText?.animate()
            ?.scaleX(1.1f)
            ?.scaleY(1.1f)
            ?.setDuration(100)
            ?.withEndAction {
                stepsCountText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
            }
    }

    private fun updateStepsProgressTextRealtime() {
        val progressText = findViewById<TextView>(R.id.progressTextSteps)
        val stepsRemainingText = findViewById<TextView>(R.id.stepsRemainingText)
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSteps)
        
        val percentage = if (stepCount > 0) (stepCount * 100) / 8000 else 0
        val remaining = maxOf(0, 8000 - stepCount)
        
        progressText?.text = "$percentage%"
        stepsRemainingText?.text = "$remaining remaining"
        
        // Animate progress bar in real-time
        progressBar?.let { bar ->
            bar.progress = stepCount
        }
        
        // Add subtle animation for progress text
        progressText?.animate()
            ?.scaleX(1.05f)
            ?.scaleY(1.05f)
            ?.setDuration(150)
            ?.withEndAction {
                progressText.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
            }
    }

    private fun updateAchievementBadgeRealtime() {
        val achievementBadge = findViewById<LinearLayout>(R.id.stepsAchievementBadge)
        if (stepCount >= 8000) {
            if (achievementBadge?.visibility != View.VISIBLE) {
                achievementBadge?.visibility = View.VISIBLE
                // Animate achievement badge appearance
                achievementBadge?.alpha = 0f
                achievementBadge?.animate()
                    ?.alpha(1f)
                    ?.scaleX(1.1f)
                    ?.scaleY(1.1f)
                    ?.setDuration(500)
                    ?.withEndAction {
                        achievementBadge.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                    }
            }
        } else {
            achievementBadge?.visibility = View.GONE
        }
    }

    private fun animateStepDetection() {
        // Animate the progress bar container for visual feedback
        val progressBar = findViewById<ProgressBar>(R.id.progressBarSteps)
        progressBar?.animate()
            ?.scaleX(1.02f)
            ?.scaleY(1.02f)
            ?.setDuration(100)
            ?.withEndAction {
                progressBar.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
            }
    }

    private fun updateLiveAnalytics(x: Float, y: Float, z: Float, accel: Float, delta: Float) {
        // Live analytics functionality is now integrated into today's progress section
        // No separate analytics display needed
    }

    private fun setupWeeklyProgress() {
        val weeklyContainer = findViewById<LinearLayout>(R.id.
        weeklyChartContainer)

        // Create dummy weekly data with improved visualization
        val weeklyData = listOf(0.8f, 0.9f, 0.7f, 0.95f, 0.85f, 0.6f, 0.75f)
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        weeklyContainer?.let { container ->
            for (i in weeklyData.indices) {
                val barContainer = LinearLayout(this).apply {
                    orientation = LinearLayout.VERTICAL
                    layoutParams = LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1.0f
                    )
                    gravity = android.view.Gravity.BOTTOM
                    setPadding(4, 0, 4, 0)
                }

                // Create enhanced progress bar for each day
                val bar = View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        0,
                        weeklyData[i]
                    )
                    background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.weekly_progress_bar)
                }

                // Create day label with better styling
                val dayLabel = TextView(this).apply {
                    text = dayLabels[i]
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    gravity = android.view.Gravity.CENTER
                    setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.hydration_blue_primary))
                    textSize = 10f
                    setTypeface(null, android.graphics.Typeface.BOLD)
                    setPadding(0, 8, 0, 0)
                }

                barContainer.addView(bar)
                barContainer.addView(dayLabel)
                container.addView(barContainer)
            }
        }
    }

    private fun setupQuickActions() {
        // Achievements Button
        findViewById<LinearLayout>(R.id.btnAchievements)?.apply {
            setOnClickListener {
                animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
                startActivity(Intent(this@HomeActivity, AchievementsActivity::class.java))
            }
        }
        
        // Tips Button
        findViewById<LinearLayout>(R.id.btnTips)?.apply {
            setOnClickListener {
                animate().scaleX(0.95f).scaleY(0.95f).setDuration(100).withEndAction {
                    animate().scaleX(1f).scaleY(1f).setDuration(100)
                }
                startActivity(Intent(this@HomeActivity, TipsActivity::class.java))
            }
        }
    }

    private fun setupWelcome() {
        val authPrefs = AuthPreferences(this)
        val username = authPrefs.getUsername() ?: "User"
        findViewById<TextView>(R.id.textWelcomeTitle)?.text = "Welcome, $username!"
        findViewById<TextView>(R.id.textWelcomeSubtitle)?.text = "Let's track your wellness today"
    }

    private fun setupEmojiSelector() {
        val wellnessPrefs = WellnessPreferences(this)
        val emojis = listOf("😊", "😄", "😌", "😔", "😢", "😡", "😴", "🤔", "😍", "😎")
        val container = findViewById<LinearLayout>(R.id.emojiContainer) ?: return
        container.removeAllViews()
        emojis.forEach { emoji ->
            val tv = TextView(this).apply {
                text = emoji
                textSize = 28f
                setPadding(16, 8, 16, 8)
                setOnClickListener {
                    val date = wellnessPrefs.getCurrentDate()
                    val time = wellnessPrefs.getCurrentTime()
                    val entry = com.example.wellnesshub.data.MoodEntry(
                        id = java.util.UUID.randomUUID().toString(),
                        emoji = emoji,
                        note = "",
                        date = date,
                        time = time
                    )
                    wellnessPrefs.saveMoodEntry(entry)
                    android.widget.Toast.makeText(this@HomeActivity, "Mood added: $emoji", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
            container.addView(tv)
        }
    }

    private fun setupSensors() {
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        
        // Enhanced sensor setup for real-time analytics
        accelerometer?.let { sensor ->
            // Use faster sampling rate for real-time updates
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
        }
        
        // Prefer hardware step sensors if available - use fastest rate
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_FASTEST)
        }
        
        // Initialize sensor values
        lastAccel = SensorManager.GRAVITY_EARTH
        currentAccel = SensorManager.GRAVITY_EARTH
        accel = 0.00f
        
        // Initialize time tracking for real-time analytics
        lastMinuteWindowStart = System.currentTimeMillis()
        stepsInCurrentMinute = 0
    }

    private fun animateViews() {
        // Simple fade in animation for the entire content
        val contentView = findViewById<View>(android.R.id.content)
        contentView.visibility = View.INVISIBLE
        
        contentView.postDelayed({
            contentView.visibility = View.VISIBLE
        }, 100)
    }

    override fun onResume() {
        super.onResume()
        accelerometer?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        // Load persisted steps for today and render weekly chart
        renderWeeklyStepsChart()
        updatePersistedSteps()
        setupWelcome()
    }

    override fun onPause() {
        sensorManager.unregisterListener(this)
        super.onPause()
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAccel = currentAccel
            currentAccel = kotlin.math.sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta = currentAccel - lastAccel
            accel = accel * 0.9f + delta
            
            // Enhanced step detection with multiple criteria
            val now = System.currentTimeMillis()
            val stepDetected = detectStep(currentAccel, delta, now)
            
            if (stepDetected) {
                stepCount += 1
                lastStepTimeMs = now
                updateStepsDisplayRealtime()
                updateStepsProgressTextRealtime()
                updateAchievementBadgeRealtime()
                updateStepsPerMinute(now)
                
                // Add visual feedback for step detection
                animateStepDetection()
            }
            
            // Enhanced shake detection for mood
            if (accel > 12) {
                val wellnessPrefs = WellnessPreferences(this)
                val entry = com.example.wellnesshub.data.MoodEntry(
                    id = java.util.UUID.randomUUID().toString(),
                    emoji = "🙂",
                    note = "Quick shake mood",
                    date = wellnessPrefs.getCurrentDate(),
                    time = wellnessPrefs.getCurrentTime()
                )
                wellnessPrefs.saveMoodEntry(entry)
                android.widget.Toast.makeText(this, "Quick mood added via shake", android.widget.Toast.LENGTH_SHORT).show()
            }

            // Update live analytics with enhanced data
            updateLiveAnalytics(x, y, z, currentAccel, delta)
            updateStepsPerMinute(now)
            
        } else if (event?.sensor?.type == Sensor.TYPE_STEP_DETECTOR) {
            // Hardware step detector - most accurate
            stepCount += 1
            val now = System.currentTimeMillis()
            updateStepsDisplayRealtime()
            updateStepsProgressTextRealtime()
            updateAchievementBadgeRealtime()
            updateStepsPerMinute(now)
            animateStepDetection()
            
        } else if (event?.sensor?.type == Sensor.TYPE_STEP_COUNTER) {
            // Cumulative count since last reboot - most reliable
            val totalSinceBoot = event.values[0].toInt()
            if (totalStepSensorInitial == null) {
                totalStepSensorInitial = totalSinceBoot
            }
            val base = totalStepSensorInitial ?: totalSinceBoot
            val newStepCount = (totalSinceBoot - base).coerceAtLeast(0)
            
            // Only update if there's a change to avoid unnecessary updates
            if (newStepCount != stepCount) {
                stepCount = newStepCount
                val now = System.currentTimeMillis()
                updateStepsDisplayRealtime()
                updateStepsProgressTextRealtime()
                updateAchievementBadgeRealtime()
                updateStepsPerMinute(now)
            }
        }
        
        // Persist current steps for today frequently
        persistTodaySteps()
    }

    private fun persistTodaySteps() {
        val prefs = WellnessPreferences(this)
        val today = prefs.getCurrentDate()
        prefs.setStepsForDate(today, stepCount)
    }

    private fun updatePersistedSteps() {
        val prefs = WellnessPreferences(this)
        val today = prefs.getCurrentDate()
        stepCount = prefs.getStepsForDate(today)
        updateStepsDisplay()
        updateStepsProgressText()
        updateAchievementBadge()
    }

    private fun renderWeeklyStepsChart() {
        val prefs = WellnessPreferences(this)
        val container = findViewById<LinearLayout>(R.id.stepsChartContainer) ?: return
        container.removeAllViews()
        val calendar = java.util.Calendar.getInstance()
        // Build last 7 days including today
        val dayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val days = mutableListOf<String>()
        for (i in 6 downTo 0) {
            val cal = java.util.Calendar.getInstance()
            cal.add(java.util.Calendar.DAY_OF_YEAR, -i)
            val year = cal.get(java.util.Calendar.YEAR)
            val month = cal.get(java.util.Calendar.MONTH) + 1
            val day = cal.get(java.util.Calendar.DAY_OF_MONTH)
            days.add(String.format("%04d-%02d-%02d", year, month, day))
        }
        val steps = days.map { prefs.getStepsForDate(it) }
        val maxSteps = (steps.maxOrNull() ?: 1).coerceAtLeast(1)
        for (i in steps.indices) {
            val barContainer = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    1.0f
                )
                gravity = android.view.Gravity.BOTTOM
                setPadding(4, 0, 4, 0)
            }
            val heightWeight = if (maxSteps == 0) 0f else (steps[i].toFloat() / maxSteps.toFloat()).coerceIn(0f, 1f)
            val bar = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    0,
                    if (heightWeight == 0f) 0.02f else heightWeight
                )
                background = ContextCompat.getDrawable(this@HomeActivity, R.drawable.weekly_progress_bar)
            }
            val label = TextView(this).apply {
                text = dayLabels[(i + 1) % 7]
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
                gravity = android.view.Gravity.CENTER
                setTextColor(ContextCompat.getColor(this@HomeActivity, R.color.hydration_blue_primary))
                textSize = 10f
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(0, 8, 0, 0)
            }
            barContainer.addView(bar)
            barContainer.addView(label)
            container.addView(barContainer)
        }
    }

    private fun updateStepsPerMinute(now: Long) {
        if (lastMinuteWindowStart == 0L) lastMinuteWindowStart = now
        // Count steps in rolling 60s window using naive approach: if a step happened now, increment
        if (now - lastStepTimeMs < 100) {
            stepsInCurrentMinute += 1
        }
        // Reset window every 60s
        if (now - lastMinuteWindowStart >= 60_000) {
            lastMinuteWindowStart = now
            stepsInCurrentMinute = 0
        }
        // Analytics functionality is now integrated into today's progress section
        // No separate analytics display needed
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { }
}
