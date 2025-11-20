package com.makak.learnactivityapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.makak.learnactivityapp.ui.screens.screen1.Screen1
import com.makak.learnactivityapp.ui.screens.screen2.Screen2
import com.makak.learnactivityapp.ui.screens.screen3.Screen3
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

        composable(
            "screen2/{siteName}",
            arguments = listOf(navArgument("siteName") { type = NavType.StringType })
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            Screen2(
                navController = navController,
                siteName = siteName
            )
        }

        composable(
            "screen3/{siteName}/{selectedMonth}",
            arguments = listOf(
                navArgument("siteName") { type = NavType.StringType },
                navArgument("selectedMonth") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedMonth = backStackEntry.arguments?.getString("selectedMonth")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            Screen3(
                navController = navController,
                siteName = siteName,
                selectedMonth = selectedMonth
            )
        }
    }
}