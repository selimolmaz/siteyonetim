package com.makak.learnactivityapp.ui.screens.screen1

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme

@Composable
fun Screen1(navController: NavController) {
    Screen1Content(
        onNavigateToScreen2 = { navController.navigate("screen2") }
    )
}

@Composable
fun Screen1Content(
    onNavigateToScreen2: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Screen 1",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateToScreen2) {
            Text("Go to Screen 2")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Screen1Preview() {
    LearnactivityappTheme {
        Screen1Content()
    }
}