package com.makak.learnactivityapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.makak.learnactivityapp.ui.screens.screen1.Screen1
import com.makak.learnactivityapp.ui.screens.screen2.Screen2
import com.makak.learnactivityapp.ui.screens.screen3.Screen3

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "screen1",
        modifier = modifier
    ) {
        composable("screen1") {
            Screen1(navController = navController)
        }
        composable("screen2") {
            Screen2(navController = navController)
        }
        composable("screen3") {
            Screen3(navController = navController)
        }
    }
}