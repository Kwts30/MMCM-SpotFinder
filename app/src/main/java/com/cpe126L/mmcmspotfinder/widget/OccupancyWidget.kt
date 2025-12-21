package com.cpe126L.mmcmspotfinder.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.cpe126L.mmcmspotfinder.MainActivity
import com.cpe126L.mmcmspotfinder.R
import com.cpe126L.mmcmspotfinder.ml.TimeOnlyPredictor
import com.cpe126L.mmcmspotfinder.viewmodel.OccClass
import java.time.*
import java.time.format.DateTimeFormatter

class OccupancyWidget : AppWidgetProvider() {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update each widget instance
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
}

internal fun updateAppWidget(
    context: Context,
    appWidgetManager: AppWidgetManager,
    appWidgetId: Int
) {
    val views = RemoteViews(context.packageName, R.layout.widget_occupancy)

    // Get current prediction
    val predictor = TimeOnlyPredictor(context)
    val zone = ZoneId.of("Asia/Manila")
    val now = ZonedDateTime.now(zone)
    
    val (isOpen, currentPercent, occupancyClass) = getCurrentOccupancy(predictor, now)
    
    // Update widget UI
    if (isOpen && currentPercent != null) {
        views.setTextViewText(R.id.widget_occupancy_percent, "${currentPercent}%")
        views.setTextViewText(R.id.widget_status, getStatusText(occupancyClass))
        views.setInt(R.id.widget_status, "setBackgroundColor", getStatusColor(occupancyClass))
    } else {
        views.setTextViewText(R.id.widget_occupancy_percent, "--")
        views.setTextViewText(R.id.widget_status, "Closed")
        views.setInt(R.id.widget_status, "setBackgroundColor", 0xFF666666.toInt())
    }
    
    // Set last update time
    val timeFmt = DateTimeFormatter.ofPattern("h:mm a")
    views.setTextViewText(R.id.widget_last_update, "Updated: ${now.format(timeFmt)}")

    // Create intent to open the app when widget is tapped
    val intent = Intent(context, MainActivity::class.java)
    val pendingIntent = PendingIntent.getActivity(
        context,
        0,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

    // Update the widget
    appWidgetManager.updateAppWidget(appWidgetId, views)
}

private fun getCurrentOccupancy(
    predictor: TimeOnlyPredictor,
    time: ZonedDateTime
): Triple<Boolean, Int?, OccClass> {
    val OPEN_HOUR = 6
    val CLOSE_HOUR = 19
    
    // Check if campus is open
    if (time.dayOfWeek == DayOfWeek.SUNDAY) {
        return Triple(false, null, OccClass.Closed)
    }
    
    val hour = time.toLocalTime().hour
    if (hour < OPEN_HOUR || hour >= CLOSE_HOUR) {
        return Triple(false, null, OccClass.Closed)
    }
    
    // Get prediction
    val weekdayIdx = when (time.dayOfWeek) {
        DayOfWeek.MONDAY -> 0
        DayOfWeek.TUESDAY -> 1
        DayOfWeek.WEDNESDAY -> 2
        DayOfWeek.THURSDAY -> 3
        DayOfWeek.FRIDAY -> 4
        DayOfWeek.SATURDAY -> 5
        else -> return Triple(false, null, OccClass.Closed)
    }
    
    val result = predictor.predict(weekdayIdx, time.hour, time.minute)
    val percent = result.percent.coerceIn(0f, 100f).toInt()
    
    val occupancyClass = when {
        percent < 40 -> OccClass.Low
        percent < 70 -> OccClass.Moderate
        else -> OccClass.High
    }
    
    return Triple(true, percent, occupancyClass)
}

private fun getStatusText(occClass: OccClass): String {
    return when (occClass) {
        OccClass.Low -> "Low"
        OccClass.Moderate -> "Moderate"
        OccClass.High -> "High"
        OccClass.Closed -> "Closed"
    }
}

private fun getStatusColor(occClass: OccClass): Int {
    return when (occClass) {
        OccClass.Low -> 0xFF4CAF50.toInt()      // Green
        OccClass.Moderate -> 0xFFFF9800.toInt() // Orange
        OccClass.High -> 0xFFF44336.toInt()     // Red
        OccClass.Closed -> 0xFF666666.toInt()   // Gray
    }
}
