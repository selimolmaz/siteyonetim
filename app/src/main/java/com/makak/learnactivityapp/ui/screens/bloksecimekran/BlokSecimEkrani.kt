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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.makak.learnactivityapp.database.database.AppDatabase
import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.repository.BlockRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import com.makak.learnactivityapp.ui.screens.ödemeekran.PaymentMemory
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BlokSecimEkrani(
    navController: NavController,
    siteName: String,
    selectedMonth: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val siteRepository = remember { SiteRepository(database.siteDao()) }
    val blockRepository = remember { BlockRepository(database.blockDao()) }

    // State
    var site by remember { mutableStateOf<Site?>(null) }
    var blocks by remember { mutableStateOf<List<Block>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load site and blocks on first composition
    LaunchedEffect(siteName, selectedMonth) {
        scope.launch {
            try {
                // Find site by name
                val allSites = siteRepository.getAllSites()
                val foundSite = allSites.find { it.name == siteName }

                if (foundSite != null) {
                    site = foundSite
                    // Blocks are site-based, not month-based!
                    blocks = blockRepository.getBlocksBySiteId(foundSite.id)
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

    BlokSecimEkraniContent(
        siteName = siteName,
        selectedMonth = selectedMonth,
        blocks = blocks,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onNavigateToScreen4 = { selectedBlock ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val encodedMonth = URLEncoder.encode(selectedMonth, StandardCharsets.UTF_8.toString())
            val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
            navController.navigate("screen4/$encodedSiteName/$encodedMonth/$encodedBlock")
        },
        onAddBlockClick = { showAddDialog = true }
    )

    // Add Block Dialog
    if (showAddDialog && site != null) {
        AddBlockDialog(
            onDismiss = { showAddDialog = false },
            onBlockAdd = { blockName ->
                scope.launch {
                    try {
                        // Check if block already exists for this site
                        if (blockRepository.isBlockNameExists(site!!.id, blockName)) {
                            errorMessage = "Bu blok adı zaten mevcut"
                            showAddDialog = false
                            return@launch
                        }

                        // Add new block to site (not month!)
                        val newBlock = Block(
                            siteId = site!!.id,
                            name = blockName
                        )
                        blockRepository.insertBlock(newBlock)

                        // Refresh blocks list
                        blocks = blockRepository.getBlocksBySiteId(site!!.id)
                        showAddDialog = false
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Blok eklenirken hata oluştu"
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
fun BlokSecimEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Ekim 2025",
    blocks: List<Block> = emptyList(),
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
                    items(blocks) { block ->
                        val isBlockPaid = PaymentMemory.isBlockFullyPaid(siteName, selectedMonth, block.name)

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
                Block(1, 1, "1A", 0),  // (id, siteId, name, createdAt)
                Block(2, 1, "2B", 0),
                Block(3, 1, "3C", 0)
            )
        )
    }
}