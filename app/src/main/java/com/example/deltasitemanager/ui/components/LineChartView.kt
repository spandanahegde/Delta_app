package com.example.deltasitemanager.ui.components

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
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import android.util.Log

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

    // Get the current time (hours and minutes)
    val currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())

    // Dynamically create time labels based on available data
    val timeLabels = graphData.mapNotNull { item ->
        runCatching {
            inputFormat.parse(item.evtime) // Try parsing the date string
        }.getOrNull()?.let {
            outputFormat.format(it) // Format it to "HH:mm"
        }
    }.distinct()

    // Filter the graph data to show points only up to the current time
    val adjustedGraphData = graphData.mapIndexedNotNull { index, item ->
        val formattedTime = runCatching {
            inputFormat.parse(item.evtime) // Try parsing the date string
        }.getOrNull()?.let {
            outputFormat.format(it) // Format it to "HH:mm"
        }

        // Log the evtime value if parsing fails
        if (formattedTime == null) {
            Log.e("LineChartView", "Invalid date format: ${item.evtime}")
        }

        // Filter data based on whether the formatted time is less than or equal to the current time
        if (formattedTime != null && formattedTime <= currentTime) {
            Entry(index.toFloat(), String.format("%.2f", powerSelector(item)).toFloat())
        } else {
            null
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        // Only display the label if it's not empty
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

                        // Set granularity to ensure spacing between labels
                        granularity = 1f // Minimum interval between values (avoid overlapping)

                        // Adjust label count based on number of time labels
                        val maxLabels = 5 // Limit to a maximum of 5 labels
                        labelCount = if (timeLabels.size > maxLabels) maxLabels else timeLabels.size

                        labelRotationAngle = -45f
                        textColor = Color.White.toArgb()
                    }

                    axisLeft.apply {
                        axisMinimum = 0f
                        setDrawGridLines(true)
                        axisLineColor = Color.White.toArgb()
                        textColor = Color.White.toArgb()
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
