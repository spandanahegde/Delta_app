package com.example.deltasitemanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import com.example.deltasitemanager.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable // For Composable function definition
import androidx.compose.ui.tooling.preview.Preview // For preview
import androidx.compose.ui.res.painterResource // For loading resources like icons
 // For navigation controller to handle screen transitions
import androidx.compose.runtime.collectAsState // For collecting state from ViewModel
import androidx.compose.foundation.Image // For displaying images (if required)
import coil.compose.rememberImagePainter // For image loading (if you're using an external URL for images)
import androidx.compose.material3.Text // For displaying text
import com.example.deltasitemanager.R

@Composable
fun DashboardScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val siteInfo by authViewModel.siteInfo.collectAsState()
    val error by authViewModel.error.collectAsState()

    val scaffoldState = rememberScaffoldState()
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        authViewModel.getSiteInfo()
    }

    // Calculate Total Installations and Total Installed Capacity
    val totalInstallations = siteInfo?.size ?: 0
    val totalInstalledCapacity = siteInfo?.sumOf { it.capacity } ?: 0

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colorScheme.background,
        drawerContent = {
            DrawerMenu(
                navController = navController,
                onNavigate = { route ->
                    navController.navigate(route) {
                        launchSingleTop = true
                    }
                },
                onCloseDrawer = {
                    coroutineScope.launch { scaffoldState.drawerState.close() }
                },
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme
            )
        },
        topBar = {
            DashboardHeader(
                onMenuClick = {
                    coroutineScope.launch {
                        scaffoldState.drawerState.open()
                    }
                },
                onLogout = onLogout
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                // Info Cards
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // <-- add this line to make both cards equal height
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        title = "Total Installations",
                        value = totalInstallations.toString(),
                        imageUrl = "https://pqr.deltaww.com/BESS/public/images/solar_icon.png",
                        modifier = Modifier
                            .weight(1f)
                    )
                    InfoCard(
                        title = "Total Installed Capacity",
                        value = "$totalInstalledCapacity kW",
                        iconRes = R.drawable.ic_launcher_foreground,
                        modifier = Modifier
                            .weight(1f)
                    )
                }


                // Site info list
                LazyColumn {
                    siteInfo?.let { list ->
                        items(list) { site ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .clickable {
                                        navController.navigate(Screen.SiteDetail.createRoute(site.mac_id))
                                    },
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colorScheme.surface
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("EMS Name: ${site.ems_name}", color = MaterialTheme.colorScheme.onSurface)
                                    Text("Sub Name: ${site.sub_name}", color = MaterialTheme.colorScheme.onSurface)
                                    Text("Capacity: ${site.capacity}", color = MaterialTheme.colorScheme.onSurface)
                                }
                            }
                        }
                    }
                }

                // Error handling
                error?.let {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Error: $it", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
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
        modifier = modifier
            .fillMaxHeight(), // <-- ensure equal height when used inside Row
        elevation = 4.dp,
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize() // make content fill the card
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween // evenly space the content
        ) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)

            imageUrl?.let {
                Image(painter = rememberImagePainter(it), contentDescription = null)
            }
            iconRes?.let {
                Icon(painter = painterResource(id = it), contentDescription = null)
            }
        }
    }
}

@Composable
fun DashboardHeader(
    onMenuClick: () -> Unit = {},
    onLogout: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }


    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)) // Glassy blue
            .padding(vertical = 16.dp, horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    )  {
        IconButton(onClick = onMenuClick) {
            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Menu", tint = MaterialTheme.colorScheme.onPrimary)
        }
        Text(
            text = "Energy Management Platform",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.weight(1f)
        )
        Box {
            IconButton(onClick = { expanded = true }) {
                Image(
                    painter = rememberAsyncImagePainter("https://pqr.deltaww.com/BESS/public/images/faces-clipart/pic-1.png"),
                    contentDescription = "User Image",
                    modifier = Modifier.size(40.dp)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                DropdownMenuItem(onClick = {
                    expanded = false
                    onLogout()
                }) {
                    Text("Log Out")
                }
            }
        }
    }
}

@Composable
fun DrawerMenu(
    navController: NavHostController,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    ModalDrawerSheet {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AsyncImage(
                    model = "https://pqr.deltaww.com/BESS/public/images/logo.png",
                    contentDescription = "Delta Logo",
                    modifier = Modifier.size(100.dp)
                )

                IconButton(onClick = onToggleTheme) {
                    Icon(
                        imageVector = if (isDarkTheme) Icons.Filled.Brightness7 else Icons.Filled.Brightness4,
                        contentDescription = "Toggle Theme",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            DrawerItem("Dashboard", Icons.Filled.Dashboard, onNavigate, onCloseDrawer)
            DrawerItem("User Info", Icons.Filled.Person, onNavigate, onCloseDrawer)
            DrawerItem("Client Info", Icons.Filled.Business, onNavigate, onCloseDrawer)
            DrawerItem("Login History", Icons.Filled.History, onNavigate, onCloseDrawer)
        }
    }
}

@Composable
fun DrawerItem(
    label: String,
    icon: ImageVector,
    onNavigate: (String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                onNavigate(label.lowercase().replace(" ", "_"))
                onCloseDrawer()
            }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = label, modifier = Modifier.size(24.dp), tint = MaterialTheme.colorScheme.onSurface)
        Spacer(modifier = Modifier.width(20.dp))
        Text(text = label, fontSize = 16.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
    }
}
