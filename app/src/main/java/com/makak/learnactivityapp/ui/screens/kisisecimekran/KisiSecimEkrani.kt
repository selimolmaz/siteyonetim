package com.makak.learnactivityapp.ui.screens.kisisecimekran

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
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.repository.PersonRepository
import com.makak.learnactivityapp.database.repository.PaymentRepository
import com.makak.learnactivityapp.database.repository.BlockRepository
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun KisiSecimEkrani(
    navController: NavController,
    siteName: String,
    selectedMonthId: Long,
    selectedBlock: String
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val siteRepository = remember { SiteRepository(database.siteDao()) }
    val monthRepository = remember { MonthRepository(database.monthDao()) }
    val blockRepository = remember { BlockRepository(database.blockDao()) }
    val personRepository = remember { PersonRepository(database.personDao()) }
    val paymentRepository = remember { PaymentRepository(database.paymentDao()) }

    // State
    var site by remember { mutableStateOf<Site?>(null) }
    var month by remember { mutableStateOf<Month?>(null) }
    var block by remember { mutableStateOf<Block?>(null) }
    var people by remember { mutableStateOf<List<Person>>(emptyList()) }
    var peopleStatus by remember { mutableStateOf<Map<Long, Boolean>>(emptyMap()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPersonForEdit by remember { mutableStateOf<Person?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load site, month, block and people on first composition
    LaunchedEffect(siteName, selectedMonthId, selectedBlock) {
        scope.launch {
            try {
                println("🔍 KisiSecimEkrani Debug - MonthID: $selectedMonthId")

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

                        // Find block by site and name
                        val siteBlocks = blockRepository.getBlocksBySiteId(foundSite.id)
                        val foundBlock = siteBlocks.find { it.name == selectedBlock }

                        if (foundBlock != null) {
                            block = foundBlock
                            // People are block-based!
                            people = personRepository.getPeopleByBlockId(foundBlock.id)

                            // Check payment status for each person in this month
                            val statusMap = mutableMapOf<Long, Boolean>()
                            people.forEach { person ->
                                statusMap[person.id] = paymentRepository.isPaymentSaved(person.id, foundMonth.id)
                            }
                            peopleStatus = statusMap
                        } else {
                            errorMessage = "Blok bulunamadı"
                            println("❌ Block not found: $selectedBlock")
                        }
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
                println("❌ Exception in KisiSecimEkrani: ${e.message}")
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    KisiSecimEkraniContent(
        siteName = siteName,
        selectedMonth = month?.name ?: "",
        selectedBlock = selectedBlock,
        people = people,
        peopleStatus = peopleStatus,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onPersonSelected = { person ->
            month?.let { monthData ->
                println("🔍 KisiSecimEkrani - Selected person: ${person.name} (ID: ${person.id})")
                val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
                val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
                val navigationRoute = "screen5/$encodedSiteName/${monthData.id}/$encodedBlock/${person.id}"
                println("🔍 Navigation route: $navigationRoute")
                navController.navigate(navigationRoute)
            }
        },
        onPersonLongClick = { person ->
            selectedPersonForEdit = person
            showEditDialog = true
        },
        onAddPersonClick = { showAddDialog = true }
    )

    // Add Person Dialog
    if (showAddDialog && block != null && month != null) {
        // Safe copies to avoid smart cast issues
        val safeBlock = block!!
        val safeMonth = month!!

        AddPersonDialog(
            onDismiss = { showAddDialog = false },
            onPersonAdd = { personName ->
                scope.launch {
                    try {
                        // Check if person already exists in this block
                        if (personRepository.isPersonNameExists(safeBlock.id, personName)) {
                            errorMessage = "Bu kişi adı zaten mevcut"
                            showAddDialog = false
                            return@launch
                        }

                        // Add new person to block
                        val newPerson = Person(
                            blockId = safeBlock.id,
                            name = personName
                        )
                        personRepository.insertPerson(newPerson)

                        // Refresh people list and status
                        people = personRepository.getPeopleByBlockId(safeBlock.id)
                        val statusMap = mutableMapOf<Long, Boolean>()
                        people.forEach { person ->
                            statusMap[person.id] = paymentRepository.isPaymentSaved(person.id, safeMonth.id)
                        }
                        peopleStatus = statusMap
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

    // Edit Person Dialog
    if (showEditDialog && selectedPersonForEdit != null && block != null && month != null) {
        // Safe copies to avoid smart cast issues
        val safeBlock = block!!
        val safeMonth = month!!

        EditPersonDialog(
            person = selectedPersonForEdit!!,
            onDismiss = {
                showEditDialog = false
                selectedPersonForEdit = null
            },
            onPersonUpdate = { person, newName ->
                scope.launch {
                    try {
                        // Check if new person name already exists in this block (excluding current person)
                        if (personRepository.isPersonNameExistsForUpdate(safeBlock.id, newName, person.id)) {
                            errorMessage = "Bu kişi adı zaten mevcut"
                            showEditDialog = false
                            selectedPersonForEdit = null
                            return@launch
                        }

                        // Update person
                        val updatedPerson = person.copy(name = newName)
                        personRepository.updatePerson(updatedPerson)

                        // Refresh people list and status
                        people = personRepository.getPeopleByBlockId(safeBlock.id)
                        val statusMap = mutableMapOf<Long, Boolean>()
                        people.forEach { personItem ->
                            statusMap[personItem.id] = paymentRepository.isPaymentSaved(personItem.id, safeMonth.id)
                        }
                        peopleStatus = statusMap
                        showEditDialog = false
                        selectedPersonForEdit = null
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Kişi güncellenirken hata oluştu"
                        showEditDialog = false
                        selectedPersonForEdit = null
                    }
                }
            },
            onPersonDelete = { person ->
                scope.launch {
                    try {
                        println("🗑️ Deleting person: ${person.name} (ID: ${person.id})")

                        // Önce person'ı sil
                        personRepository.deletePerson(person.id)
                        println("✅ Person deleted successfully")

                        // State'i tamamen yenile - bu critical!
                        val refreshedPeople = personRepository.getPeopleByBlockId(safeBlock.id)
                        println("🔄 Refreshed people count: ${refreshedPeople.size}")
                        people = refreshedPeople

                        // Status map'ini de tamamen yeniden oluştur
                        val newStatusMap = mutableMapOf<Long, Boolean>()
                        refreshedPeople.forEach { personItem ->
                            newStatusMap[personItem.id] = paymentRepository.isPaymentSaved(personItem.id, safeMonth.id)
                        }
                        peopleStatus = newStatusMap

                        showEditDialog = false
                        selectedPersonForEdit = null
                        errorMessage = null

                    } catch (e: Exception) {
                        println("❌ Error deleting person: ${e.message}")
                        errorMessage = "Kişi silinirken hata oluştu: ${e.message}"
                        showEditDialog = false
                        selectedPersonForEdit = null
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
    peopleStatus: Map<Long, Boolean> = emptyMap(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onPersonSelected: (Person) -> Unit = {},
    onPersonLongClick: (Person) -> Unit = {},
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
                text = if (people.isEmpty() && !isLoading) "Kişi eklemek için + butonuna tıklayın" else "Kişi seçin (uzun basarak düzenleyebilirsiniz)",
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
                    items(
                        items = people,
                        key = { person -> person.id } // Bu çok önemli!
                    ) { person ->
                        val isSaved = peopleStatus[person.id] ?: false

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onPersonSelected(person) },
                                        onLongPress = { onPersonLongClick(person) }
                                    )
                                },
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
                                Column {
                                    Text(
                                        text = person.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal,
                                        color = if (isSaved) Color(0xFF2E7D32) else Color.Black
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
                Person(1, 1, "Ali Yılmaz", 0),
                Person(2, 1, "Mehmet Demir", 0),
                Person(3, 1, "Ayşe Kaya", 0)
            ),
            onPersonLongClick = {}
        )
    }
}