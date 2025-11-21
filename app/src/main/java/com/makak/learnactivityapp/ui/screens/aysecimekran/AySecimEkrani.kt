package com.makak.learnactivityapp.ui.screens.aysecimekran

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
import com.makak.learnactivityapp.ui.screens.ödemeekran.PaymentMemory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AySecimEkrani(
    navController: NavController,
    siteName: String = "Nezihpark Sitesi"
) {
    AySecimEkraniContent(
        siteName = siteName,
        onItemClick = { selectedMonth ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val encodedMonth = URLEncoder.encode(selectedMonth, StandardCharsets.UTF_8.toString())
            navController.navigate("screen3/$encodedSiteName/$encodedMonth")
        }
    )
}

@Composable
fun AySecimEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    onItemClick: (String) -> Unit = {}
) {
    val months = listOf("Kasım 2025", "Ekim 2025", "Eylül 2025", "Aralık 2025", "Ocak 2026")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Text(
            text = siteName,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Ay seçin",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Months List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(months) { month ->
                val isMonthPaid = PaymentMemory.isMonthFullyPaid(siteName, month)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onItemClick(month) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMonthPaid) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White
                    ),
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
                            text = month,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isMonthPaid) Color(0xFF2E7D32) else Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = if (isMonthPaid) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AySecimEkraniPreview() {
    LearnactivityappTheme {
        AySecimEkraniContent()
    }
}