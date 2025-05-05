package com.example.deltasitemanager.ui.components

import android.graphics.Color
import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.example.deltasitemanager.models.GraphDataItem
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.unit.dp
import com.example.deltasitemanager.ui.components.TimeMarkerView

@Composable
fun LineChartView(
    graphData: List<GraphDataItem>,
    powerSelector: (GraphDataItem) -> Float,
    lineColor: Int,
    modifier: Modifier = Modifier
) {
    val timeLabels = graphData.map {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.evtime) ?: Date()
        )
    }

    AndroidView(
        factory = { context ->
            LineChart(context).apply {
                val entries = graphData.mapIndexed { index, item ->
                    Entry(index.toFloat(), powerSelector(item))
                }

                val dataSet = LineDataSet(entries, "").apply {
                    color = lineColor
                    lineWidth = 2f
                    setDrawCircles(false)
//                    setCircleColor(lineColor)
                    setDrawCircleHole(false)
                    setDrawValues(false)
                }

                data = LineData(dataSet)

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    valueFormatter = IndexAxisValueFormatter(timeLabels)
                    setDrawGridLines(false)
                    labelRotationAngle = -45f
                    granularity = 1f
                    labelCount = 5
                }

                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(true)
                    axisLineColor = Color.DKGRAY
                }

                axisRight.isEnabled = false
                marker = TimeMarkerView(context, timeLabels)
                description = Description().apply { text = "" }
                invalidate()
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
    )
}
