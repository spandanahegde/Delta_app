package com.example.deltasitemanager.ui.components

import android.util.Log
import androidx.compose.foundation.layout.*
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
import java.text.SimpleDateFormat
import java.util.*

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

    // Define time formats
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val currentTime = outputFormat.format(Date())

    // Extract time labels from graph data
    val timeLabels = graphData.mapNotNull {
        runCatching { inputFormat.parse(it.evtime) }.getOrNull()?.let { outputFormat.format(it) }
    }.distinct()

    // Generate chart entries with timestamps up to current time
    val entries = graphData.mapIndexedNotNull { index, item ->
        val parsedDate = runCatching { inputFormat.parse(item.evtime) }.getOrNull()
        if (parsedDate == null) {
            Log.e("LineChartView", "Invalid date format: ${item.evtime}")
            return@mapIndexedNotNull null
        }

        val value = powerSelector(item)
        val cleanValue = if (kotlin.math.abs(value) < 0.0001f) 0f else value
        Entry(index.toFloat(), cleanValue)
    }

    // Main chart container
    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Optional chart title
        if (label.isNotEmpty()) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = labelColor
            )
        }

        // AndroidView to embed MPAndroidChart LineChart
        AndroidView(
            factory = { context ->
                LineChart(context).apply {
                    setupChart(this, entries, timeLabels, lineColor, label)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )
    }
}

/**
 * Configures the LineChart appearance, data, and interaction.
 */
private fun setupChart(
    chart: LineChart,
    entries: List<Entry>,
    timeLabels: List<String>,
    lineColor: Int,
    label: String
) {
    val dataSet = LineDataSet(entries, label).apply {
        color = lineColor
        lineWidth = 2f
        setDrawCircles(false)
        setDrawValues(false)
        setDrawCircleHole(false)
        mode = LineDataSet.Mode.CUBIC_BEZIER
    }

    chart.data = LineData(dataSet)

    chart.xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        valueFormatter = IndexAxisValueFormatter(timeLabels)
        setDrawGridLines(false)
        granularity = 1f
        labelCount = minOf(timeLabels.size, 5)
        labelRotationAngle = -45f
        textColor = Color.White.toArgb()
    }

    chart.axisLeft.apply {
        setDrawGridLines(true)
        axisLineColor = Color.White.toArgb()
        textColor = Color.White.toArgb()
        isGranularityEnabled = true
        granularity = 1f
    }

    chart.axisRight.isEnabled = false

    chart.description = Description().apply {
        text = ""
        isEnabled = false
    }

    chart.marker = TimeMarkerView(chart.context, timeLabels)
    chart.invalidate()
}
