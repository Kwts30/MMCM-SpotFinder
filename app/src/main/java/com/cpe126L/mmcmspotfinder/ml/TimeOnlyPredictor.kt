package com.cpe126L.mmcmspotfinder.ml

import android.content.Context
import android.content.res.AssetManager
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TimeOnlyPredictor(
    context: Context,
    modelAssetPath: String = "parking_timeonly.tflite",
    specAssetPath: String = "feature_spec_timeonly.json"
) : AutoCloseable {

    data class Prediction(
        val percent: Float,         // 0..100
        val errorMaePp: Float,      // fixed MAE in percentage points (from spec)
        val errorBand90Pp: Float?   // optional 90% error band (from spec)
    )

    private val interpreter: Interpreter
    private val featureOrder: List<String>
    private val fixedErrorMaePp: Float
    private val fixedErrorBand90Pp: Float?

    init {
        interpreter = Interpreter(loadModelFile(context.assets, modelAssetPath), Interpreter.Options().apply {
            setUseXNNPACK(true)
        })
        val spec = context.assets.open(specAssetPath).use { it.readBytes().decodeToString() }
        val obj = JSONObject(spec)
        val arr = obj.getJSONArray("numeric_feature_order")
        featureOrder = buildList { for (i in 0 until arr.length()) add(arr.getString(i)) }
        fixedErrorMaePp = obj.optDouble("fixed_error_mae_pp", Double.NaN).toFloat()
        fixedErrorBand90Pp = obj.optDouble("fixed_error_band90_pp", Double.NaN).let { if (it.isNaN()) null else it.toFloat() }
    }

    override fun close() { interpreter.close() }

    // weekday: 0=Mon..5=Sat; hour24: 0..23; minute: 0..59
    fun predict(weekday: Int, hour24: Int, minute: Int): Prediction {
        require(weekday in 0..5) { "weekday must be 0..5 (Mon..Sat)" }
        require(hour24 in 0..23 && minute in 0..59) { "invalid time" }
        val x = featuresFrom(weekday, hour24, minute)
        val out = Array(1) { FloatArray(1) }
        interpreter.run(x, out)
        val percent = (out[0][0].coerceIn(0f, 1f) * 100f)
        return Prediction(percent = percent, errorMaePp = fixedErrorMaePp, errorBand90Pp = fixedErrorBand90Pp)
    }

    // Optional helper for AM/PM UI
    fun predict12h(weekday: Int, hour12: Int, minute: Int, isAm: Boolean): Prediction {
        require(hour12 in 1..12) { "hour12 must be 1..12" }
        val hour24 = if (isAm) (if (hour12 == 12) 0 else hour12) else (if (hour12 == 12) 12 else hour12 + 12)
        return predict(weekday, hour24, minute)
    }

    private fun featuresFrom(weekday: Int, hour24: Int, minute: Int): Array<FloatArray> {
        val hour = hour24 + (minute / 60f)
        val hourSin = sin(2f * PI.toFloat() * hour / 24f)
        val hourCos = cos(2f * PI.toFloat() * hour / 24f)
        val isAm = if (hour24 < 12) 1f else 0f
        val dow = FloatArray(6) { i -> if (i == weekday) 1f else 0f }

        val values = mapOf(
            "hour_sin" to hourSin,
            "hour_cos" to hourCos,
            "is_am" to isAm,
            "dow_0" to dow[0],
            "dow_1" to dow[1],
            "dow_2" to dow[2],
            "dow_3" to dow[3],
            "dow_4" to dow[4],
            "dow_5" to dow[5]
        )
        val x = FloatArray(featureOrder.size) { idx -> values[featureOrder[idx]] ?: 0f }
        return arrayOf(x)
    }

    private fun loadModelFile(assets: AssetManager, path: String): MappedByteBuffer {
        val fd = assets.openFd(path)
        FileInputStream(fd.fileDescriptor).use { fis ->
            return fis.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.length)
        }
    }
}