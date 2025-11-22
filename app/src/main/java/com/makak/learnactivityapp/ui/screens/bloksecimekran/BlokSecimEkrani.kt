package com.makak.learnactivityapp.ui.screens.bloksecimekran

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.ui.screens.bloksecimekran.viewmodel.BlokSecimViewModel
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BlokSecimEkrani(
    navController: NavController,
    siteName: String,
    selectedMonthId: Long,
    viewModel: BlokSecimViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    BlokSecimEkraniContent(
        siteName = siteName,
        selectedMonth = uiState.month?.name ?: "",
        blocks = uiState.blocks,
        blocksStatus = uiState.blocksStatus,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onNavigateToScreen4 = { selectedBlock ->
            uiState.month?.let { monthData ->
                val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
                val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
                val navigationRoute = "screen4/$encodedSiteName/${monthData.id}/$encodedBlock"
                navController.navigate(navigationRoute)
            }
        },
        onAddBlockClick = { showAddDialog = true }
    )

    // Add Block Dialog
    if (showAddDialog) {
        AddBlockDialog(
            onDismiss = { showAddDialog = false },
            onBlockAdd = { blockName ->
                viewModel.addBlock(blockName)
                showAddDialog = false
            }
        )
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
fun BlokSecimEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Ekim 2025",
    blocks: List<Block> = emptyList(),
    blocksStatus: Map<Long, Boolean> = emptyMap(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onNavigateToScreen4: (String) -> Unit = {},
    onAddBlockClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddBlockClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Blok Ekle",
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
                text = selectedMonth,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = if (blocks.isEmpty() && !isLoading) "Blok eklemek için + butonuna tıklayın" else "Blok seçin",
                style = MaterialTheme.typography.bodyMedium,
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
                // Blocks List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(
                        items = blocks,
                        key = { block -> block.id }
                    ) { block ->
                        val isBlockPaid = blocksStatus[block.id] ?: false

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNavigateToScreen4(block.name) },
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
                                    text = "Blok ${block.name}",
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun BlokSecimEkraniPreview() {
    LearnactivityappTheme {
        BlokSecimEkraniContent(
            blocks = listOf(
                Block(1, 1, "1A", 0),
                Block(2, 1, "2B", 0),
                Block(3, 1, "3C", 0)
            )
        )
    }
}