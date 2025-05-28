package com.example.deltasitemanager.ui.screens

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.deltasitemanager.models.GraphDataItem
import com.example.deltasitemanager.models.IndividualSiteInfo
import com.example.deltasitemanager.ui.components.LineChartView
import com.example.deltasitemanager.viewmodel.GraphViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GraphScreen(viewModel: GraphViewModel, macId: String, navController: NavController) {
    val graphData by viewModel.graphData.collectAsState()
    val siteInfo by viewModel.siteInfo.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val currentSiteInfo = siteInfo?.firstOrNull()
    val context = LocalContext.current

    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
    val calendar = remember { Calendar.getInstance() }

    var selectedDate by remember { mutableStateOf(dateFormat.format(calendar.time)) }

    LaunchedEffect(macId, selectedDate) {
        viewModel.fetchGraphData(macId, selectedDate)
        viewModel.fetchSiteInfo(macId)
    }

    val graphColors = mapOf(
        "Grid Power (kW)" to Color(0xFF1B4FE8),
        "Load Power (kW)" to Color(0xFFE0A006),
        "DG Power (kW)" to Color(0xFF23E52B),
        "ESS Output (kW)" to Color(0xFFE10E0E),
        "PVI Total Power (kW)" to Color(0xFF00BCD4)
    )

    val graphConfigs = listOf(
        "Grid Power (kW)" to { it: GraphDataItem -> it.GRID_Active_Power_RYB },
        "Load Power (kW)" to { it: GraphDataItem -> it.Load_Active_Power },
        "DG Power (kW)" to { it: GraphDataItem -> it.DG2_Active_Power_RYB },
        "ESS Output (kW)" to { it: GraphDataItem -> it.PCS_ActivePower },
        "PVI Total Power (kW)" to { it: GraphDataItem -> it.PVI_Total_Active_Power }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Daily Cumulative Record", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val dp = DatePickerDialog(
                            context,
                            { _: DatePicker, year: Int, month: Int, day: Int ->
                                calendar.set(year, month, day)
                                selectedDate = dateFormat.format(calendar.time)
                            },
                            calendar.get(Calendar.YEAR),
                            calendar.get(Calendar.MONTH),
                            calendar.get(Calendar.DAY_OF_MONTH)
                        )
                        dp.show()
                    }) {
                        Icon(Icons.Filled.CalendarToday, contentDescription = "Select Date", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF202A3D),
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
            Text("Date: $selectedDate", color = Color.White, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))

            when {
                isLoading -> LoadingPlaceholder()
                graphData.isNotEmpty() -> {
                    graphConfigs
                        .filterNot { (label, _) ->
                            // Hide PVI graph if today's total generation is zero
                            label == "PVI Total Power (kW)" && (currentSiteInfo?.PVI_Total_Gen_Today ?: 0f) == 0f
                        }
                        .forEach { (label, selector) ->
                            if (label == "PVI Total Power (kW)") {
                                // Show PVI graph only if any data point is non-zero
                                val hasNonZeroData = graphData.any { selector(it) != 0f }
                                if (hasNonZeroData) {
                                    PowerGraph(
                                        title = label,
                                        data = graphData,
                                        selector = selector,
                                        color = graphColors[label] ?: Color.Cyan,
                                        siteInfo = currentSiteInfo
                                    )
                                }
                            } else {
                                // Show all other graphs even if data is zero
                                PowerGraph(
                                    title = label,
                                    data = graphData,
                                    selector = selector,
                                    color = graphColors[label] ?: Color.Cyan,
                                    siteInfo = currentSiteInfo
                                )
                            }
                        }
                }
                else -> NoDataMessage()
            }
        }
    }
}

@Composable
fun LoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(150.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF232222)),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...", color = Color(0xFF6567D9), fontSize = 16.sp)
    }
}

@Composable
fun NoDataMessage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("No data available", color = Color.Gray, fontSize = 16.sp)
    }
}

@Composable
fun PowerGraph(
    title: String,
    data: List<GraphDataItem>,
    selector: (GraphDataItem) -> Float,
    color: Color,
    siteInfo: IndividualSiteInfo?
) {
    val (todayEnergy, cumulativeEnergy) = getEnergyData(title, siteInfo)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF141415))
            .shadow(4.dp, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            color = Color(0xFFEEEEEE),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Today: $todayEnergy",
                color = Color(0xFFB0BEC5),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Cumulative: $cumulativeEnergy",
                color = Color(0xFFDEE8EE),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LineChartView(
            graphData = data,
            powerSelector = selector,
            lineColor = color.toArgb()
        )
    }
}

@Composable
fun getEnergyData(title: String, siteInfo: IndividualSiteInfo?): Pair<String, String> {
    return when (title) {
        "PVI Total Power (kW)" -> {
            "%.2f kWh".format(siteInfo?.PVI_Total_Gen_Today ?: 0f) to
                    "%.2f MWh".format((siteInfo?.PVI_Total_Gen_Lifetime?.toFloat() ?: 0f) / 1000f)
        }
        "Grid Power (kW)" -> {
            "%.2f kWh".format(siteInfo?.Grid_Export_Energy_Today ?: 0f) to
                    "%.2f MWh".format((siteInfo?.GRID_Active_Total_Export?.toFloat() ?: 0f) / 1000f)
        }
        "DG Power (kW)" -> {
            val dg1 = siteInfo?.DG1_Export_Energy_Today?.toFloat() ?: 0f
            val dg2 = siteInfo?.DG2_Export_Energy_Today?.toFloat() ?: 0f
            "%.2f kWh".format(dg1 + dg2) to "--"
        }
        "Load Power (kW)" -> {
            "%.2f kWh".format(siteInfo?.Total_Plant_Export_Today ?: 0f) to "--"
        }
        "ESS Output (kW)" -> {
            "%.2f kWh".format(siteInfo?.PCS_EnergyExport_Today ?: 0f) to
                    "%.2f MWh".format((siteInfo?.PCS_EnergyExport_Lifetime?.toFloat() ?: 0f) / 1000f)
        }
        else -> "--" to "--"
    }
}
