package com.cpe126L.mmcmspotfinder.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.cpe126L.mmcmspotfinder.ml.TimeOnlyPredictor

@Composable
fun ForecastScreen() {
    val context = LocalContext.current

    val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var weekdayIdx by remember { mutableStateOf(0) } // 0..5
    var hourText by remember { mutableStateOf("7") }     // 1..12
    var minuteText by remember { mutableStateOf("50") }  // 0..59
    var isAm by remember { mutableStateOf(true) }

    var output by remember { mutableStateOf<String?>(null) }
    var inputError by remember { mutableStateOf<String?>(null) }

    val predictor = remember { TimeOnlyPredictor(context) }
    DisposableEffect(Unit) { onDispose { predictor.close() } }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Forecast", style = MaterialTheme.typography.titleLarge)

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            weekdays.forEachIndexed { idx, label ->
                FilterChip(
                    selected = weekdayIdx == idx,
                    onClick = { weekdayIdx = idx },
                    label = { Text(label) }
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = hourText,
                onValueChange = { v ->
                    if (v.length <= 2 && v.all { it.isDigit() }) hourText = v
                },
                label = { Text("Hour (1–12)") },
                modifier = Modifier.width(140.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            OutlinedTextField(
                value = minuteText,
                onValueChange = { v ->
                    if (v.length <= 2 && v.all { it.isDigit() }) minuteText = v
                },
                label = { Text("Minute (0–59)") },
                modifier = Modifier.width(160.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            FilterChip(selected = isAm, onClick = { isAm = true }, label = { Text("AM") })
            FilterChip(selected = !isAm, onClick = { isAm = false }, label = { Text("PM") })
        }

        Button(onClick = {
            inputError = null
            output = null

            val hour = hourText.toIntOrNull()
            val minute = minuteText.toIntOrNull()
            if (hour == null || hour !in 1..12) { inputError = "Hour must be 1–12"; return@Button }
            if (minute == null || minute !in 0..59) { inputError = "Minute must be 0–59"; return@Button }
            val wd = weekdayIdx.coerceIn(0, 5)

            try {
                val pred = predictor.predict12h(weekday = wd, hour12 = hour, minute = minute, isAm = isAm)
                val pct = "%.1f".format(pred.percent)
                val err = "%.2f".format(pred.errorMaePp)
                output = "Predicted: $pct%  |  Error: $err pp"
            } catch (t: Throwable) {
                inputError = t.message ?: "Prediction error"
            }
        }) { Text("Predict") }

        Divider()

        if (inputError != null) {
            Text(inputError!!, color = MaterialTheme.colorScheme.error)
        }
        Text(output ?: "—", style = MaterialTheme.typography.bodyLarge)
    }
}