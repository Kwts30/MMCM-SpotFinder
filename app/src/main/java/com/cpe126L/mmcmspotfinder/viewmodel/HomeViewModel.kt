package com.cpe126L.mmcmspotfinder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.cpe126L.mmcmspotfinder.ml.TimeOnlyPredictor
import com.cpe126L.mmcmspotfinder.util.CLOSE_HOUR
import com.cpe126L.mmcmspotfinder.util.OPEN_HOUR
import com.cpe126L.mmcmspotfinder.util.classifyOccupancy
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.*

private const val FREQ_MIN = 10

enum class OccClass { Low, Moderate, High, Closed }

data class HomeUiState(
    val isOpen: Boolean = false,
    val slotTime: ZonedDateTime = ZonedDateTime.now(),
    val currentPercent: Int? = null, // null when closed
    val currentClass: OccClass = OccClass.Closed,
    val nextHourClass: OccClass = OccClass.Closed,
    val nextHourAvgPercent: Int? = null,
    val peakHourLabel: String = "--:--",
    val openCloseMode: OpenCloseMode = OpenCloseMode.BeforeOpen,
    val recommendationText: String = "Campus closed. Predictions resume at 6:00 AM.",
)

enum class OpenCloseMode { BeforeOpen, DuringOpen, AfterClose }

class HomeViewModel(
    private val predictor: TimeOnlyPredictor,
    private val zone: ZoneId
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state

    private var tickerJob: Job? = null
    private var lastPeakDay: LocalDate? = null

    init {
        startTicker()
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = viewModelScope.launch {
            while (true) {
                val now = ZonedDateTime.now(zone)
                val floored = floorToSlot(now, FREQ_MIN)
                recomputeAll(floored)
                val next = floored.plusMinutes(FREQ_MIN.toLong())
                val waitMs = Duration.between(ZonedDateTime.now(zone), next).toMillis().coerceAtLeast(0)
                delay(waitMs)
            }
        }
    }

    private fun recomputeAll(slot: ZonedDateTime) {
        val (open, mode) = resolveOpenClose(slot)
        val currentPercent = if (open) predictPercent(slot) else null
        val currentClass = classifyOccupancy(currentPercent)

        val (nextHourAvg, nextHourClass) = if (open) {
            val avg = averageNextHour(slot)
            avg to classifyOccupancy(avg)
        } else (null to OccClass.Closed)

        val peakLabel = computeOrGetPeakHour(slot)
        val recommendation = buildRecommendation(currentClass)

        _state.value = HomeUiState(
            isOpen = open,
            slotTime = slot,
            currentPercent = currentPercent,
            currentClass = currentClass,
            nextHourClass = nextHourClass,
            nextHourAvgPercent = nextHourAvg,
            peakHourLabel = peakLabel,
            openCloseMode = mode,
            recommendationText = recommendation
        )
    }

    private fun resolveOpenClose(t: ZonedDateTime): Pair<Boolean, OpenCloseMode> {
        if (t.dayOfWeek == DayOfWeek.SUNDAY) {
            return false to if (t.toLocalTime().hour < OPEN_HOUR) OpenCloseMode.BeforeOpen else OpenCloseMode.AfterClose
        }
        val lt = t.toLocalTime()
        return when {
            lt.hour < OPEN_HOUR -> false to OpenCloseMode.BeforeOpen
            lt.hour >= CLOSE_HOUR -> false to OpenCloseMode.AfterClose
            else -> true to OpenCloseMode.DuringOpen
        }
    }

    private fun predictPercent(t: ZonedDateTime): Int {
        val weekdayIdx = when (t.dayOfWeek) {
            DayOfWeek.MONDAY -> 0
            DayOfWeek.TUESDAY -> 1
            DayOfWeek.WEDNESDAY -> 2
            DayOfWeek.THURSDAY -> 3
            DayOfWeek.FRIDAY -> 4
            DayOfWeek.SATURDAY -> 5
            DayOfWeek.SUNDAY -> return 0
        }
        val res = predictor.predict(weekdayIdx, t.hour, t.minute)
        return res.percent.coerceIn(0f, 100f).toInt()
    }

    private fun averageNextHour(nowSlot: ZonedDateTime): Int? {
        val slots = mutableListOf<Int>()
        var t = nowSlot.plusMinutes(FREQ_MIN.toLong()) // start at next slot
        val end = nowSlot.plusHours(1)
        while (!t.isAfter(end)) {
            if (resolveOpenClose(t).first) slots.add(predictPercent(t))
            t = t.plusMinutes(FREQ_MIN.toLong())
        }
        return if (slots.isEmpty()) null else slots.average().toInt()
    }

    // Includes 6:00 PM (18:00) as candidate and prefers later hour on ties
    private fun computeOrGetPeakHour(now: ZonedDateTime): String {
        val today = now.toLocalDate()
        if (lastPeakDay == today) return _state.value.peakHourLabel

        val start = today.atTime(LocalTime.of(OPEN_HOUR, 0)).atZone(zone)      // 06:00
        val end = today.atTime(LocalTime.of(CLOSE_HOUR, 0)).atZone(zone)       // 19:00 (exclusive)

        val byHour = mutableMapOf<Int, MutableList<Int>>()
        var t = start
        while (t.isBefore(end)) {
            val p = predictPercent(t)
            byHour.getOrPut(t.hour) { mutableListOf() }.add(p)
            t = t.plusMinutes(FREQ_MIN.toLong())
        }

        var bestHour = -1
        var bestAvg = -1.0
        byHour.forEach { (hour, values) ->
            val avg = if (values.isEmpty()) 0.0 else values.map { it.toDouble() }.average()
            if (avg > bestAvg + 1e-6 || (kotlin.math.abs(avg - bestAvg) < 0.05 && hour > bestHour)) {
                bestAvg = avg
                bestHour = hour
            }
        }

        lastPeakDay = today
        return if (bestHour >= 0) formatHour(bestHour) else "--:--"
    }

    private fun buildRecommendation(c: OccClass): String = when (c) {
        OccClass.Low -> "Low Occupancy predicted at this Hour. It’s a good time to go."
        OccClass.Moderate -> "Moderate Occupancy. Expect some congestion; consider arriving a bit earlier."
        OccClass.High -> "High Occupancy predicted. Consider adjusting your schedule or transport."
        OccClass.Closed -> "Campus closed. Predictions resume at 6:00 AM."
    }

    private fun floorToSlot(t: ZonedDateTime, freqMin: Int): ZonedDateTime {
        val m = (t.minute / freqMin) * freqMin
        return t.withSecond(0).withNano(0).withMinute(m)
    }

    private fun formatHour(hour24: Int): String {
        val t = LocalTime.of(hour24, 0)
        val fmt = java.time.format.DateTimeFormatter.ofPattern("h:00 a")
        return t.format(fmt)
    }

    companion object {
        fun provideFactory(predictor: TimeOnlyPredictor, zone: ZoneId): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HomeViewModel(predictor, zone) as T
                }
            }
    }
}