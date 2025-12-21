# MMCM SpotFinder Home Screen Widget

## Overview
The MMCM SpotFinder widget provides at-a-glance campus occupancy information directly on your Android home screen, allowing users to check current campus traffic conditions without opening the app.

## Features
- **Real-time Occupancy Display**: Shows current campus occupancy percentage
- **Visual Status Indicator**: Color-coded status badge (Low/Moderate/High/Closed)
- **Last Update Timestamp**: Displays when the data was last refreshed
- **Quick Access**: Tap the widget to open the full application
- **Automatic Updates**: Refreshes every 15 minutes using WorkManager

## Widget Components

### Layout
The widget displays:
- **Title**: "MMCM Occupancy"
- **Occupancy Percentage**: Large, bold percentage (e.g., "45%")
- **Status Badge**: Color-coded status indicator
  - Green: Low occupancy (< 40%)
  - Orange: Moderate occupancy (40-69%)
  - Red: High occupancy (≥ 70%)
  - Gray: Campus closed
- **Update Time**: Small timestamp showing last refresh time

### Technical Implementation

#### Files
- `OccupancyWidget.kt`: Main widget provider class
- `WidgetUpdateWorker.kt`: WorkManager worker for periodic updates
- `OccupancyUtils.kt`: Shared utilities for occupancy classification
- `widget_occupancy.xml`: Widget layout
- `widget_info.xml`: Widget configuration
- `widget_background.xml`: Widget background drawable
- `widget_preview.xml`: Widget preview image

#### Update Mechanism
The widget uses WorkManager for reliable background updates:
- **Update Interval**: 15 minutes (minimum allowed by WorkManager for periodic work)
- **Reliability**: WorkManager ensures updates occur even if app is closed
- **Battery Optimization**: Updates are deferred during Doze mode and app standby

#### Data Flow
1. WorkManager triggers `WidgetUpdateWorker` every 15 minutes
2. Worker calls `updateAppWidget()` for each widget instance
3. Function creates a `TimeOnlyPredictor` instance
4. Predictor generates occupancy prediction based on current time
5. Widget UI is updated with new data via `RemoteViews`
6. Predictor resources are properly released

## Adding the Widget

### For Users
1. Long-press on your Android home screen
2. Tap "Widgets"
3. Find "MMCM SpotFinder" in the widget list
4. Drag the "MMCM Occupancy" widget to your desired location
5. The widget will automatically start updating

### For Developers
The widget is automatically registered in `AndroidManifest.xml`:
```xml
<receiver
    android:name=".widget.OccupancyWidget"
    android:exported="true">
    <intent-filter>
        <action android:name="android.appwidget.action.APPWIDGET_UPDATE" />
    </intent-filter>
    <meta-data
        android:name="android.appwidget.provider"
        android:resource="@xml/widget_info" />
</receiver>
```

## Configuration

### Widget Sizing
- **Minimum Width**: 180dp (approximately 2x2 grid cells)
- **Minimum Height**: 180dp
- **Resizable**: Both horizontal and vertical

### Dependencies
```kotlin
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

## Campus Hours
The widget displays occupancy predictions only during campus operating hours:
- **Open**: Monday-Saturday, 6:00 AM - 7:00 PM
- **Closed**: Sundays and outside operating hours
- When closed, the widget displays "--" for percentage and "Closed" status

## Occupancy Classification
Occupancy levels are classified as follows:
- **Low**: 0-39% occupancy (Green)
- **Moderate**: 40-69% occupancy (Orange)
- **High**: 70-100% occupancy (Red)
- **Closed**: Campus is closed (Gray)

## Architecture Notes

### Resource Management
- Each widget update creates a new `TimeOnlyPredictor` instance
- This is necessary because widgets run in a separate process
- Predictor resources are properly released using try-finally
- TensorFlow Lite handles internal model caching efficiently

### Shared Logic
- Occupancy classification logic is shared between the app and widget
- Constants (OPEN_HOUR, CLOSE_HOUR) are centralized in `OccupancyUtils`
- This ensures consistency across the application

## Testing
To test the widget:
1. Build and install the app
2. Add the widget to home screen
3. Verify initial display shows current occupancy or "Closed"
4. Wait 15 minutes to confirm automatic update
5. Tap widget to verify it opens the main app
6. Remove and re-add widget to test initialization

## Troubleshooting

### Widget Not Updating
- Check that the app has permission to run in the background
- Verify WorkManager is not being restricted by battery optimization
- Check system settings for widget refresh settings

### Widget Shows Old Data
- The widget updates every 15 minutes
- Manual refresh is not available; tap to open the app for real-time data

### Widget Shows "--"
- This is expected when campus is closed
- Verify current day/time against campus operating hours
