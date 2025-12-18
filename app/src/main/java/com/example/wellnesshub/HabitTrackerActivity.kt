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
import com.example.wellnesshub.data.Habit
import com.example.wellnesshub.data.WellnessPreferences
import com.example.wellnesshub.utils.SystemUIHelper
import com.example.wellnesshub.utils.NotchFlowHelper
import java.util.*

class HabitTrackerActivity : AppCompatActivity() {
    
    private lateinit var wellnessPrefs: WellnessPreferences
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var habitsAdapter: HabitsAdapter
    private lateinit var progressText: TextView
    private lateinit var addHabitButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Configure system UI for punch hole with bottom navigation awareness
        SystemUIHelper.configureForPunchHoleWithBottomNav(this)
        
        setContentView(R.layout.activity_habit_tracker)
        
        // Configure content to flow organically around notch like text around a hole
        NotchFlowHelper.configureContentFlowAroundNotchWithBottomNav(this, findViewById(android.R.id.content))
        
        wellnessPrefs = WellnessPreferences(this)
        
        setupViews()
        setupRecyclerView()
        updateProgress()
        setupBottomNavigation()
    }
    
    private fun setupViews() {
        habitsRecyclerView = findViewById(R.id.habitsRecyclerView)
        progressText = findViewById(R.id.progressText)
        addHabitButton = findViewById(R.id.addHabitButton)
        
        // Add habit button
        addHabitButton.setOnClickListener {
            showAddHabitDialog()
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
                R.id.nav_mood -> {
                    startActivity(Intent(this, MoodJournalActivity::class.java))
                    true
                }
                R.id.nav_habits -> true
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
        bottomNav?.selectedItemId = R.id.nav_habits
    }
    
    private fun setupRecyclerView() {
        habitsAdapter = HabitsAdapter(wellnessPrefs.getHabits()) { habit ->
            showEditHabitDialog(habit)
        }
        habitsRecyclerView.layoutManager = LinearLayoutManager(this)
        habitsRecyclerView.adapter = habitsAdapter
    }
    
    private fun updateProgress() {
        val today = wellnessPrefs.getCurrentDate()
        val habits = wellnessPrefs.getHabits()
        val completions = wellnessPrefs.getHabitCompletionsForDate(today)
        
        val completedCount = completions.size
        val totalCount = habits.size
        
        val progressPercentage = if (totalCount > 0) {
            (completedCount * 100) / totalCount
        } else {
            0
        }
        
        progressText.text = "Today's Progress: $completedCount/$totalCount habits completed ($progressPercentage%)"
    }
    
    private fun showAddHabitDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextHabitName)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editTextHabitDescription)
        val targetEditText = dialogView.findViewById<EditText>(R.id.editTextTargetCount)
        val unitEditText = dialogView.findViewById<EditText>(R.id.editTextUnit)
        
        AlertDialog.Builder(this)
            .setTitle("Add New Habit")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetText = targetEditText.text.toString().trim()
                val unit = unitEditText.text.toString().trim()
                
                if (name.isNotEmpty() && targetText.isNotEmpty()) {
                    val target = targetText.toIntOrNull() ?: 1
                    val habit = Habit(
                        id = UUID.randomUUID().toString(),
                        name = name,
                        description = description,
                        targetCount = target,
                        unit = unit.ifEmpty { "times" }
                    )
                    
                    wellnessPrefs.addHabit(habit)
                    habitsAdapter.updateHabits(wellnessPrefs.getHabits())
                    updateProgress()
                } else {
                    Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showEditHabitDialog(habit: Habit) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_habit, null)
        val nameEditText = dialogView.findViewById<EditText>(R.id.editTextHabitName)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editTextHabitDescription)
        val targetEditText = dialogView.findViewById<EditText>(R.id.editTextTargetCount)
        val unitEditText = dialogView.findViewById<EditText>(R.id.editTextUnit)
        
        // Pre-fill with existing data
        nameEditText.setText(habit.name)
        descriptionEditText.setText(habit.description)
        targetEditText.setText(habit.targetCount.toString())
        unitEditText.setText(habit.unit)
        
        AlertDialog.Builder(this)
            .setTitle("Edit Habit")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val name = nameEditText.text.toString().trim()
                val description = descriptionEditText.text.toString().trim()
                val targetText = targetEditText.text.toString().trim()
                val unit = unitEditText.text.toString().trim()
                
                if (name.isNotEmpty() && targetText.isNotEmpty()) {
                    val target = targetText.toIntOrNull() ?: 1
                    val updatedHabit = habit.copy(
                        name = name,
                        description = description,
                        targetCount = target,
                        unit = unit.ifEmpty { "times" }
                    )
                    
                    wellnessPrefs.updateHabit(updatedHabit)
                    habitsAdapter.updateHabits(wellnessPrefs.getHabits())
                    updateProgress()
                } else {
                    Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Delete") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Habit")
                    .setMessage("Are you sure you want to delete this habit?")
                    .setPositiveButton("Delete") { _, _ ->
                        wellnessPrefs.deleteHabit(habit.id)
                        habitsAdapter.updateHabits(wellnessPrefs.getHabits())
                        updateProgress()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    inner class HabitsAdapter(
        private var habits: List<Habit>,
        private val onHabitClick: (Habit) -> Unit
    ) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {
        
        inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText: TextView = itemView.findViewById(R.id.habitName)
            val descriptionText: TextView = itemView.findViewById(R.id.habitDescription)
            val targetText: TextView = itemView.findViewById(R.id.habitTarget)
            val progressBar: ProgressBar = itemView.findViewById(R.id.habitProgressBar)
            val progressText: TextView = itemView.findViewById(R.id.habitProgressText)
            val completeButton: Button = itemView.findViewById(R.id.completeButton)
        }
        
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_habit, parent, false)
            return HabitViewHolder(view)
        }
        
        override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
            val habit = habits[position]
            val today = wellnessPrefs.getCurrentDate()
            val completion = wellnessPrefs.getHabitCompletionsForDate(today)
                .find { it.habitId == habit.id }
            
            holder.nameText.text = habit.name
            holder.descriptionText.text = habit.description
            holder.targetText.text = "Target: ${habit.targetCount} ${habit.unit}"
            
            val completedCount = completion?.completedCount ?: 0
            val progress = if (habit.targetCount > 0) {
                (completedCount * 100) / habit.targetCount
            } else {
                0
            }
            
            holder.progressBar.progress = progress
            holder.progressText.text = "$completedCount/${habit.targetCount}"
            
            holder.completeButton.setOnClickListener {
                val newCount = completedCount + 1
                val habitCompletion = com.example.wellnesshub.data.HabitCompletion(
                    habitId = habit.id,
                    date = today,
                    completedCount = newCount
                )
                wellnessPrefs.saveHabitCompletion(habitCompletion)
                notifyItemChanged(position)
                updateProgress()
            }
            
            holder.itemView.setOnClickListener {
                onHabitClick(habit)
            }
        }
        
        override fun getItemCount() = habits.size
        
        fun updateHabits(newHabits: List<Habit>) {
            habits = newHabits
            notifyDataSetChanged()
        }
    }
}
