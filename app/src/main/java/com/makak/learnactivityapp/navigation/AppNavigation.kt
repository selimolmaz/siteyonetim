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
            "screen3/{siteName}/{selectedMonthId}",
            arguments = listOf(
                navArgument("siteName") { type = NavType.StringType },
                navArgument("selectedMonthId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedMonthId = backStackEntry.arguments?.getLong("selectedMonthId") ?: 0L
            BlokSecimEkrani(
                navController = navController,
                siteName = siteName,
                selectedMonthId = selectedMonthId
            )
        }

        composable(
            "screen4/{siteName}/{selectedMonthId}/{selectedBlock}",
            arguments = listOf(
                navArgument("siteName") { type = NavType.StringType },
                navArgument("selectedMonthId") { type = NavType.LongType },
                navArgument("selectedBlock") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedMonthId = backStackEntry.arguments?.getLong("selectedMonthId") ?: 0L
            val selectedBlock = backStackEntry.arguments?.getString("selectedBlock")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            KisiSecimEkrani(
                navController = navController,
                siteName = siteName,
                selectedMonthId = selectedMonthId,
                selectedBlock = selectedBlock
            )
        }

        composable(
            "screen5/{siteName}/{selectedMonthId}/{selectedBlock}/{selectedPersonId}",
            arguments = listOf(
                navArgument("siteName") { type = NavType.StringType },
                navArgument("selectedMonthId") { type = NavType.LongType },
                navArgument("selectedBlock") { type = NavType.StringType },
                navArgument("selectedPersonId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val siteName = backStackEntry.arguments?.getString("siteName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedMonthId = backStackEntry.arguments?.getLong("selectedMonthId") ?: 0L
            val selectedBlock = backStackEntry.arguments?.getString("selectedBlock")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: ""
            val selectedPersonId = backStackEntry.arguments?.getLong("selectedPersonId") ?: 0L
            OdemeEkrani(
                navController = navController,
                siteName = siteName,
                selectedMonthId = selectedMonthId,
                selectedBlock = selectedBlock,
                selectedPersonId = selectedPersonId
            )
        }
    }
}