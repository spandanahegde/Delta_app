package com.example.deltasitemanager.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.deltasitemanager.models.GraphDataItem
import com.example.deltasitemanager.ui.components.LineChartView
import com.example.deltasitemanager.viewmodel.GraphViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(viewModel: GraphViewModel, macId: String, navController: NavController) {
    val graphData by viewModel.graphData.collectAsState()
    val today = remember { viewModel.getTodayDate() }

    LaunchedEffect(macId, today) {
        viewModel.fetchGraphData(macId, today)
    }

    // Define colors
    val graphColors = mapOf(
        "Grid Power (kW)" to Color(0xFF073E9D),
        "PCS Power (kW)" to Color(0xFFE10E0E),
        "Load Power (kW)" to Color(0xFFE0A006),
        "DG Power (kW)" to Color(0xFF23E52B),
        "PVI Total Power (kW)" to Color(0xFF00BCD4)
    )

    // Define selectors
    val graphConfigs = listOf<Pair<String, (GraphDataItem) -> Float>>(
        "Grid Power (kW)" to { it.GRID_Active_Power_RYB },
        "PCS Power (kW)" to { it.PCS_ActivePower.toFloat() },
        "Load Power (kW)" to { it.Load_Active_Power },
        "DG Power (kW)" to { it.DG1_Active_Power_RYB }
    ).toMutableList()

    val hasPviData = remember(graphData) { graphData.any { it.PVI_Total_Active_Power != 0f } }
    if (hasPviData) {
        graphConfigs.add("PVI Total Power (kW)" to { it.PVI_Total_Active_Power })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daily Cumulative Record") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF435385),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Date: $today", style = MaterialTheme.typography.bodyLarge, color = Color.LightGray)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Graph points: ${graphData.size}", color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))

            if (graphData.isNotEmpty()) {
                graphConfigs.forEach { (label, selector) ->
                    PowerGraph(label, graphData, selector, graphColors[label] ?: Color.Cyan)
                }
            } else {
                Text("No data available", color = Color.Gray)
            }
        }
    }
}

@Composable
fun PowerGraph(
    title: String,
    data: List<GraphDataItem>,
    selector: (GraphDataItem) -> Float,
    color: Color
) {
    val headerStyle = MaterialTheme.typography.titleMedium
    Text(title, style = headerStyle, color = Color.White)
    LineChartView(
        graphData = data,
        powerSelector = selector,
        lineColor = color.toArgb()
    )
    Spacer(modifier = Modifier.height(16.dp))
}
