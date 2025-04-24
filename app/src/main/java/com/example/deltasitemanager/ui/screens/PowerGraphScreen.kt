package com.example.deltasitemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.deltasitemanager.viewmodel.AuthViewModel
import com.example.deltasitemanager.viewmodel.GraphViewModel
import com.example.deltasitemanager.viewmodel.GraphViewModelFactory
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items


@Composable
fun PowerGraphScreen(navController: NavController, authViewModel: AuthViewModel) {
    val graphViewModel: GraphViewModel = viewModel(factory = GraphViewModelFactory(authViewModel))

    val pcsActivePower by graphViewModel.pcsData.collectAsState(emptyList())
    val gridActivePowerRYB by graphViewModel.gridData.collectAsState(emptyList())
    val loadActivePower by graphViewModel.loadData.collectAsState(emptyList())
    val dg1ActivePowerRYB by graphViewModel.dgData.collectAsState(emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Power Graphs",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = Color.White,
                elevation = 8.dp
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            item {
                PowerGraph(
                    title = "PCS Active Power (kW)",
                    entries = pcsActivePower,
                    lineColor = Color.Blue
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PowerGraph(
                    title = "GRID Active Power RYB (kW)",
                    entries = gridActivePowerRYB,
                    lineColor = Color.Green
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PowerGraph(
                    title = "Load Active Power (kW)",
                    entries = loadActivePower,
                    lineColor = Color.Red
                )
            }
            item {
                Spacer(modifier = Modifier.height(16.dp))
                PowerGraph(
                    title = "DG1 Active Power RYB (kW)",
                    entries = dg1ActivePowerRYB,
                    lineColor = Color.Cyan
                )
            }
        }
    }
}


@Composable
fun PowerGraph(title: String, entries: List<Entry>, lineColor: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
    ) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                com.github.mikephil.charting.charts.LineChart(context).apply {
                    description = Description().apply {
                        text = title
                        textColor = Color.DarkGray.toArgb()
                    }

                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f
                    xAxis.valueFormatter = object : ValueFormatter() {
                        private val format = SimpleDateFormat("HH:mm", Locale.getDefault())
                        override fun getFormattedValue(value: Float): String {
                            return format.format(Date(value.toLong()))
                        }
                    }

                    axisLeft.setDrawGridLines(true)
                    axisLeft.axisMinimum = 0f
                    axisLeft.valueFormatter = object : ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            return value.toInt().toString() // Only number
                        }
                    }

                    axisRight.isEnabled = false
                    legend.isEnabled = false
                    setScaleEnabled(true)
                    setDrawBorders(false)
                    setBackgroundColor(Color(0xFFF1F1F1).toArgb())
                }
            },
            update = { chart ->
                val dataSet = LineDataSet(entries, "").apply {
                    color = lineColor.toArgb()
                    valueTextColor = Color.Black.toArgb()
                    valueTextSize = 9f
                    setDrawValues(true)
                    lineWidth = 2.5f
                    setDrawCircles(false)
                    mode = LineDataSet.Mode.LINEAR // Clear linear, shows tiny changes
                    setDrawFilled(true)
                    fillColor = lineColor.toArgb()
                    fillAlpha = 60
                }
                chart.data = LineData(dataSet)
                chart.invalidate()
            }
        )
    }
}
