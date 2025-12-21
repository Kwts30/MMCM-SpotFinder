package com.cpe126L.mmcmspotfinder.util

import com.cpe126L.mmcmspotfinder.viewmodel.OccClass

const val OPEN_HOUR = 6
const val CLOSE_HOUR = 19

fun classifyOccupancy(percent: Int?): OccClass {
    val v = percent ?: return OccClass.Closed
    return when {
        v < 40 -> OccClass.Low
        v < 70 -> OccClass.Moderate
        else -> OccClass.High
    }
}
