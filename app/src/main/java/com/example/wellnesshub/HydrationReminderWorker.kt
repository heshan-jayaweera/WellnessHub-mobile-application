package com.example.wellnesshub

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.text.SimpleDateFormat
import java.util.*

class HydrationReminderWorker(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {
    
    override fun doWork(): Result {
        try {
            val message = inputData.getString("message") ?: "Time to hydrate! 💧"
            val startTime = inputData.getString("startTime") ?: "08:00"
            val endTime = inputData.getString("endTime") ?: "22:00"
            
            val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            
            android.util.Log.d("HydrationReminderWorker", "Checking reminder: current=$currentTime, start=$startTime, end=$endTime")
            
            // Check if current time is within the reminder window
            if (isTimeInRange(currentTime, startTime, endTime)) {
                showNotification(message)
                android.util.Log.d("HydrationReminderWorker", "Notification sent: $message")
            } else {
                android.util.Log.d("HydrationReminderWorker", "Outside reminder window, skipping notification")
            }
            
            return Result.success()
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminderWorker", "Error in doWork", e)
            return Result.retry()
        }
    }
    
    private fun isTimeInRange(current: String, start: String, end: String): Boolean {
        val currentMinutes = timeToMinutes(current)
        val startMinutes = timeToMinutes(start)
        val endMinutes = timeToMinutes(end)
        
        return if (startMinutes <= endMinutes) {
            currentMinutes in startMinutes..endMinutes
        } else {
            // Handle overnight range (e.g., 22:00 to 08:00)
            currentMinutes >= startMinutes || currentMinutes <= endMinutes
        }
    }
    
    private fun timeToMinutes(time: String): Int {
        val parts = time.split(":")
        return parts[0].toInt() * 60 + parts[1].toInt()
    }
    
    private fun showNotification(message: String) {
        try {
            val notificationManager = applicationContext
                .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            
            // Ensure notification channel exists
            createNotificationChannel(notificationManager)
            
            val intent = Intent(applicationContext, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                applicationContext,
                System.currentTimeMillis().toInt(), // Use unique request code
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            val notification = NotificationCompat.Builder(applicationContext, "hydration_reminders")
                .setSmallIcon(R.drawable.ic_water_drop)
                .setContentTitle("💧 Hydration Reminder")
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_ALL)
                .setCategory(NotificationCompat.CATEGORY_REMINDER)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .build()
            
            // Use unique notification ID based on current time
            val notificationId = (System.currentTimeMillis() % 10000).toInt()
            notificationManager.notify(notificationId, notification)
            
            android.util.Log.d("HydrationReminderWorker", "Notification created with ID: $notificationId")
            
        } catch (e: Exception) {
            android.util.Log.e("HydrationReminderWorker", "Failed to show notification", e)
        }
    }
    
    private fun createNotificationChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
                android.util.Log.d("HydrationReminderWorker", "Notification channel created with HIGH importance")
            } else {
                android.util.Log.d("HydrationReminderWorker", "Notification channel already exists with importance: ${existingChannel.importance}")
            }
        }
    }
}
