package com.example.deltasitemanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.example.deltasitemanager.R
import com.example.deltasitemanager.ui.theme.poppins
import com.example.deltasitemanager.viewmodel.AuthViewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController

//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val siteInfo by authViewModel.siteInfo.collectAsState()
    val error by authViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        authViewModel.getSiteInfo()
    }

    val totalInstallations = siteInfo?.size ?: 0
    val totalInstalledCapacity = siteInfo?.sumOf { it.capacity } ?: 0

    val systemUiController = rememberSystemUiController()
    val headerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)

    SideEffect {
        systemUiController.setStatusBarColor(color = headerColor, darkIcons = false)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            DashboardHeader(
                onLogout = onLogout
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 11.dp, vertical = 9.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                InfoCard(
                    iconRes = R.drawable.solar_icon,
                    title = "Total Installations",
                    value = totalInstallations.toString(),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    iconRes = R.drawable.baseline_hexagon_24,
                    title = "Installed Capacity",
                    value = "$totalInstalledCapacity kW",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 64.dp)
            ) {
                siteInfo?.let { list ->
                    items(list) { site ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    authViewModel.setSelectedMacId(site.mac_id) // ✅ Set MAC ID before navigating
                                    navController.navigate(Screen.SiteDetail.createRoute(site.mac_id))
                                },
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    "EMS Name: ${site.ems_name}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    "Client: ${site.sub_name}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Capacity: ${site.capacity}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp
                                )
                                Text(
                                    "Last Updated: ${site.created_at}",
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
            

            error?.let {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Error: $it",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp)) // Added spacer at the bottom for better spacing
        }
    }
}


@Composable
fun InfoCard(
    title: String,
    value: String,
    imageUrl: String? = null,
    iconRes: Int? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(150.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.height(6.dp))
            }
            Text(title, style = MaterialTheme.typography.bodyMedium,fontFamily = poppins, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(value, style = MaterialTheme.typography.bodyMedium, fontFamily = poppins,fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun DashboardHeader(
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(WindowInsets.statusBars.asPaddingValues())
            .background(Color(0xFF2E3A59))
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Menu button removed here

        Text(
            text = "Energy Management Platform",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )

        Box {
            IconButton(onClick = { expanded = true }) {
                Image(
                    painter = rememberAsyncImagePainter("https://pqr.deltaww.com/BESS/public/images/faces-clipart/pic-1.png"),
                    contentDescription = "User Image",
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(18.dp)
                        )
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onLogout()
                    },
                    text = {
                        Text(
                            text = "Log Out",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                )
            }
        }
    }
}
