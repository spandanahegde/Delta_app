package com.example.deltasitemanager.ui.screens
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import com.example.deltasitemanager.ui.components.LineChartView
import com.example.deltasitemanager.viewmodel.GraphViewModel
import androidx.navigation.NavController
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(viewModel: GraphViewModel, macId: String,navController: NavController ) {
    val graphData by viewModel.graphData.collectAsState()

    // Trigger fetch once on composition
    LaunchedEffect(Unit) {
        val today = viewModel.getTodayDate()
        viewModel.fetchGraphData(macId, today)
    }

    // Colors
    val SoftBlue = Color(0xFF073E9D).toArgb()
    val SoftGreen = Color(0xFF23E52B).toArgb()
    val SoftMagenta = Color(0xFFE10E0E).toArgb()
    val SoftRed = Color(0xFFE0A006).toArgb()

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
            Log.d("GraphScreen", "Graph data size: ${graphData.size}")
            Log.d("GraphScreen", "Graph data: $graphData")
//
//            Text("Power Graphs", style = MaterialTheme.typography.titleLarge)
            Text("Graph points: ${graphData.size}", color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            if (graphData.isNotEmpty()) {
                Text("Grid Power (kW)", style = MaterialTheme.typography.titleMedium)
                LineChartView(
                    graphData = graphData,
                    powerSelector = { it.GRID_Active_Power_RYB },
                    lineColor = SoftBlue
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("PCS Power (kW)", style = MaterialTheme.typography.titleMedium)
                LineChartView(
                    graphData = graphData,
                    powerSelector = { it.PCS_ActivePower.toFloat() },
                    lineColor = SoftGreen
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("Load Power (kW)", style = MaterialTheme.typography.titleMedium)
                LineChartView(
                    graphData = graphData,
                    powerSelector = { it.Load_Active_Power },
                    lineColor = SoftMagenta
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text("DG1 Power (kW)", style = MaterialTheme.typography.titleMedium)
                LineChartView(
                    graphData = graphData,
                    powerSelector = { it.DG1_Active_Power_RYB },
                    lineColor = SoftRed
                )
            } else {
                Text("No data available", color = Color.Gray)
            }
        }
    }
}
