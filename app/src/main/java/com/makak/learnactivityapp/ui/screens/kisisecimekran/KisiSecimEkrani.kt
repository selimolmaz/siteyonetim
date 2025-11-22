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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.ui.screens.kisisecimekran.viewmodel.KisiSecimViewModel
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun KisiSecimEkrani(
    navController: NavController,
    siteName: String,
    selectedMonthId: Long,
    selectedBlock: String,
    viewModel: KisiSecimViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedPersonForEdit by remember { mutableStateOf<Person?>(null) }

    KisiSecimEkraniContent(
        siteName = siteName,
        selectedMonth = uiState.month?.name ?: "",
        selectedBlock = selectedBlock,
        people = uiState.people,
        peopleStatus = uiState.peopleStatus,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
        onPersonSelected = { person ->
            uiState.month?.let { monthData ->
                val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
                val encodedBlock = URLEncoder.encode(selectedBlock, StandardCharsets.UTF_8.toString())
                val navigationRoute = "screen5/$encodedSiteName/${monthData.id}/$encodedBlock/${person.id}"
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
    if (showAddDialog) {
        AddPersonDialog(
            onDismiss = { showAddDialog = false },
            onPersonAdd = { personName ->
                viewModel.addPerson(personName)
                showAddDialog = false
            }
        )
    }

    // Edit Person Dialog
    selectedPersonForEdit?.let { person ->
        if (showEditDialog) {
            EditPersonDialog(
                person = person,
                onDismiss = {
                    showEditDialog = false
                    selectedPersonForEdit = null
                },
                onPersonUpdate = { person, newName ->
                    viewModel.updatePerson(person, newName)
                    showEditDialog = false
                    selectedPersonForEdit = null
                },
                onPersonDelete = { person ->
                    viewModel.deletePerson(person.id)
                    showEditDialog = false
                    selectedPersonForEdit = null
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
                        key = { person -> person.id }
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
            )
        )
    }
}