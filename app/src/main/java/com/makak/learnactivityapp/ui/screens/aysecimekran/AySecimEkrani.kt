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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.ui.screens.aysecimekran.viewmodel.AySecimViewModel
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun AySecimEkrani(
    navController: NavController,
    siteName: String = "Nezihpark Sitesi",
    viewModel: AySecimViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMonthForEdit by remember { mutableStateOf<Month?>(null) }

    AySecimEkraniContent(
        siteName = siteName,
        months = uiState.months,
        monthsStatus = uiState.monthsStatus,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onItemClick = { month ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val navigationRoute = "screen3/$encodedSiteName/${month.id}"
            navController.navigate(navigationRoute)
        },
        onMonthLongClick = { month ->
            selectedMonthForEdit = month
            showEditDialog = true
        },
        onAddMonthClick = { showAddDialog = true }
    )

    // Add Month Dialog
    if (showAddDialog) {
        AddMonthDialog(
            onDismiss = { showAddDialog = false },
            onMonthAdd = { monthNumber, year ->
                viewModel.addMonth(monthNumber, year)
                showAddDialog = false
            }
        )
    }

    // Edit Month Dialog
    selectedMonthForEdit?.let { month ->
        if (showEditDialog) {
            EditMonthDialog(
                month = month,
                onDismiss = {
                    showEditDialog = false
                    selectedMonthForEdit = null
                },
                onMonthUpdate = { month, newMonthNumber, newYear ->
                    viewModel.updateMonth(month, newMonthNumber, newYear)
                    showEditDialog = false
                    selectedMonthForEdit = null
                },
                onMonthDelete = { month ->
                    viewModel.deleteMonth(month.id)
                    showEditDialog = false
                    selectedMonthForEdit = null
                }
            )
        }
    }

    // Auto-clear error after 3 seconds
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            delay(3000)
            viewModel.clearError()
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

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AySecimEkraniPreview() {
    LearnactivityappTheme {
        AySecimEkraniContent(
            months = listOf(
                Month(1, 1, "Kasım 2025", 2025, 11, 0),
                Month(2, 1, "Ekim 2025", 2025, 10, 0)
            )
        )
    }
}