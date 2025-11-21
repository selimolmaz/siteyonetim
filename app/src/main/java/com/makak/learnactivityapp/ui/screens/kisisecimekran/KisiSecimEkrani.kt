package com.makak.learnactivityapp.ui.screens.kisisecimekran

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
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.repository.PersonRepository
import com.makak.learnactivityapp.database.repository.BlockRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import com.makak.learnactivityapp.ui.screens.ödemeekran.PaymentMemory
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun KisiSecimEkrani(
    navController: NavController,
    siteName: String,
    selectedMonth: String,
    selectedBlock: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val siteRepository = remember { SiteRepository(database.siteDao()) }
    val blockRepository = remember { BlockRepository(database.blockDao()) }
    val personRepository = remember { PersonRepository(database.personDao()) }

    // State
    var site by remember { mutableStateOf<Site?>(null) }
    var block by remember { mutableStateOf<Block?>(null) }
    var people by remember { mutableStateOf<List<Person>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load site, block and people on first composition
    LaunchedEffect(siteName, selectedMonth, selectedBlock) {
        scope.launch {
            try {
                // Find site by name
                val allSites = siteRepository.getAllSites()
                val foundSite = allSites.find { it.name == siteName }

                if (foundSite != null) {
                    site = foundSite

                    // Find block by site and name
                    val siteBlocks = blockRepository.getBlocksBySiteId(foundSite.id)
                    val foundBlock = siteBlocks.find { it.name == selectedBlock }

                    if (foundBlock != null) {
                        block = foundBlock
                        // People are block-based!
                        people = personRepository.getPeopleByBlockId(foundBlock.id)
                    } else {
                        errorMessage = "Blok bulunamadı"
                    }
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

    KisiSecimEkraniContent(
        siteName = siteName,
        selectedMonth = selectedMonth,
        selectedBlock = selectedBlock,
        people = people,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onPersonSelected = { person ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            val encodedMonth = URLEncoder.encode(selectedMonth, StandardCharsets.UTF_8.toString())
            val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
            val encodedPerson = URLEncoder.encode(person, StandardCharsets.UTF_8.toString())
            navController.navigate("screen5/$encodedSiteName/$encodedMonth/$encodedBlock/$encodedPerson")
        },
        onAddPersonClick = { showAddDialog = true }
    )

    // Add Person Dialog
    if (showAddDialog && block != null) {
        AddPersonDialog(
            onDismiss = { showAddDialog = false },
            onPersonAdd = { personName ->
                scope.launch {
                    try {
                        // Check if person already exists in this block
                        if (personRepository.isPersonNameExists(block!!.id, personName)) {
                            errorMessage = "Bu kişi adı zaten mevcut"
                            showAddDialog = false
                            return@launch
                        }

                        // Add new person to block
                        val newPerson = Person(
                            blockId = block!!.id,
                            name = personName
                        )
                        personRepository.insertPerson(newPerson)

                        // Refresh people list
                        people = personRepository.getPeopleByBlockId(block!!.id)
                        showAddDialog = false
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Kişi eklenirken hata oluştu"
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
fun KisiSecimEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Kasım 2025",
    selectedBlock: String = "2B",
    people: List<Person> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onPersonSelected: (String) -> Unit = {},
    onAddPersonClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddPersonClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Kişi Ekle",
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
                text = if (people.isEmpty() && !isLoading) "Kişi eklemek için + butonuna tıklayın" else "Kişi seçin",
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
                // People Selection Section
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp)
                ) {
                    items(people) { person ->
                        val paymentKey = "$siteName-$selectedMonth-$selectedBlock-${person.name}"
                        val isSaved = PaymentMemory.isSaved(paymentKey)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPersonSelected(person.name) },
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
                                    text = person.name,
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
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun KisiSecimEkraniPreview() {
    LearnactivityappTheme {
        KisiSecimEkraniContent(
            siteName = "Nezihpark Sitesi",
            selectedMonth = "Kasım 2025",
            selectedBlock = "2B",
            people = listOf(
                Person(1, 1, "Ali Yılmaz", 0),     // (id, blockId, name, createdAt)
                Person(2, 1, "Mehmet Demir", 0),
                Person(3, 1, "Ayşe Kaya", 0)
            )
        )
    }
}