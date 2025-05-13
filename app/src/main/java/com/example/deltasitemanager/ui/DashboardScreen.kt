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
import androidx.compose.material3.MaterialTheme
import com.example.deltasitemanager.viewmodel.AuthViewModel
import androidx.compose.runtime.Composable // For Composable function definition
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.res.painterResource // For loading resources like icons
import androidx.compose.runtime.collectAsState // For collecting state from ViewModel
import androidx.compose.foundation.Image // For displaying images
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import coil.compose.rememberImagePainter // For image loading (if you're using an external URL for images)
import androidx.compose.material3.Text // For displaying text
import androidx.compose.material3.DropdownMenuItem
import com.example.deltasitemanager.R
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController


@Composable
fun DashboardScreen(
    navController: NavHostController,
    onLogout: () -> Unit,
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
    val systemUiController = rememberSystemUiController()
    val headerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)

    SideEffect {
        systemUiController.setStatusBarColor(
            color = headerColor,
            darkIcons = false // white icons for dark background
        )
    }

    Scaffold(
        scaffoldState = scaffoldState,
        backgroundColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets.systemBars,
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
                    .padding(16.dp), // only your own content padding
                verticalArrangement = Arrangement.spacedBy(16.dp)
            )
            {
                // Info Cards
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min), // <-- add this line to make both cards equal height
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    InfoCard(
                        iconRes = R.drawable.solar_icon,
                        title = "Total Installations",
                        value = totalInstallations.toString(),
                        modifier = Modifier
                            .weight(1f)
                    )
                    InfoCard(
                        iconRes = R.drawable.baseline_hexagon_24,
                        title = "Total Installed Capacity",
                        value = "$totalInstalledCapacity kW",
                        modifier = Modifier
                            .weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                // Site info list
                LazyColumn {
                    siteInfo?.let { list ->
                        items(list) { site ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .clickable {
                                        navController.navigate(Screen.SiteDetail.createRoute(site.mac_id))
                                    },
                                elevation = 4.dp,
                                backgroundColor = MaterialTheme.colorScheme.surface,

                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("EMS Name: ${site.ems_name}", color = MaterialTheme.colorScheme.onSurface)
                                    Text("Client Name: ${site.sub_name}", color = MaterialTheme.colorScheme.onSurface)
                                    Text("Capacity: ${site.capacity}", color = MaterialTheme.colorScheme.onSurface)
                                    Text("Last Updated: ${site.created_at}", color = MaterialTheme.colorScheme.onSurface)
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


            iconRes?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = null,
                    tint = Color.Unspecified
                )
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
            .padding(WindowInsets.statusBars.asPaddingValues())
            .background(Color(0xFF435385))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)) // Glassy blue
            .padding(vertical = 16.dp, horizontal = 20.dp),

        verticalAlignment = Alignment.CenterVertically
    ) {

        Text(
            text = "Energy Management Platform",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
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
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.Black) // ✅ White background for visibility
            ) {
                DropdownMenuItem(
                    onClick = {
                        expanded = false
                        onLogout()
                    },
                    text = {
                        Text(
                            text = "Log Out",
                            color = Color.White
                        )
                    }
                )

            }
        }
    }
}

