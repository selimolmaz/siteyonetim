package com.makak.learnactivityapp.ui.screens.kisisecimekran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
fun KisiSecimEkrani(
    navController: NavController,
    siteName: String,
    selectedMonth: String,
    selectedBlock: String
) {
    KisiSecimEkraniContent(
        siteName = siteName,
        selectedMonth = selectedMonth,
        selectedBlock = selectedBlock,
        onPersonSelected = { person ->
            // Kişi seçildi, Screen5'e git
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val encodedMonth = URLEncoder.encode(selectedMonth, StandardCharsets.UTF_8.toString())
            val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
            val encodedPerson = URLEncoder.encode(person, StandardCharsets.UTF_8.toString())
            navController.navigate("screen5/$encodedSiteName/$encodedMonth/$encodedBlock/$encodedPerson")
        }
    )
}

@Composable
fun KisiSecimEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Kasım 2025",
    selectedBlock: String = "2B",
    onPersonSelected: (String) -> Unit = {}
) {
    val people = listOf("A kişisi", "B kişisi", "C kişisi")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Text(
            text = siteName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = selectedMonth,
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = "Blok: $selectedBlock",
            fontSize = 16.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Kişi seçin",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // People Selection Section
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp)
        ) {
            items(people) { person ->
                val paymentKey = "$siteName-$selectedMonth-$selectedBlock-$person"
                val isSaved = PaymentMemory.isSaved(paymentKey)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPersonSelected(person) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSaved) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White
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
                            text = person,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isSaved) Color(0xFF2E7D32) else Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = if (isSaved) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KisiSecimEkraniPreview() {
    LearnactivityappTheme {
        KisiSecimEkraniContent(
            siteName = "Nezihpark Sitesi",
            selectedMonth = "Kasım 2025",
            selectedBlock = "2B"
        )
    }
}