package com.makak.learnactivityapp.ui.screens.sitesecimekran

import androidx.compose.foundation.clickable
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
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SiteSecimEkrani(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val repository = remember { SiteRepository(database.siteDao()) }

    // State
    var sites by remember { mutableStateOf<List<Site>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedSiteForEdit by remember { mutableStateOf<Site?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load sites on first composition
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                sites = repository.getAllSites()
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Site listesi yüklenirken hata oluştu"
                isLoading = false
            }
        }
    }

    SiteSecimEkraniContent(
        sites = sites,
        isLoading = isLoading,
        errorMessage = errorMessage,
        onItemClick = { siteName ->
            val encodedSiteName = URLEncoder.encode(siteName, StandardCharsets.UTF_8.toString())
            navController.navigate("screen2/$encodedSiteName")
        },
        onItemLongClick = { site ->
            selectedSiteForEdit = site
            showEditDialog = true
        },
        onAddSiteClick = { showAddDialog = true }
    )

    // Add Site Dialog
    if (showAddDialog) {
        AddSiteDialog(
            onDismiss = { showAddDialog = false },
            onSiteAdd = { siteName ->
                scope.launch {
                    try {
                        // Check if site name already exists
                        if (repository.isSiteNameExists(siteName)) {
                            errorMessage = "Bu site adı zaten mevcut"
                            showAddDialog = false
                            return@launch
                        }

                        // Add new site
                        val newSite = Site(name = siteName)
                        repository.insertSite(newSite)

                        // Refresh sites list
                        sites = repository.getAllSites()
                        showAddDialog = false
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Site eklenirken hata oluştu"
                        showAddDialog = false
                    }
                }
            }
        )
    }

    // Edit Site Dialog
    if (showEditDialog && selectedSiteForEdit != null) {
        EditSiteDialog(
            site = selectedSiteForEdit!!,
            onDismiss = {
                showEditDialog = false
                selectedSiteForEdit = null
            },
            onSiteUpdate = { site, newName ->
                scope.launch {
                    try {
                        // Check if new site name already exists (excluding current site)
                        if (repository.isSiteNameExistsForUpdate(newName, site.id)) {
                            errorMessage = "Bu site adı zaten mevcut"
                            showEditDialog = false
                            selectedSiteForEdit = null
                            return@launch
                        }

                        // Update site
                        val updatedSite = site.copy(name = newName)
                        repository.updateSite(updatedSite)

                        // Refresh sites list
                        sites = repository.getAllSites()
                        showEditDialog = false
                        selectedSiteForEdit = null
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Site güncellenirken hata oluştu"
                        showEditDialog = false
                        selectedSiteForEdit = null
                    }
                }
            },
            onSiteDelete = { site ->
                scope.launch {
                    try {
                        repository.deleteSite(site.id)

                        // Refresh sites list
                        sites = repository.getAllSites()
                        showEditDialog = false
                        selectedSiteForEdit = null
                        errorMessage = null

                    } catch (e: Exception) {
                        errorMessage = "Site silinirken hata oluştu"
                        showEditDialog = false
                        selectedSiteForEdit = null
                    }
                }
            }
        )
    }

    // Show error message if exists
    errorMessage?.let { message ->
        LaunchedEffect(message) {
            // Clear error message after 3 seconds
            kotlinx.coroutines.delay(3000)
            errorMessage = null
        }
    }
}

@Composable
fun SiteSecimEkraniContent(
    sites: List<Site> = emptyList(),
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onItemClick: (String) -> Unit = {},
    onItemLongClick: (Site) -> Unit = {},
    onAddSiteClick: () -> Unit = {}
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSiteClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Site Ekle",
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
                text = "Site Seçimi",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            Text(
                text = if (sites.isEmpty() && !isLoading) "Site eklemek için + butonuna tıklayın" else "Site seçin (uzun basarak düzenleyebilirsiniz)",
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
                // Sites List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(sites) { site ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .pointerInput(Unit) {
                                    detectTapGestures(
                                        onTap = { onItemClick(site.name) },
                                        onLongPress = { onItemLongClick(site) }
                                    )
                                },
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
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
                                        text = site.name,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Normal
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
                                    tint = Color.Gray
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
fun SiteSecimEkraniPreview() {
    LearnactivityappTheme {
        SiteSecimEkraniContent(
            sites = listOf(
                Site(1, "Nezihpark Sitesi", 0),
                Site(2, "Bahçeşehir Sitesi", 0)
            ),
            onItemLongClick = {}
        )
    }
}