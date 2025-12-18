package com.example.wellnesshub

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.work.*
import com.example.wellnesshub.data.HydrationReminder
import com.example.wellnesshub.data.WellnessPreferences
import java.util.concurrent.TimeUnit

class HydrationReminderActivity : AppCompatActivity() {
    
    private lateinit var wellnessPrefs: WellnessPreferences
    private lateinit var intervalSpinner: Spinner
    private lateinit var startTimePicker: TimePicker
    private lateinit var endTimePicker: TimePicker
    private lateinit var messageEditText: EditText
    private lateinit var enableSwitch: Switch
    private lateinit var saveButton: Button
    
    // Water tracking variables
    private lateinit var waterProgressBar: ProgressBar
    private lateinit var waterConsumedText: TextView
    private lateinit var waterProgressText: TextView
    private lateinit var waterRemainingText: TextView
    private lateinit var btnAddWater250: Button
    private lateinit var btnAddWater500: Button
    private lateinit var btnAddWater1000: Button
    private lateinit var btnUndoWater: Button
    private lateinit var btnResetWater: Button
    
    private var waterConsumed: Float = 0f // in milliliters
    private val waterGoal: Float = 7000f // 7 liters in milliliters
    private var lastWaterAddition: Float = 0f // Track last addition for undo
    private var canUndo: Boolean = false // Whether undo is available
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hydration_reminder)
        
        wellnessPrefs = WellnessPreferences(this)
        createNotificationChannel()
        
        // Request notification permission for Android 13+
        requestNotificationPermission()
        
        setupViews()
        setupWaterTracking()
        loadExistingSettings()
        setupBottomNavigation()
    }
    
    private fun setupViews() {
        intervalSpinner = findViewById(R.id.intervalSpinner)
        startTimePicker = findViewById(R.id.startTimePicker)
        endTimePicker = findViewById(R.id.endTimePicker)
        messageEditText = findViewById(R.id.messageEditText)
        enableSwitch = findViewById(R.id.enableSwitch)
        saveButton = findViewById(R.id.saveButton)
        
        // Water tracking views
        waterProgressBar = findViewById(R.id.waterProgressBar)
        waterConsumedText = findViewById(R.id.waterConsumedText)
        waterProgressText = findViewById(R.id.waterProgressText)
        waterRemainingText = findViewById(R.id.waterRemainingText)
        btnAddWater250 = findViewById(R.id.btnAddWater250)
        btnAddWater500 = findViewById(R.id.btnAddWater500)
        btnAddWater1000 = findViewById(R.id.btnAddWater1000)
        btnUndoWater = findViewById(R.id.btnUndoWater)
        btnResetWater = findViewById(R.id.btnResetWater)
        
        // Setup interval spinner
        val intervals = arrayOf("1 minute", "15 minutes", "30 minutes", "45 minutes", "1 hour", "2 hours", "3 hours")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, intervals)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        intervalSpinner.adapter = adapter
        
        // Setup time pickers
        startTimePicker.setIs24HourView(true)
        endTimePicker.setIs24HourView(true)
        
        // Save button
        saveButton.setOnClickListener {
            saveReminderSettings()
        }
    }

    private fun setupWaterTracking() {
        // Load existing water consumption
        val previousWater = waterConsumed
        waterConsumed = wellnessPrefs.getWaterConsumed()
        
        // Check if it's a new day and show notification
        if (previousWater > 0 && waterConsumed == 0f) {
            showDailyResetNotification()
        }
        
        // Initialize undo state
        canUndo = false
        lastWaterAddition = 0f
        btnUndoWater.visibility = android.view.View.GONE
        
        // Setup water increment buttons
        btnAddWater250.setOnClickListener { addWater(250f) }
        btnAddWater500.setOnClickListener { addWater(500f) }
        btnAddWater1000.setOnClickListener { addWater(1000f) }
        btnUndoWater.setOnClickListener { undoLastWaterAddition() }
        btnResetWater.setOnClickListener { resetWaterConsumed() }
        
        // Update display
        updateWaterDisplay()
    }

    private fun addWater(amount: Float) {
        // Track the addition for undo functionality
        lastWaterAddition = amount
        canUndo = true
        
        waterConsumed += amount
        
        // Save to preferences
        wellnessPrefs.setWaterConsumed(waterConsumed)
        
        // Update display
        updateWaterDisplay()
        
        // Show undo button
        btnUndoWater.visibility = android.view.View.VISIBLE
        
        // Check for overdone state
        handleOverdoneState()
        
        // Show feedback based on consumption level
        val message = when {
            waterConsumed > waterGoal * 1.2f -> {
                "⚠️ Overhydration warning! You've exceeded 8.4L. Consider consulting a doctor."
            }
            waterConsumed > waterGoal -> {
                "🎉 Goal exceeded! You've had ${String.format("%.1f", waterConsumed / 1000)}L. Great job!"
            }
            waterConsumed >= waterGoal -> {
                "🎉 Goal reached! Great job staying hydrated!"
            }
            else -> {
                "💧 Added ${if (amount >= 1000) "${(amount/1000).toInt()}L" else "${amount.toInt()}ml"} of water"
            }
        }
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun updateWaterDisplay() {
        val percentage = ((waterConsumed / waterGoal) * 100).toInt()
        val remaining = waterGoal - waterConsumed
        
        // Update progress bar (cap at 100% for visual purposes)
        waterProgressBar.progress = minOf(waterConsumed.toInt(), waterGoal.toInt())
        
        // Update text displays
        waterConsumedText.text = "${String.format("%.1f", waterConsumed / 1000)}L"
        waterProgressText.text = "$percentage%"
        
        // Update remaining text based on state
        waterRemainingText.text = when {
            waterConsumed > waterGoal * 1.2f -> "⚠️ Overhydration risk!"
            waterConsumed > waterGoal -> "🎉 Goal exceeded by ${String.format("%.1f", (waterConsumed - waterGoal) / 1000)}L!"
            waterConsumed >= waterGoal -> "🎉 Goal achieved!"
            else -> "${String.format("%.1f", remaining / 1000)}L remaining"
        }
        
        // Change color based on progress and overdone state
        val color = when {
            waterConsumed > waterGoal * 1.2f -> android.graphics.Color.parseColor("#F44336") // Red - Overhydration warning
            waterConsumed > waterGoal -> android.graphics.Color.parseColor("#4CAF50") // Green - Goal exceeded
            percentage >= 100 -> android.graphics.Color.parseColor("#4CAF50") // Green - Goal reached
            percentage >= 75 -> android.graphics.Color.parseColor("#FF9800") // Orange - Close to goal
            else -> android.graphics.Color.parseColor("#2196F3") // Blue - Normal progress
        }
        waterProgressText.setTextColor(color)
        waterRemainingText.setTextColor(color)
    }

    private fun handleOverdoneState() {
        if (waterConsumed > waterGoal * 1.2f) {
            // Show overhydration warning dialog
            showOverhydrationWarning()
        }
    }

    private fun showOverhydrationWarning() {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle("⚠️ Overhydration Warning")
        builder.setMessage("You've consumed ${String.format("%.1f", waterConsumed / 1000)}L of water, which exceeds the recommended daily intake. Overhydration can be dangerous and may lead to water intoxication. Please consult a healthcare professional if you continue to feel thirsty or experience symptoms.")
        
        builder.setPositiveButton("I Understand") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.setNeutralButton("Undo Last Addition") { dialog, _ ->
            undoLastWaterAddition()
            dialog.dismiss()
        }
        
        builder.setNegativeButton("Continue Tracking") { dialog, _ ->
            dialog.dismiss()
        }
        
        builder.show()
    }

    private fun undoLastWaterAddition() {
        if (canUndo && lastWaterAddition > 0) {
            waterConsumed -= lastWaterAddition
            
            // Ensure water doesn't go below 0
            if (waterConsumed < 0) {
                waterConsumed = 0f
            }
            
            // Save to preferences
            wellnessPrefs.setWaterConsumed(waterConsumed)
            
            // Update display
            updateWaterDisplay()
            
            // Hide undo button
            btnUndoWater.visibility = android.view.View.GONE
            canUndo = false
            
            // Show feedback
            val undoAmount = if (lastWaterAddition >= 1000) "${(lastWaterAddition/1000).toInt()}L" else "${lastWaterAddition.toInt()}ml"
            Toast.makeText(this, "↶ Undid $undoAmount water addition", Toast.LENGTH_SHORT).show()
        }
    }

    private fun resetWaterConsumed() {
        // Show confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Reset Water Consumption")
            .setMessage("Are you sure you want to reset your water consumption to 0? This action cannot be undone.")
            .setPositiveButton("Reset") { _, _ ->
                // Reset water consumption
                waterConsumed = 0f
                lastWaterAddition = 0f
                canUndo = false
                
                // Save to preferences
                wellnessPrefs.setWaterConsumed(waterConsumed)
                
                // Update display
                updateWaterDisplay()
                
                // Hide undo button
                btnUndoWater.visibility = android.view.View.GONE
                
                // Show feedback
                Toast.makeText(this, "🔄 Water consumption reset to 0", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }


    private fun showDailyResetNotification() {
        Toast.makeText(this, "🌅 New day! Water intake reset to 0L. Start fresh!", Toast.LENGTH_LONG).show()
    }

    private fun resetWaterIntake() {
        waterConsumed = 0f
        wellnessPrefs.resetWaterConsumed()
        updateWaterDisplay()
        btnUndoWater.visibility = android.view.View.GONE
        canUndo = false
        Toast.makeText(this, "💧 Water intake reset to 0L", Toast.LENGTH_SHORT).show()
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
                R.id.nav_hydration -> true
                R.id.nav_profile -> {
                    startActivity(Intent(this, ProfileActivity::class.java))
                    true
                }
                else -> false
            }
        }
        bottomNav?.selectedItemId = R.id.nav_hydration
    }
    
    private fun loadExistingSettings() {
        val reminder = wellnessPrefs.getHydrationReminder()
        if (reminder != null) {
            enableSwitch.isChecked = reminder.isEnabled
            
            // Set interval
            val intervalIndex = when (reminder.intervalMinutes) {
                15 -> 0
                30 -> 1
                45 -> 2
                60 -> 3
                120 -> 4
                180 -> 5
                else -> 3
            }
            intervalSpinner.setSelection(intervalIndex)
            
            // Set start time
            val startTimeParts = reminder.startTime.split(":")
            startTimePicker.hour = startTimeParts[0].toInt()
            startTimePicker.minute = startTimeParts[1].toInt()
            
            // Set end time
            val endTimeParts = reminder.endTime.split(":")
            endTimePicker.hour = endTimeParts[0].toInt()
            endTimePicker.minute = endTimeParts[1].toInt()
            
            messageEditText.setText(reminder.message)
        }
    }
    
    private fun saveReminderSettings() {
        val isEnabled = enableSwitch.isChecked
        val intervalMinutes = when (intervalSpinner.selectedItemPosition) {
            0 -> 1
            1 -> 15
            2 -> 30
            3 -> 45
            4 -> 60
            5 -> 120
            6 -> 180
            else -> 60
        }
        
        val startHour = startTimePicker.hour
        val startMinute = startTimePicker.minute
        val endHour = endTimePicker.hour
        val endMinute = endTimePicker.minute
        
        val startTime = String.format("%02d:%02d", startHour, startMinute)
        val endTime = String.format("%02d:%02d", endHour, endMinute)
        val message = messageEditText.text.toString().trim().ifEmpty { "Time to hydrate! 💧" }
        
        val reminder = HydrationReminder(
            id = "hydration_reminder",
            intervalMinutes = intervalMinutes,
            isEnabled = isEnabled,
            startTime = startTime,
            endTime = endTime,
            message = message
        )
        
        wellnessPrefs.saveHydrationReminder(reminder)
        
        if (isEnabled) {
            scheduleReminders(reminder)
            Toast.makeText(this, "💧 Hydration reminders enabled! You'll be reminded every $intervalMinutes minutes.", Toast.LENGTH_LONG).show()
        } else {
            cancelReminders()
            Toast.makeText(this, "🔕 Hydration reminders disabled", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun scheduleReminders(reminder: HydrationReminder) {
        // Cancel existing reminders
        cancelReminders()
        
        if (!reminder.isEnabled) return
        
        try {
            val workManager = WorkManager.getInstance(this)
            
            // Create periodic work request with minimal constraints for better reliability
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .setRequiresBatteryNotLow(false)
                .setRequiresCharging(false)
                .setRequiresDeviceIdle(false)
                .setRequiresStorageNotLow(false)
                .build()
            
            val hydrationWork = PeriodicWorkRequestBuilder<HydrationReminderWorker>(
                reminder.intervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setInputData(workDataOf(
                    "message" to reminder.message,
                    "startTime" to reminder.startTime,
                    "endTime" to reminder.endTime
                ))
                .setBackoffCriteria(
                    BackoffPolicy.LINEAR,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()
            
            workManager.enqueueUniquePeriodicWork(
                "hydration_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                hydrationWork
            )
            
            // Log successful scheduling
            android.util.Log.d("HydrationReminder", "Reminders scheduled: interval=${reminder.intervalMinutes}min, start=${reminder.startTime}, end=${reminder.endTime}")
            
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminder", "Failed to schedule reminders", e)
            Toast.makeText(this, "⚠️ Failed to schedule reminders. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun cancelReminders() {
        val workManager = WorkManager.getInstance(this)
        workManager.cancelUniqueWork("hydration_reminder")
        android.util.Log.d("HydrationReminder", "Reminders cancelled")
    }
    
    
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) 
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
    
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1001 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "✅ Notification permission granted!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "⚠️ Notification permission denied. Reminders may not work.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Check if channel already exists
            val existingChannel = notificationManager.getNotificationChannel("hydration_reminders")
            if (existingChannel == null) {
                val channel = NotificationChannel(
                    "hydration_reminders",
                    "Hydration Reminders",
                    NotificationManager.IMPORTANCE_HIGH  // Changed to HIGH for better visibility
                ).apply {
                    description = "Reminds you to stay hydrated throughout the day"
                    enableVibration(true)
                    enableLights(true)
                    setShowBadge(true)
                    setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION), null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                }
                
                notificationManager.createNotificationChannel(channel)
                android.util.Log.d("HydrationReminder", "Notification channel created with HIGH importance")
            } else {
                android.util.Log.d("HydrationReminder", "Notification channel already exists with importance: ${existingChannel.importance}")
            }
        }
    }
}
