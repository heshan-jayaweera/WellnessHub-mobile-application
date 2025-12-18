package com.example.wellnesshub.data

import java.util.Date

// Data classes for wellness app
data class Habit(
    val id: String,
    val name: String,
    val description: String,
    val targetCount: Int,
    val unit: String,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

data class HabitCompletion(
    val habitId: String,
    val date: String, // YYYY-MM-DD format
    val completedCount: Int,
    val timestamp: Long = System.currentTimeMillis()
)

data class MoodEntry(
    val id: String,
    val emoji: String,
    val note: String = "",
    val date: String, // YYYY-MM-DD format
    val time: String, // HH:MM format
    val timestamp: Long = System.currentTimeMillis()
)

data class HydrationReminder(
    val id: String,
    val intervalMinutes: Int,
    val isEnabled: Boolean,
    val startTime: String, // HH:MM format
    val endTime: String, // HH:MM format
    val message: String = "Time to hydrate! 💧"
)

data class WellnessStats(
    val date: String,
    val habitsCompleted: Int,
    val totalHabits: Int,
    val moodScore: Int, // 1-5 scale
    val waterIntake: Int, // in ml
    val steps: Int = 0
)
