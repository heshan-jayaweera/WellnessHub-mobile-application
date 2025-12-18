package com.example.wellnesshub.data

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WellnessPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("wellness_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val KEY_HABITS = "habits"
        private const val KEY_HABIT_COMPLETIONS = "habit_completions"
        private const val KEY_MOOD_ENTRIES = "mood_entries"
        private const val KEY_HYDRATION_REMINDER = "hydration_reminder"
        private const val KEY_WELLNESS_STATS = "wellness_stats"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_DAILY_GOAL = "daily_goal"
        private const val KEY_DAILY_STEPS = "daily_steps"
    }

    // Habit management
    fun saveHabits(habits: List<Habit>) {
        val json = gson.toJson(habits)
        prefs.edit().putString(KEY_HABITS, json).apply()
    }

    fun getHabits(): List<Habit> {
        val json = prefs.getString(KEY_HABITS, null) ?: return emptyList()
        val type = object : TypeToken<List<Habit>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun addHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        habits.add(habit)
        saveHabits(habits)
    }

    fun updateHabit(habit: Habit) {
        val habits = getHabits().toMutableList()
        val index = habits.indexOfFirst { it.id == habit.id }
        if (index != -1) {
            habits[index] = habit
            saveHabits(habits)
        }
    }

    fun deleteHabit(habitId: String) {
        val habits = getHabits().toMutableList()
        habits.removeAll { it.id == habitId }
        saveHabits(habits)
    }

    // Habit completion tracking
    fun saveHabitCompletion(completion: HabitCompletion) {
        val completions = getHabitCompletions().toMutableList()
        val existingIndex = completions.indexOfFirst { 
            it.habitId == completion.habitId && it.date == completion.date 
        }
        
        if (existingIndex != -1) {
            completions[existingIndex] = completion
        } else {
            completions.add(completion)
        }
        
        val json = gson.toJson(completions)
        prefs.edit().putString(KEY_HABIT_COMPLETIONS, json).apply()
    }

    fun getHabitCompletions(): List<HabitCompletion> {
        val json = prefs.getString(KEY_HABIT_COMPLETIONS, null) ?: return emptyList()
        val type = object : TypeToken<List<HabitCompletion>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun getHabitCompletionsForDate(date: String): List<HabitCompletion> {
        return getHabitCompletions().filter { it.date == date }
    }

    // Mood journal
    fun saveMoodEntry(moodEntry: MoodEntry) {
        val entries = getMoodEntries().toMutableList()
        entries.add(moodEntry)
        val json = gson.toJson(entries)
        prefs.edit().putString(KEY_MOOD_ENTRIES, json).apply()
    }

    fun getMoodEntries(): List<MoodEntry> {
        val json = prefs.getString(KEY_MOOD_ENTRIES, null) ?: return emptyList()
        val type = object : TypeToken<List<MoodEntry>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun getMoodEntriesForDate(date: String): List<MoodEntry> {
        return getMoodEntries().filter { it.date == date }
    }

    // Hydration reminder
    fun saveHydrationReminder(reminder: HydrationReminder) {
        val json = gson.toJson(reminder)
        prefs.edit().putString(KEY_HYDRATION_REMINDER, json).apply()
    }

    fun getHydrationReminder(): HydrationReminder? {
        val json = prefs.getString(KEY_HYDRATION_REMINDER, null) ?: return null
        return gson.fromJson(json, HydrationReminder::class.java)
    }

    // Wellness stats
    fun saveWellnessStats(stats: WellnessStats) {
        val allStats = getWellnessStats().toMutableList()
        val existingIndex = allStats.indexOfFirst { it.date == stats.date }
        
        if (existingIndex != -1) {
            allStats[existingIndex] = stats
        } else {
            allStats.add(stats)
        }
        
        val json = gson.toJson(allStats)
        prefs.edit().putString(KEY_WELLNESS_STATS, json).apply()
    }

    fun getWellnessStats(): List<WellnessStats> {
        val json = prefs.getString(KEY_WELLNESS_STATS, null) ?: return emptyList()
        val type = object : TypeToken<List<WellnessStats>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun getWellnessStatsForDate(date: String): WellnessStats? {
        return getWellnessStats().find { it.date == date }
    }

    // User preferences
    fun setUserName(name: String) {
        prefs.edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserName(): String {
        return prefs.getString(KEY_USER_NAME, "User") ?: "User"
    }

    fun setDailyGoal(goal: Int) {
        prefs.edit().putInt(KEY_DAILY_GOAL, goal).apply()
    }

    fun getDailyGoal(): Int {
        return prefs.getInt(KEY_DAILY_GOAL, 8) // Default 8 glasses
    }

    // Steps persistence
    fun getStepsForDate(date: String): Int {
        val key = "$KEY_DAILY_STEPS:$date"
        return prefs.getInt(key, 0)
    }

    fun setStepsForDate(date: String, steps: Int) {
        val key = "$KEY_DAILY_STEPS:$date"
        prefs.edit().putInt(key, steps.coerceAtLeast(0)).apply()
    }

    // Utility methods
    fun getCurrentDate(): String {
        val calendar = java.util.Calendar.getInstance()
        val year = calendar.get(java.util.Calendar.YEAR)
        val month = calendar.get(java.util.Calendar.MONTH) + 1
        val day = calendar.get(java.util.Calendar.DAY_OF_MONTH)
        return String.format("%04d-%02d-%02d", year, month, day)
    }

    fun getCurrentTime(): String {
        val calendar = java.util.Calendar.getInstance()
        val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
        val minute = calendar.get(java.util.Calendar.MINUTE)
        return String.format("%02d:%02d", hour, minute)
    }
    
    fun getSharedPreferences(): SharedPreferences {
        return prefs
    }

    // Water tracking methods
    fun setWaterConsumed(amount: Float) {
        prefs.edit().putFloat("water_consumed", amount).apply()
        // Update last reset date to today
        prefs.edit().putString("water_last_reset_date", getCurrentDate()).apply()
    }

    fun getWaterConsumed(): Float {
        // Check if we need to reset for a new day
        checkAndResetDailyWater()
        return prefs.getFloat("water_consumed", 0f)
    }

    fun resetWaterConsumed() {
        prefs.edit().remove("water_consumed").apply()
        prefs.edit().putString("water_last_reset_date", getCurrentDate()).apply()
    }

    private fun checkAndResetDailyWater() {
        val lastResetDate = prefs.getString("water_last_reset_date", "")
        val currentDate = getCurrentDate()
        
        // If it's a new day, reset water consumption
        if (lastResetDate != currentDate) {
            prefs.edit().putFloat("water_consumed", 0f).apply()
            prefs.edit().putString("water_last_reset_date", currentDate).apply()
        }
    }
}
