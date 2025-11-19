package com.makak.learnactivityapp.ui.screens.screen3

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
fun Screen3(navController: NavController) {
    Screen3Content(
        onNavigateBack = { navController.popBackStack() }
    )
}

@Composable
fun Screen3Content(
    onNavigateBack: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Screen 3",
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onNavigateBack) {
            Text("Back to Screen 2")
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun Screen3Preview() {
    LearnactivityappTheme {
        Screen3Content()
    }
}