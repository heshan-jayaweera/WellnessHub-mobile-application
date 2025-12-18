package com.example.wellnesshub

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wellnesshub.data.MoodEntry
import com.example.wellnesshub.data.WellnessPreferences
import com.example.wellnesshub.utils.SystemUIHelper
import com.example.wellnesshub.utils.NotchFlowHelper
import java.text.SimpleDateFormat
import java.util.*

class MoodJournalActivity : AppCompatActivity() {
    
    private lateinit var wellnessPrefs: WellnessPreferences
    private lateinit var moodRecyclerView: RecyclerView
    private lateinit var moodAdapter: MoodAdapter
    private lateinit var addMoodButton: Button
    private lateinit var calendarView: CalendarView
    
    private val emojis = listOf("😊", "😄", "😌", "😔", "😢", "😡", "😴", "🤔", "😍", "😎")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure system UI for punch hole with bottom navigation awareness
        SystemUIHelper.configureForPunchHoleWithBottomNav(this)
        
        setContentView(R.layout.activity_mood_journal)
        
        // Configure content to flow organically around notch like text around a hole
        NotchFlowHelper.configureContentFlowAroundNotchWithBottomNav(this, findViewById(android.R.id.content))
        
        wellnessPrefs = WellnessPreferences(this)
        
        setupViews()
        setupRecyclerView()
        setupCalendar()
        setupBottomNavigation()
    }
    
    private fun setupViews() {
        moodRecyclerView = findViewById(R.id.moodRecyclerView)
        addMoodButton = findViewById(R.id.addMoodButton)
        calendarView = findViewById(R.id.calendarView)
        
        // Add mood button
        addMoodButton.setOnClickListener {
            showAddMoodDialog()
        }
    }
    
    private fun setupRecyclerView() {
        moodAdapter = MoodAdapter(wellnessPrefs.getMoodEntries()) { moodEntry ->
            showEditMoodDialog(moodEntry)
        }
        moodRecyclerView.layoutManager = LinearLayoutManager(this)
        moodRecyclerView.adapter = moodAdapter
    }
    
    private fun setupCalendar() {
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val selectedDate = String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth)
            val moodEntries = wellnessPrefs.getMoodEntriesForDate(selectedDate)
            moodAdapter.updateMoods(moodEntries)
        }
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_navigation)
        bottomNav?.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_mood -> true
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
        bottomNav?.selectedItemId = R.id.nav_mood
    }
    
    private fun showAddMoodDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_mood, null)
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emojiGrid)
        val noteEditText = dialogView.findViewById<EditText>(R.id.editTextMoodNote)
        val selectedEmojiText = dialogView.findViewById<TextView>(R.id.selectedEmojiText)
        
        // Create emoji buttons
        var selectedEmoji = ""
        for (emoji in emojis) {
            val button = Button(this).apply {
                text = emoji
                textSize = 24f
                layoutParams = ViewGroup.LayoutParams(120, 120)
                setOnClickListener {
                    selectedEmoji = emoji
                    selectedEmojiText.text = "Selected: $emoji"
                    selectedEmojiText.visibility = View.VISIBLE
                }
            }
            emojiGrid.addView(button)
        }
        
        AlertDialog.Builder(this)
            .setTitle("Log Your Mood")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                if (selectedEmoji.isNotEmpty()) {
                    val note = noteEditText.text.toString().trim()
                    val currentDate = wellnessPrefs.getCurrentDate()
                    val currentTime = wellnessPrefs.getCurrentTime()
                    
                    val moodEntry = MoodEntry(
                        id = UUID.randomUUID().toString(),
                        emoji = selectedEmoji,
                        note = note,
                        date = currentDate,
                        time = currentTime
                    )
                    
                    wellnessPrefs.saveMoodEntry(moodEntry)
                    moodAdapter.updateMoods(wellnessPrefs.getMoodEntries())
                    Toast.makeText(this, "Mood logged successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Please select an emoji", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditMoodDialog(moodEntry: MoodEntry) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_mood, null)
        val emojiGrid = dialogView.findViewById<GridLayout>(R.id.emojiGrid)
        val noteEditText = dialogView.findViewById<EditText>(R.id.editTextMoodNote)
        val selectedEmojiText = dialogView.findViewById<TextView>(R.id.selectedEmojiText)
        
        // Pre-fill with existing data
        noteEditText.setText(moodEntry.note)
        selectedEmojiText.text = "Selected: ${moodEntry.emoji}"
        selectedEmojiText.visibility = View.VISIBLE
        
        // Create emoji buttons
        var selectedEmoji = moodEntry.emoji
        for (emoji in emojis) {
            val button = Button(this).apply {
                text = emoji
                textSize = 24f
                layoutParams = ViewGroup.LayoutParams(120, 120)
                if (emoji == moodEntry.emoji) {
                    background = getDrawable(R.drawable.button_primary_gradient)
                    setTextColor(getColor(android.R.color.white))
                }
                setOnClickListener {
                    selectedEmoji = emoji
                    selectedEmojiText.text = "Selected: $emoji"
                    selectedEmojiText.visibility = View.VISIBLE
                    // Reset all buttons
                    for (i in 0 until emojiGrid.childCount) {
                        val child = emojiGrid.getChildAt(i) as Button
                        child.background = getDrawable(R.drawable.button_secondary_gradient)
                        child.setTextColor(getColor(android.R.color.white))
                    }
                    // Highlight selected
                    background = getDrawable(R.drawable.button_primary_gradient)
                    setTextColor(getColor(android.R.color.white))
                }
            }
            emojiGrid.addView(button)
        }
        
        AlertDialog.Builder(this)
            .setTitle("Edit Mood Entry")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val note = noteEditText.text.toString().trim()
                val updatedMoodEntry = moodEntry.copy(
                    emoji = selectedEmoji,
                    note = note
                )
                
                // Update in preferences (we'll need to implement this method)
                updateMoodEntry(updatedMoodEntry)
                moodAdapter.updateMoods(wellnessPrefs.getMoodEntries())
                Toast.makeText(this, "Mood updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .setNeutralButton("Delete") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Mood Entry")
                    .setMessage("Are you sure you want to delete this mood entry?")
                    .setPositiveButton("Delete") { _, _ ->
                        deleteMoodEntry(moodEntry.id)
                        moodAdapter.updateMoods(wellnessPrefs.getMoodEntries())
                        Toast.makeText(this, "Mood entry deleted", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun updateMoodEntry(moodEntry: MoodEntry) {
        val entries = wellnessPrefs.getMoodEntries().toMutableList()
        val index = entries.indexOfFirst { it.id == moodEntry.id }
        if (index != -1) {
            entries[index] = moodEntry
            val json = com.google.gson.Gson().toJson(entries)
            wellnessPrefs.getSharedPreferences().edit()
                .putString("mood_entries", json).apply()
        }
    }
    
    private fun deleteMoodEntry(moodEntryId: String) {
        val entries = wellnessPrefs.getMoodEntries().toMutableList()
        entries.removeAll { it.id == moodEntryId }
        val json = com.google.gson.Gson().toJson(entries)
        wellnessPrefs.getSharedPreferences().edit()
            .putString("mood_entries", json).apply()
    }
    
    inner class MoodAdapter(
        private var moods: List<MoodEntry>,
        private val onMoodClick: (MoodEntry) -> Unit
    ) : RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {
        
        inner class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val emojiText: TextView = itemView.findViewById(R.id.moodEmoji)
            val dateText: TextView = itemView.findViewById(R.id.moodDate)
            val timeText: TextView = itemView.findViewById(R.id.moodTime)
            val noteText: TextView = itemView.findViewById(R.id.moodNote)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_mood, parent, false)
            return MoodViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
            val mood = moods[position]
            
            holder.emojiText.text = mood.emoji
            holder.dateText.text = formatDate(mood.date)
            holder.timeText.text = mood.time
            holder.noteText.text = mood.note.ifEmpty { "No note" }
            
            holder.itemView.setOnClickListener {
                onMoodClick(mood)
            }
        }
        
        override fun getItemCount() = moods.size
        
        fun updateMoods(newMoods: List<MoodEntry>) {
            moods = newMoods.sortedByDescending { it.timestamp }
            notifyDataSetChanged()
        }
        
        private fun formatDate(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                dateString
            }
        }
    }
}
