package com.makak.learnactivityapp.ui.screens.sitesecimekran

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
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.ui.screens.sitesecimekran.viewmodel.SiteViewModel
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun SiteSecimEkrani(
    navController: NavController,
    viewModel: SiteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedSiteForEdit by remember { mutableStateOf<Site?>(null) }

    SiteSecimEkraniContent(
        sites = uiState.sites,
        isLoading = uiState.isLoading,
        errorMessage = uiState.errorMessage,
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
                viewModel.addSite(siteName)
                showAddDialog = false
            }
        )
    }

    // Edit Site Dialog
    selectedSiteForEdit?.let { site ->
        if (showEditDialog) {
            EditSiteDialog(
                site = site,
                onDismiss = {
                    showEditDialog = false
                    selectedSiteForEdit = null
                },
                onSiteUpdate = { site, newName ->
                    viewModel.updateSite(site, newName)
                    showEditDialog = false
                    selectedSiteForEdit = null
                },
                onSiteDelete = { site ->
                    viewModel.deleteSite(site.id)
                    showEditDialog = false
                    selectedSiteForEdit = null
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
            )
        )
    }
}