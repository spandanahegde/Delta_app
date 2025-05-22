package com.example.deltasitemanager.ui.components

import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.example.deltasitemanager.R

class TimeMarkerView(context: Context, private val timeLabels: List<String>) :
    MarkerView(context, R.layout.marker_view) {

    private val textView: TextView = findViewById(R.id.marker_text)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e == null) return
        val index = e.x.toInt()
        val time = timeLabels.getOrNull(index) ?: "Time"
        val value = e.y
        textView.text = "$time\n$value kW"
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2f), -height.toFloat())
    }
}
