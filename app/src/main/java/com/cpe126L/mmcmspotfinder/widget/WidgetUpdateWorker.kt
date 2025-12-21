package com.cpe126L.mmcmspotfinder.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class WidgetUpdateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            // Get all widget IDs for this app
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val widgetComponent = ComponentName(context, OccupancyWidget::class.java)
            val widgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

            // Update all widgets
            for (widgetId in widgetIds) {
                updateAppWidget(context, appWidgetManager, widgetId)
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
