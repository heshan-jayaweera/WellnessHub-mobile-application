package com.example.wellnesshub

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.wellnesshub.data.WellnessPreferences

class WellnessWidgetProvider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onEnabled(context: Context) {
        // Called when the first widget is created
    }
    
    override fun onDisabled(context: Context) {
        // Called when the last widget is removed
    }
    
    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val wellnessPrefs = WellnessPreferences(context)
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
            
            val views = RemoteViews(context.packageName, R.layout.wellness_widget)
            
            // Update widget content
            views.setTextViewText(R.id.widgetTitle, "Today's Progress")
            views.setTextViewText(R.id.widgetProgress, "$completedCount/$totalCount")
            views.setTextViewText(R.id.widgetPercentage, "$progressPercentage%")
            
            // Set progress bar
            views.setProgressBar(R.id.widgetProgressBar, 100, progressPercentage, false)
            
            // Set click intent to open the app
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widgetContainer, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
