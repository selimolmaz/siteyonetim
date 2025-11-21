package com.makak.learnactivityapp.ui.screens.aysecimekran

import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.input.pointer.pointerInput
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
import com.makak.learnactivityapp.database.repository.PaymentRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
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
    val paymentRepository = remember { PaymentRepository(database.paymentDao()) }

    // State
    var site by remember { mutableStateOf<Site?>(null) }
    var months by remember { mutableStateOf<List<Month>>(emptyList()) }
    var monthsStatus by remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMonthForEdit by remember { mutableStateOf<Month?>(null) }
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

                    // Check payment status for each month
                    val statusMap = mutableMapOf<Long, Boolean>()
                    months.forEach { month ->
                        statusMap[month.id] = paymentRepository.isMonthFullyPaidInSite(foundSite.id, month.id)
                    }
                    monthsStatus = statusMap
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
        monthsStatus = monthsStatus,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onItemClick = { month ->
            println("🔍 AySecimEkrani - Selected month: ${month.name} (ID: ${month.id})")
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val navigationRoute = "screen3/$encodedSiteName/${month.id}"
            println("🔍 Navigation route: $navigationRoute")
            navController.navigate(navigationRoute)
        },
        onMonthLongClick = { month ->
            selectedMonthForEdit = month
            showEditDialog = true
        },
        onAddMonthClick = { showAddDialog = true }
    )

    // Add Month Dialog
    if (showAddDialog && site != null) {
        // Safe copy to avoid smart cast issues
        val safeSite = site!!

        AddMonthDialog(
            onDismiss = { showAddDialog = false },
            onMonthAdd = { monthNumber, year ->
                scope.launch {
                    try {
                        val monthName = getMonthName(monthNumber, year)

                        // Check if month already exists for this site
                        if (monthRepository.isMonthNameExists(safeSite.id, monthName)) {
                            errorMessage = "Bu ay zaten mevcut"
                            showAddDialog = false
                            return@launch
                        }

                        // Add new month
                        val newMonth = Month(
                            siteId = safeSite.id,
                            name = monthName,
                            year = year,
                            monthNumber = monthNumber
                        )
                        monthRepository.insertMonth(newMonth)

                        // Refresh months list and status
                        months = monthRepository.getMonthsBySiteId(safeSite.id)
                        val statusMap = mutableMapOf<Long, Boolean>()
                        months.forEach { month ->
                            statusMap[month.id] = paymentRepository.isMonthFullyPaidInSite(safeSite.id, month.id)
                        }
                        monthsStatus = statusMap
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

    // Edit Month Dialog
    if (showEditDialog && selectedMonthForEdit != null && site != null) {
        // Safe copy to avoid smart cast issues
        val safeSite = site!!

        EditMonthDialog(
            month = selectedMonthForEdit!!,
            onDismiss = {
                showEditDialog = false
                selectedMonthForEdit = null
            },
            onMonthUpdate = { month, newMonthNumber, newYear ->
                scope.launch {
                    try {
                        val newMonthName = getMonthName(newMonthNumber, newYear)

                        // Check if new month name already exists for this site (excluding current month)
                        if (monthRepository.isMonthNameExistsForUpdate(safeSite.id, newMonthName, month.id)) {
                            errorMessage = "Bu ay zaten mevcut"
                            showEditDialog = false
                            selectedMonthForEdit = null
                            return@launch
                        }

                        // Update month
                        val updatedMonth = month.copy(
                            name = newMonthName,
                            year = newYear,
                            monthNumber = newMonthNumber
                        )
                        monthRepository.updateMonth(updatedMonth)

                        // Refresh months list and status
                        months = monthRepository.getMonthsBySiteId(safeSite.id)
                        val statusMap = mutableMapOf<Long, Boolean>()
                        months.forEach { monthItem ->
                            statusMap[monthItem.id] = paymentRepository.isMonthFullyPaidInSite(safeSite.id, monthItem.id)
                        }
                        monthsStatus = statusMap
                        showEditDialog = false
                        selectedMonthForEdit = null
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Ay güncellenirken hata oluştu"
                        showEditDialog = false
                        selectedMonthForEdit = null
                    }
                }
            },
            onMonthDelete = { month ->
                scope.launch {
                    try {
                        monthRepository.deleteMonth(month.id)

                        // Refresh months list and status
                        months = monthRepository.getMonthsBySiteId(safeSite.id)
                        val statusMap = mutableMapOf<Long, Boolean>()
                        months.forEach { monthItem ->
                            statusMap[monthItem.id] = paymentRepository.isMonthFullyPaidInSite(safeSite.id, monthItem.id)
                        }
                        monthsStatus = statusMap
                        showEditDialog = false
                        selectedMonthForEdit = null
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Ay silinirken hata oluştu"
                        showEditDialog = false
                        selectedMonthForEdit = null
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
    monthsStatus: Map<Long, Boolean> = emptyMap(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onItemClick: (Month) -> Unit = {},
    onMonthLongClick: (Month) -> Unit = {},
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
                text = if (months.isEmpty() && !isLoading) "Ay eklemek için + butonuna tıklayın" else "Ay seçin (uzun basarak düzenleyebilirsiniz)",
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
                    items(
                        items = months,
                        key = { month -> month.id }
                    ) { month ->
                        val isMonthPaid = monthsStatus[month.id] ?: false

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onItemClick(month) },
                                        onLongPress = { onMonthLongClick(month) }
                                    )
                                },
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
                                Column {
                                    Text(
                                        text = month.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isMonthPaid) Color(0xFF2E7D32) else Color.Black
                                    )
                                    Text(
                                        text = "Düzenlemek için uzun basın",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
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
                Month(1, 1, "Kasım 2025", 2025, 11, 0),
                Month(2, 1, "Ekim 2025", 2025, 10, 0)
            ),
            onMonthLongClick = {}
        )
    }
}