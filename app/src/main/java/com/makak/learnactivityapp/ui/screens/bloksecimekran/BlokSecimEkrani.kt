package com.makak.learnactivityapp.ui.screens.bloksecimekran

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
fun Screen3(
    navController: NavController,
    siteName: String,
    selectedMonth: String
) {
    Screen3Content(
        siteName = siteName,
        selectedMonth = selectedMonth,
        onNavigateBack = { navController.popBackStack() },
        onNavigateToScreen4 = { selectedBlock ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val encodedMonth = URLEncoder.encode(selectedMonth, StandardCharsets.UTF_8.toString())
            val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
            navController.navigate("screen4/$encodedSiteName/$encodedMonth/$encodedBlock")
        }
    )
}

@Composable
fun Screen3Content(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Ekim 2025",
    onNavigateBack: () -> Unit = {},
    onNavigateToScreen4: (String) -> Unit = {}
) {
    val blocks = listOf("1A", "2B", "3C", "4D", "5E")

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
            text = selectedMonth,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Text(
            text = "Blok seçin",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Blocks List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(blocks) { block ->
                val isBlockPaid = PaymentMemory.isBlockFullyPaid(siteName, selectedMonth, block)

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToScreen4(block) },
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isBlockPaid) Color(0xFF4CAF50).copy(alpha = 0.2f) else Color.White
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
                            text = "Blok $block",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Normal,
                            color = if (isBlockPaid) Color(0xFF2E7D32) else Color.Black
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = "Navigate",
                            tint = if (isBlockPaid) Color(0xFF2E7D32) else Color.Gray
                        )
                    }
                }
            }
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