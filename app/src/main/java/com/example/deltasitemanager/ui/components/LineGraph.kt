package com.example.deltasitemanager.ui.components

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet

@Composable
fun LineGraph(title: String, entries: List<Entry>, lineColor: Int = Color.BLUE) {
    AndroidView(factory = { context ->
        LineChart(context).apply {
            // Enable/disable chart description
            description.isEnabled = true
            description.text = title
            description.textColor = Color.BLACK
            description.textSize = 12f

            // Interactivity
            setTouchEnabled(true)
            setPinchZoom(true)

            // Hide right axis
            axisRight.isEnabled = false
            legend.isEnabled = false

            // X-Axis Configuration
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.granularity = 1f

            // Y-Axis Configuration (Optional)
            axisLeft.setDrawGridLines(true)
            axisLeft.setDrawLabels(true)
            axisLeft.axisMinimum = 0f
        }
    }, update = { chart ->
        val dataSet = LineDataSet(entries, title).apply {
            color = lineColor
            setDrawCircles(false) // You can set this to true to show data points as circles
            setDrawValues(false) // Hide data values on the graph
            lineWidth = 2f
        }

        chart.data = LineData(dataSet)
        chart.invalidate() // Refresh the chart to update the data
    })
}
