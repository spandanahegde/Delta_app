package com.example.deltasitemanager.ui.components

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.deltasitemanager.models.GraphDataItem
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

// Formatter that only shows selected time points
class TimeAxisFormatter(private val labels: List<String>) : ValueFormatter() {
    private val showOnly = listOf("0:00", "12:00", "18:00", "24:00")
    override fun getFormattedValue(value: Float): String {
        val index = value.toInt()
        return labels.getOrNull(index)?.takeIf { it in showOnly } ?: ""
    }
}

@Composable
fun LineChartView(
    graphData: List<GraphDataItem>,
    powerSelector: (GraphDataItem) -> Float,
    lineColor: Int,
    label: String = "",
    labelColor: Color = Color.Transparent,
    modifier: Modifier = Modifier
) {

    if (graphData.isEmpty()) return

    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    // Extract and format unique time labels
    val timeLabels = graphData.mapNotNull { item ->
        runCatching {
            inputFormat.parse(item.evtime)
        }.getOrNull()?.let {
            outputFormat.format(it)
        }
    }.distinct()

    // Adjusted graph data: only show values up to current time
    val adjustedGraphData = graphData.mapIndexedNotNull { index, item ->
        val formattedTime = runCatching {
            inputFormat.parse(item.evtime)
        }.getOrNull()?.let {
            outputFormat.format(it)
        }

        if (formattedTime == null) {
            Log.e("LineChartView", "Invalid date format: ${item.evtime}")
        }

        if (formattedTime != null && formattedTime <= currentTime) {
            val rawValue = powerSelector(item)
            val displayValue = if (kotlin.math.abs(rawValue) < 0.0001f) 0f else rawValue
            Entry(index.toFloat(), displayValue)

        } else {
            null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = labelColor
            )
        }

        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    val dataSet = LineDataSet(adjustedGraphData, label).apply {
                        color = lineColor
                        lineWidth = 2f
                        setDrawCircles(false)
                        setDrawValues(false)
                        setDrawCircleHole(false)
                        mode = LineDataSet.Mode.CUBIC_BEZIER
                    }

                    data = LineData(dataSet)

                    xAxis.apply {
                        position = XAxis.XAxisPosition.BOTTOM
                        valueFormatter = IndexAxisValueFormatter(timeLabels)
                        setDrawGridLines(false)
                        granularity = 1f
                        labelCount = if (timeLabels.size > 5) 5 else timeLabels.size
                        labelRotationAngle = -45f
                        textColor = Color.White.toArgb()
                    }

                    axisLeft.apply {
                        // ✅ Removed axisMinimum = 0f to allow negative values
                        setDrawGridLines(true)
                        axisLineColor = Color.White.toArgb()
                        textColor = Color.White.toArgb()
                        isGranularityEnabled = true
                        granularity = 1f
                    }

                    axisRight.isEnabled = false

                    description = Description().apply {
                        text = ""
                        isEnabled = false
                    }

                    marker = TimeMarkerView(context, timeLabels)
                    invalidate()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}
