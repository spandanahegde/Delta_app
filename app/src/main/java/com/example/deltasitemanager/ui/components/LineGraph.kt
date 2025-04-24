// File: ui.components.LineGraph.kt

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
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            axisRight.isEnabled = false
            legend.isEnabled = false
            xAxis.position = XAxis.XAxisPosition.BOTTOM
        }
    }, update = { chart ->
        val dataSet = LineDataSet(entries, title).apply {
            color = lineColor
            setDrawCircles(false)
            setDrawValues(false)
            lineWidth = 2f
        }
        chart.data = LineData(dataSet)
        chart.invalidate()
    })
}
