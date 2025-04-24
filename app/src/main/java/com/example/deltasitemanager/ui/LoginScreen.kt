package com.example.deltasitemanager.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.deltasitemanager.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    val apiKey by authViewModel.apiKey.collectAsState()

    LaunchedEffect(apiKey) {
        if (apiKey != null) {
            showError = false
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo
        Image(
            painter = rememberAsyncImagePainter("https://pqr.deltaww.com/BESS/public/images/delta_logo.png"),
            contentDescription = "Delta Logo",
            modifier = Modifier
                .height(150.dp)
                .width(300.dp)
                .padding(bottom = 60.dp),
            contentScale = ContentScale.Fit
        )

        // Username Field
        OutlinedTextField(
            value = username,
            onValueChange = {
                username = it
                showError = false
            },
            label = { Text("Username") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Person, contentDescription = "User Icon") },
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color(0xFFF5F5F5),
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Password Field with Visibility Toggle
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                showError = false
            },
            label = { Text("Password") },
            singleLine = true,
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Icon") },
            trailingIcon = {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    modifier = Modifier.clickable { passwordVisible = !passwordVisible }
                )
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.outlinedTextFieldColors(
                backgroundColor = Color(0xFFF5F5F5),
                focusedBorderColor = MaterialTheme.colors.primary,
                unfocusedBorderColor = Color.Gray
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Error Message ABOVE login button
        if (showError) {
            Text(
                text = "Invalid username or password",
                fontSize = 14.sp,
                color = MaterialTheme.colors.error,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        // Login Button
        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                coroutineScope.launch {
                    authViewModel.login(username, password)
                    delay(2000)
                    if (authViewModel.apiKey.value == null) {
                        showError = true
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Login", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Forgot Password Centered
//        Text(
//            text = "Forgot Password?",
//            color = MaterialTheme.colors.primary,
//            fontSize = 14.sp,
//            modifier = Modifier.align(Alignment.CenterHorizontally)
//        )
    }
}
