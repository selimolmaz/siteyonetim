package com.makak.learnactivityapp.ui.screens.sitesecimekran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun Screen1(navController: NavController) {
    Screen1Content(
        onItemClick = { siteName ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            navController.navigate("screen2/$encodedSiteName")
        }
    )
}

@Composable
fun Screen1Content(
    onItemClick: (String) -> Unit = {}
) {
    val sites = listOf("Nezihpark Sitesi")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Text(
            text = "Site Seçimi",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Site seçin",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Sites List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(sites) { site ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(site) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = site,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = Color.Gray
                        )
                    }
                }
            }
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