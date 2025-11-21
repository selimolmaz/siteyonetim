package com.makak.learnactivityapp.ui.screens.aysecimekran

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.makak.learnactivityapp.database.database.AppDatabase
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import com.makak.learnactivityapp.ui.screens.ödemeekran.PaymentMemory
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AySecimEkrani(
    navController: NavController,
    siteName: String = "Nezihpark Sitesi"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val siteRepository = remember { SiteRepository(database.siteDao()) }
    val monthRepository = remember { MonthRepository(database.monthDao()) }

    // State
    var site by remember { mutableStateOf<Site?>(null) }
    var months by remember { mutableStateOf<List<Month>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load site and months on first composition
    LaunchedEffect(siteName) {
        scope.launch {
            try {
                // Find site by name
                val allSites = siteRepository.getAllSites()
                val foundSite = allSites.find { it.name == siteName }

                if (foundSite != null) {
                    site = foundSite
                    months = monthRepository.getMonthsBySiteId(foundSite.id)
                } else {
                    errorMessage = "Site bulunamadı"
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Veriler yüklenirken hata oluştu"
                isLoading = false
            }
        }
    }

    AySecimEkraniContent(
        siteName = siteName,
        months = months,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onItemClick = { selectedMonth ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val encodedMonth = URLEncoder.encode(selectedMonth, StandardCharsets.UTF_8.toString())
            navController.navigate("screen3/$encodedSiteName/$encodedMonth")
        },
        onAddMonthClick = { showAddDialog = true }
    )

    // Add Month Dialog
    if (showAddDialog && site != null) {
        AddMonthDialog(
            onDismiss = { showAddDialog = false },
            onMonthAdd = { monthNumber, year ->
                scope.launch {
                    try {
                        val monthName = getMonthName(monthNumber, year)

                        // Check if month already exists for this site
                        if (monthRepository.isMonthNameExists(site!!.id, monthName)) {
                            errorMessage = "Bu ay zaten mevcut"
                            showAddDialog = false
                            return@launch
                        }

                        // Add new month
                        val newMonth = Month(
                            siteId = site!!.id,
                            name = monthName,
                            year = year,
                            monthNumber = monthNumber
                        )
                        monthRepository.insertMonth(newMonth)

                        // Refresh months list
                        months = monthRepository.getMonthsBySiteId(site!!.id)
                        showAddDialog = false
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Ay eklenirken hata oluştu"
                        showAddDialog = false
                    }
                }
            }
        )
    }

    // Show error message if exists
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            kotlinx.coroutines.delay(3000)
            errorMessage = null
        }
    }
}

@Composable
fun AySecimEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    months: List<Month> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onItemClick: (String) -> Unit = {},
    onAddMonthClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMonthClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ay Ekle",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Header Section
            Text(
                text = siteName,
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = if (months.isEmpty() && !isLoading) "Ay eklemek için + butonuna tıklayın" else "Ay seçin",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Error Message
            errorMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Red.copy(alpha = 0.1f))
                ) {
                    Text(
                        text = message,
                        color = Color.Red,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                // Months List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(months) { month ->
                        val isMonthPaid = PaymentMemory.isMonthFullyPaid(siteName, month.name)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onItemClick(month.name) },
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
                                    text = month.name,
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
    }
}

// Helper function to generate month name
private fun getMonthName(monthNumber: Int, year: Int): String {
    val months = listOf(
        "Ocak", "Şubat", "Mart", "Nisan",
        "Mayıs", "Haziran", "Temmuz", "Ağustos",
        "Eylül", "Ekim", "Kasım", "Aralık"
    )
    return "${months[monthNumber - 1]} $year"
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AySecimEkraniPreview() {
    LearnactivityappTheme {
        AySecimEkraniContent(
            months = listOf(
                Month(1, 1, "Kasım 2025", 2025, 11, 0),  // (id, siteId, name, year, monthNumber, createdAt)
                Month(2, 1, "Ekim 2025", 2025, 10, 0)
            )
        )
    }
}