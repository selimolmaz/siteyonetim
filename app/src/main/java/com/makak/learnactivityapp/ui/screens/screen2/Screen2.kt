package com.makak.learnactivityapp.ui.screens.screen2

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
fun Screen2(navController: NavController) {
    Screen2Content(
        onNavigateBack = { navController.popBackStack() },
        onNavigateToScreen3 = { navController.navigate("screen3") }
    )
}

@Composable
fun Screen2Content(
    onNavigateBack: () -> Unit = {},
    onNavigateToScreen3: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Screen 2",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(onClick = onNavigateBack) {
                Text("Back to Screen 1")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = onNavigateToScreen3) {
                Text("Go to Screen 3")
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Screen2Preview() {
    LearnactivityappTheme {
        Screen2Content()
    }
}