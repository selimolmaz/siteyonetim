package com.makak.learnactivityapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.makak.learnactivityapp.ui.screens.sitesecimekran.SiteSecimEkrani
import com.makak.learnactivityapp.ui.screens.aysecimekran.AySecimEkrani
import com.makak.learnactivityapp.ui.screens.bloksecimekran.BlokSecimEkrani
import com.makak.learnactivityapp.ui.screens.kisisecimekran.KisiSecimEkrani
import com.makak.learnactivityapp.ui.screens.ödemeekran.OdemeEkrani
import java.net.URLDecoder
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
            SiteSecimEkrani(navController = navController)
        }

        composable(
            "screen2/{siteName}",
            arguments = listOf(navArgument("siteName") { type = NavType.StringType })
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            AySecimEkrani(
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
            BlokSecimEkrani(
                navController = navController,
                siteName = siteName,
                selectedMonth = selectedMonth
            )
        }

        composable(
            "screen4/{siteName}/{selectedMonth}/{selectedBlock}",
            arguments = listOf(
                navArgument("siteName") { type = NavType.StringType },
                navArgument("selectedMonth") { type = NavType.StringType },
                navArgument("selectedBlock") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedMonth = backStackEntry.arguments?.getString("selectedMonth")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedBlock = backStackEntry.arguments?.getString("selectedBlock")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            KisiSecimEkrani(
                navController = navController,
                siteName = siteName,
                selectedMonth = selectedMonth,
                selectedBlock = selectedBlock
            )
        }

        composable(
            "screen5/{siteName}/{selectedMonth}/{selectedBlock}/{selectedPerson}",
            arguments = listOf(
                navArgument("siteName") { type = NavType.StringType },
                navArgument("selectedMonth") { type = NavType.StringType },
                navArgument("selectedBlock") { type = NavType.StringType },
                navArgument("selectedPerson") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedMonth = backStackEntry.arguments?.getString("selectedMonth")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedBlock = backStackEntry.arguments?.getString("selectedBlock")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedPerson = backStackEntry.arguments?.getString("selectedPerson")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            OdemeEkrani(
                navController = navController,
                siteName = siteName,
                selectedMonth = selectedMonth,
                selectedBlock = selectedBlock,
                selectedPerson = selectedPerson
            )
        }
    }
}