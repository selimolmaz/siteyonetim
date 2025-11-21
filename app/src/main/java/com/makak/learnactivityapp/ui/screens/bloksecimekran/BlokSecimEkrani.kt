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
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.repository.BlockRepository
import com.makak.learnactivityapp.database.repository.PaymentRepository
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun BlokSecimEkrani(
    navController: NavController,
    siteName: String,
    selectedMonthId: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val siteRepository = remember { SiteRepository(database.siteDao()) }
    val monthRepository = remember { MonthRepository(database.monthDao()) }
    val blockRepository = remember { BlockRepository(database.blockDao()) }
    val paymentRepository = remember { PaymentRepository(database.paymentDao()) }

    // State
    var site by remember { mutableStateOf<Site?>(null) }
    var month by remember { mutableStateOf<Month?>(null) }
    var blocks by remember { mutableStateOf<List<Block>>(emptyList()) }
    var blocksStatus by remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load site, month and blocks on first composition
    LaunchedEffect(siteName, selectedMonthId) {
        scope.launch {
            try {
                println("🔍 BlokSecimEkrani Debug - MonthID: $selectedMonthId")

                // Find site by name
                val allSites = siteRepository.getAllSites()
                val foundSite = allSites.find { it.name == siteName }

                if (foundSite != null) {
                    site = foundSite
                    println("✅ Site found: ${foundSite.name}")

                    // Find month by ID directly
                    val foundMonth = monthRepository.getMonthById(selectedMonthId)
                    println("🔍 Searching month with ID: $selectedMonthId")
                    println("🔍 Found month: $foundMonth")

                    if (foundMonth != null) {
                        month = foundMonth
                        println("✅ Month found: ${foundMonth.name} (ID: ${foundMonth.id})")

                        // Blocks are site-based, not month-based!
                        blocks = blockRepository.getBlocksBySiteId(foundSite.id)

                        // Check payment status for each block in this month
                        val statusMap = mutableMapOf<Long, Boolean>()
                        blocks.forEach { block ->
                            statusMap[block.id] = paymentRepository.isBlockFullyPaid(block.id, foundMonth.id)
                        }
                        blocksStatus = statusMap
                    } else {
                        errorMessage = "Ay bulunamadı (ID: $selectedMonthId)"
                        println("❌ Month not found with ID: $selectedMonthId")
                        // Month bulunamazsa geri dön
                        navController.popBackStack()
                    }
                } else {
                    errorMessage = "Site bulunamadı"
                    println("❌ Site not found: $siteName")
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Veriler yüklenirken hata oluştu: ${e.message}"
                println("❌ Exception in BlokSecimEkrani: ${e.message}")
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    BlokSecimEkraniContent(
        siteName = siteName,
        selectedMonth = month?.name ?: "",
        blocks = blocks,
        blocksStatus = blocksStatus,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onNavigateToScreen4 = { selectedBlock ->
            month?.let { monthData ->
                val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
                val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
                val navigationRoute = "screen4/$encodedSiteName/${monthData.id}/$encodedBlock"
                println("🔍 BlokSecimEkrani navigation route: $navigationRoute")
                navController.navigate(navigationRoute)
            }
        },
        onAddBlockClick = { showAddDialog = true }
    )

    // Add Block Dialog
    if (showAddDialog && site != null && month != null) {
        // Safe copies to avoid smart cast issues
        val safeSite = site!!
        val safeMonth = month!!

        AddBlockDialog(
            onDismiss = { showAddDialog = false },
            onBlockAdd = { blockName ->
                scope.launch {
                    try {
                        // Check if block already exists for this site
                        if (blockRepository.isBlockNameExists(safeSite.id, blockName)) {
                            errorMessage = "Bu blok adı zaten mevcut"
                            showAddDialog = false
                            return@launch
                        }

                        // Add new block to site (not month!)
                        val newBlock = Block(
                            siteId = safeSite.id,
                            name = blockName
                        )
                        blockRepository.insertBlock(newBlock)

                        // Refresh blocks list and status
                        blocks = blockRepository.getBlocksBySiteId(safeSite.id)
                        val statusMap = mutableMapOf<Long, Boolean>()
                        blocks.forEach { block ->
                            statusMap[block.id] = paymentRepository.isBlockFullyPaid(block.id, safeMonth.id)
                        }
                        blocksStatus = statusMap
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