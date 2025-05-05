package com.example.deltasitemanager.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Dp
import com.example.deltasitemanager.R
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.deltasitemanager.viewmodel.AuthViewModel
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.delay
import com.example.deltasitemanager.models.IndividualSiteInfo
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDetailScreen(
    macId: String,
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val scope = rememberCoroutineScope()

    val individualSiteInfo by authViewModel.individualSiteInfo.collectAsState()
    val powerGridData = remember { mutableStateListOf<Entry>() }
    val loadData = remember { mutableStateListOf<Entry>() }
    val dgData = remember { mutableStateListOf<Entry>() }
    val essData = remember { mutableStateListOf<Entry>() }

    var xIndex by remember { mutableStateOf(0f) }

    // Fetch every 60 seconds
    LaunchedEffect(macId) {
        var counter = 0f
        while (true) {
            authViewModel.getIndividualSiteInfo(macId)
            val siteData = authViewModel.individualSiteInfo.value?.firstOrNull()
            siteData?.let {
                powerGridData.add(Entry(counter, it.Grid_Import_Energy_Today.toFloat()))
                loadData.add(Entry(counter, it.Load_Active_Power.toFloat()))
                dgData.add(Entry(counter, (it.DG1_Active_Total_Export + it.DG2_Active_Total_Export).toFloat()))
                essData.add(Entry(counter, it.PCS_EnergyExport_Today.toFloat()))
                counter += 1f
            }
            delay(60000)
        }
    }

//    ModalNavigationDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            DrawerContent(navController = navController, onItemClick = {
//                scope.launch { drawerState.close() }
//            })
//        }
//    ) {
        Scaffold(
            topBar = {
                SmallTopAppBar(
                    title = {
                        Text(
                            "Real Time Monitoring : BESS Mode",
                            style = MaterialTheme.typography.titleMedium.copy(color = Color.White)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.popBackStack()
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    actions = {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "View Graph",
                            modifier = Modifier
                                .size(24.dp)
                                .clickable {
                                    navController.navigate("graph_screen/$macId")
                                },
                            tint = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = Color(0xFF435385)
                    )
                )

            }
        ) { innerPadding ->
            val siteData = individualSiteInfo?.firstOrNull()

            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Enables scrolling if content overflows
            ) {
                // Diagram Section (with fixed height so it doesn't eat all space)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp) // Set height based on your visual needs
                ) {
                    SiteDiagram(siteData)
                }

                // 2. Spacer between diagram and summary
                Spacer(modifier = Modifier.height(45.dp))

                // 3. Summary widget
                siteData?.let {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WidgetItem(
                                title = "Grid Outage",
                                todayValue = "${it.PerDay_GridOutage_Instance} times"
                            )
                            WidgetItem(
                                title = "Avg Load",
                                todayValue = "${String.format("%.2f", it.PerDay_AvgLoad)} kW"
                            )
                            WidgetItem(
                                title = "PCS Import",
                                todayValue = "${String.format("%.2f", it.PCS_EnergyImport_Today)} kWh",
                                cumulativeValue = "${String.format("%.2f", it.PCS_EnergyImport_Lifetime / 1000)} MWh"
                            )
                        }


                    }

                    Spacer(modifier = Modifier.height(16.dp)) // Add spacing between cards

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WidgetItem(
                                title = "Charging Cycles",
                                todayValue = String.format("%.2f", it.charging_cycles)
                            )
                            WidgetItem(
                                title = "Discharging Cycles",
                                todayValue = String.format("%.2f", it.discharging_cycles)
                            )
                            WidgetItem(
                                title = "Total Cycle Count",
                                todayValue = String.format("%.2f", it.total_cycle_count)
                            )
                        }

                    }
                    Spacer(modifier = Modifier.height(16.dp)) // spacing between rows if needed

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )
                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WidgetItem(
                                title = "Grid Outage Duration",
                                todayValue = formatDuration(it.Grid_outage_duration) // in seconds
                            )
                            WidgetItem(
                                title = "Decarbonization",
                                todayValue = "${String.format("%.2f", it.co2_emission)} kg",
                                cumulativeValue = "${String.format("%.2f", it.co2_emission / 1000)} ton"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp)) // spacing between rows if needed
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .background(
                                MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    )

                    {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            WidgetItem(
                                title = "Diesel Saving",
                                todayValue = "${String.format("%.2f", it.diesel_save )}Ltr",
//                                cumulativeValue = "${String.format("%.2f", it.diesel_save_cumulative)}Ltr"
                            )

                            WidgetItem(
                                title = "Diesel Cost Saving",
                                todayValue = "₹ ${String.format("%.2f", it.cost_diesel_save )}",
//                                cumulativeValue = "₹ ${String.format("%.2f", it.cost_diesel_save_cumulative ?: 0.0)}"
                            )
                        }

                    }

                    }


                }
            }
        }


fun formatDuration(seconds: Int): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return String.format("%02dhr : %02dmin : %02dsec", hrs, mins, secs)
}


@Composable
fun WidgetItem(
    title: String,
    todayValue: String,
    cumulativeValue: String? = null,
    unit: String? = null
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy( color =Color(0xFFFF9800))
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (cumulativeValue == null) {
            Text(
                text = "Today",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
            Text(
                text = if (unit != null) "$todayValue $unit" else todayValue,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium,color =Color(0xFF9370DB))
            )
        } else {
            Text(
                text = "Today / Cumulative",
                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
            )
            Text(
                text = if (unit != null) "$todayValue $unit / $cumulativeValue $unit" else "$todayValue / $cumulativeValue",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium,color =Color(0xFF9370DB))
            )
        }
    }
}

//@Composable
//fun DrawerItem(label: String, icon: ImageVector, onClick: () -> Unit) {
//    Row(
//        verticalAlignment = Alignment.CenterVertically,
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { onClick() }
//            .padding(vertical = 12.dp)
//    ) {
//        Icon(icon, contentDescription = label, tint = Color.White)
//        Spacer(modifier = Modifier.width(8.dp))
//        Text(text = label, color = Color.White)
//    }
//}
//@Composable
//fun DrawerContent(navController: NavController, onItemClick: () -> Unit) {
//    Column(
//        modifier = Modifier
//            .fillMaxHeight()
//            .width(250.dp)
//            .background(Color.Black)
//            .padding(16.dp)
//    ) {
//        DrawerItem("Site Dashboard", Icons.Default.Dashboard, onClick = {
//            // navController.navigate("site_dashboard")
//            onItemClick()
//        })
//        DrawerItem("Analytics", Icons.Default.ShowChart, onClick = {
//            // navController.navigate("analytics")
//            onItemClick()
//        })
//        DrawerItem("Report", Icons.Default.Assignment, onClick = {
//            // navController.navigate("report")
//            onItemClick()
//        })
//        DrawerItem("Events", Icons.Default.Notifications, onClick = {
//            // navController.navigate("events")
//            onItemClick()
//        })
//        DrawerItem("Homepage", Icons.Default.Home, onClick = {
//            // navController.navigate("homepage")
//            onItemClick()
//        })
//    }
//}
@Composable
fun DeviceBox(
    modifier: Modifier = Modifier,
    imageRes: Int,
    label: String,
    value: String,
    label1: String? = null,
    value1: String? = null,
    extraImageRes: Int? = null,
    infoOnLeft: Boolean = false,
    imageOffsetX: Dp = 0.dp,
    imageOffsetY: Dp = 0.dp,
    extraImageOffsetX: Dp = 0.dp,
    extraImageOffsetY: Dp = 0.dp,
) {
    val iconSize = 80.dp
    val extraIconSize = 20.dp

    Row(
        modifier = modifier.padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (infoOnLeft) {
            // Left side info
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .widthIn(min = 100.dp, max = 180.dp)
            ) {
                // First label
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                // Value + optional icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    extraImageRes?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = "Extra Icon",
                            modifier = Modifier
                                .size(extraIconSize)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        color = Color(0xFFEC942A),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Second label and value
                label1?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value1 ?: "--",
                        fontSize = 18.sp,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        // Device Image
        Box(
            modifier = Modifier.offset(x = imageOffsetX, y = imageOffsetY),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Device Image",
                modifier = Modifier
                    .size(iconSize)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.Gray, CircleShape)
            )
        }

        if (!infoOnLeft) {
            Spacer(modifier = Modifier.width(4.dp))
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.widthIn(min = 100.dp, max = 140.dp)
            ) {
                // First label
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Row for value with icon
                Row(verticalAlignment = Alignment.CenterVertically) {
                    extraImageRes?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = "Extra Icon",
                            modifier = Modifier
                                .size(extraIconSize)
                                .padding(end = 4.dp)
                        )
                    }
                    Text(
                        text = value,
                        fontSize = 18.sp,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Second label and value
                label1?.let {
                    Text(
                        text = it,
                        fontSize = 12.sp,
                        color = Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = value1 ?: "--",
                        fontSize = 18.sp,
                        color = Color(0xFFFF5722),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}


@Composable
fun SiteDiagram(siteData: IndividualSiteInfo?) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val centerSize = 120.dp
        val iconSize = 80.dp
        val offsetX = 150.dp
        val offsetY = 150.dp
        val borderColor = Color(0xFF76C7C0)

        Box(
            modifier = Modifier
                .padding(top = 80.dp)
                .size(400.dp),
            contentAlignment = Alignment.Center
        )  {
            val dgColor = Color(0xFFFF5722)
            val socPathColor = Color(0xFFFFC107)
            val loadColor = Color(0xFFFF9800)
            val gridColor = Color(0xFF2196F3)

            Canvas(modifier = Modifier.fillMaxSize()) {
                val cloudPosition = Offset(size.width / 2, size.height / 2)
                val dgPosition =
                    Offset(size.width / 2 + offsetX.toPx(), size.height / 2 - offsetY.toPx())
                val socPosition =
                    Offset(size.width / 2 + offsetX.toPx(), size.height / 2 + offsetY.toPx())
                val loadPosition =
                    Offset(size.width / 2 - offsetX.toPx(), size.height / 2 + offsetY.toPx())
                val gridPosition =
                    Offset(size.width / 2 - offsetX.toPx(), size.height / 2 - offsetY.toPx())

                val pathGap = 5.dp.toPx()
                val verticalStagger = 8.dp.toPx()

                val cloudBottomCenter =
                    Offset(cloudPosition.x, cloudPosition.y + centerSize.toPx() / 2)
                val socTopCenter = Offset(socPosition.x, socPosition.y - iconSize.toPx() / 2)

                // DG Path
                drawPath(
                    path = Path().apply {
                        moveTo(dgPosition.x, dgPosition.y)
                        lineTo(dgPosition.x, cloudPosition.y)
                        lineTo(cloudPosition.x, cloudPosition.y)
                    },
                    color = dgColor,
                    style = Stroke(
                        2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )

                // SOC left path
                val leftStart = Offset(cloudBottomCenter.x - pathGap, cloudBottomCenter.y)
                val leftDown = Offset(leftStart.x, leftStart.y + 40.dp.toPx())
                val leftRight = Offset(socTopCenter.x - pathGap, leftDown.y)
                val leftFinal = Offset(leftRight.x, socTopCenter.y)
                drawPath(
                    path = Path().apply {
                        moveTo(leftStart.x, leftStart.y)
                        lineTo(leftDown.x, leftDown.y)
                        lineTo(leftRight.x, leftRight.y)
                        lineTo(leftFinal.x, socTopCenter.y)
                    },
                    color = socPathColor,
                    style = Stroke(
                        2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )

                // SOC right path
                val rightStart = Offset(cloudBottomCenter.x + pathGap, cloudBottomCenter.y)
                val rightDown = Offset(rightStart.x, rightStart.y + 20.dp.toPx() + verticalStagger)
                val rightRight = Offset(socTopCenter.x + pathGap, rightDown.y)
                val rightFinal = Offset(rightRight.x, socTopCenter.y)
                drawPath(
                    path = Path().apply {
                        moveTo(rightStart.x, rightStart.y)
                        lineTo(rightDown.x, rightDown.y)
                        lineTo(rightRight.x, rightRight.y)
                        lineTo(rightFinal.x, socTopCenter.y)
                    },
                    color = socPathColor,
                    style = Stroke(
                        2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )

                // Start under the cloud
                val loadStart = Offset(
                    cloudBottomCenter.x - 20.dp.toPx(),
                    cloudBottomCenter.y  // slightly below the cloud center
                )

              // Vertical drop down from cloud
                val verticalDrop = Offset(loadStart.x, loadStart.y + 20.dp.toPx()) // drop vertically
              // Turn left horizontally, extending to the load's center
                val horizontalToLoadCenter = Offset(loadPosition.x, verticalDrop.y) // move horizontally to load's center

              // Final vertical drop to load image
                val finalLoadPosition = Offset(loadPosition.x, loadPosition.y)

                drawPath(
                    path = Path().apply {
                        moveTo(loadStart.x, loadStart.y) // Start under the cloud
                        lineTo(verticalDrop.x, verticalDrop.y) // Drop vertically from cloud
                        lineTo(horizontalToLoadCenter.x, horizontalToLoadCenter.y) // Extend horizontally to load's center
                        lineTo(finalLoadPosition.x, finalLoadPosition.y) // Final vertical drop to load
                    },
                    color = loadColor,
                    style = Stroke(
                        3.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )


                // Grid → Cloud Path ("L" shaped)
                val cloudLeft = Offset(cloudPosition.x - centerSize.toPx() / 2, cloudPosition.y)
                val gridToHorizontal =
                    Offset(gridPosition.x - 10.dp.toPx(), gridPosition.y) // small right offset
                val downToCloud = Offset(gridToHorizontal.x, cloudLeft.y)
                val toCloud = Offset(cloudLeft.x, cloudLeft.y)

                drawPath(
                    path = Path().apply {
                        moveTo(gridPosition.x, gridPosition.y)
                        lineTo(gridToHorizontal.x, gridToHorizontal.y) // horizontal segment
                        lineTo(downToCloud.x, downToCloud.y)           // vertical segment
                        lineTo(toCloud.x, toCloud.y)                   // horizontal to cloud
                    },
                    color = gridColor,
                    style = Stroke(
                        2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )


                val startFromCloud = Offset(
                cloudPosition.x - centerSize.toPx() / 2,
                cloudPosition.y - 10.dp.toPx() // slightly above cloud left
            )
                // Starting point at cloud's left
                val horizontalOffset = 90.dp.toPx() // adjust length as needed
                val verticalOffset = 20.dp.toPx()   // vertical distance from cloud to grid

                val horizontalLeft =
                    Offset(startFromCloud.x - horizontalOffset, startFromCloud.y) // move left
                val verticalUpToGrid =
                    Offset(horizontalLeft.x, gridPosition.y + verticalOffset) // move up

                drawPath(
                    path = Path().apply {
                        moveTo(startFromCloud.x, startFromCloud.y) // Start at cloud left
                        lineTo(horizontalLeft.x, horizontalLeft.y) // Horizontal segment
                        lineTo(verticalUpToGrid.x, verticalUpToGrid.y) // Vertical up
                        lineTo(
                            gridPosition.x,
                            gridPosition.y
                        ) // Final small line to exact grid point (optional)
                    },
                    color = gridColor,
                    style = Stroke(
                        2.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
                    )
                )

            }
                // Center - Cloud (with optional label below)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(centerSize)
                    .border(4.dp, borderColor, RoundedCornerShape(50.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.cloud),
                    contentDescription = "Cloud",
                    contentScale = ContentScale.Fit
                )
            }


// Power Grid (Top-Left)
            DeviceBox(
                modifier = Modifier
                    .offset(x = -offsetX + 40.dp, y = -offsetY), // shift right for label visibility
                imageRes = R.drawable.power_grid,
                label = "Power Grid",
                value = "${siteData?.GRID_Active_Power_RYB?.toString() ?: "--"} kW",
//                infoOnLeft = true,
                imageOffsetX = (0).dp
            )

            DeviceBox(
                modifier = Modifier
                    .offset(x = offsetX - 20.dp, y = -offsetY), // Move the whole box 20.dp to the left
                imageRes = R.drawable.dg,
                label = "DG (250 kVA)",
                value = "${siteData?.DG1_Active_Power_RYB ?: "--"} kW",
                label1 = "DG (500 kVA)",
                value1 = "${siteData?.DG2_Active_Power_RYB ?: "--"} kW",
                infoOnLeft = true,
                imageOffsetY = (0).dp,
                imageOffsetX = (-30).dp
            )





// Load (Bottom-Left)
            DeviceBox(
                modifier = Modifier
                    .offset(x = -offsetX + 40.dp, y = offsetY), // shift right for label visibility
                imageRes = R.drawable.load,
                label = "Load",
                value = "${siteData?.Load_Active_Power?.toString() ?: "--"} kW",
//                infoOnLeft = true,
                imageOffsetX = (0).dp // 👈 Shift image slightly to left
            )


            DeviceBox(
                modifier = Modifier .offset(x = offsetX - 20.dp, y = offsetY),  // Adjust the offset as needed
                imageRes = R.drawable.ess2,
                extraImageRes = R.drawable.soc,
                label = "SoC",
                value = "${siteData?.Total_SoC?.let { "$it %" } ?: "--"} kw",
                label1 = "ESS Output",
                value1 = "${siteData?.PCS_ActivePower ?: "--"} kW",
                infoOnLeft = true, // This places info on the opposite side
                imageOffsetX = (-30).dp,
                extraImageOffsetX = (-70).dp,  // Adjust this value to move extra image to the left
                extraImageOffsetY = 0.dp
            )


        }
    }
}

