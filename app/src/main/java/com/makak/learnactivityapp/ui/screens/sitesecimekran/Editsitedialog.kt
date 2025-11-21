package com.makak.learnactivityapp.ui.screens.sitesecimekran

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme

@Composable
fun EditSiteDialog(
    site: Site,
    onDismiss: () -> Unit,
    onSiteUpdate: (Site, String) -> Unit,
    onSiteDelete: (Site) -> Unit
) {
    var siteName by remember { mutableStateOf(site.name) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Site Düzenle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Site Name Input
                OutlinedTextField(
                    value = siteName,
                    onValueChange = { siteName = it },
                    label = { Text("Site Adı") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Delete Button
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        enabled = !isLoading,
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.Red
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Sil")
                    }

                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal")
                    }

                    // Update Button
                    Button(
                        onClick = {
                            if (siteName.trim().isNotBlank()) {
                                isLoading = true
                                onSiteUpdate(site, siteName.trim())
                            }
                        },
                        enabled = siteName.trim().isNotBlank() && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Güncelle")
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Site Silme") },
            text = { Text("'${site.name}' sitesini silmek istediğinizden emin misiniz? Bu işlem geri alınamaz.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onSiteDelete(site)
                    }
                ) {
                    Text("Sil", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("İptal")
                }
            }
        )
    }
}

@Preview
@Composable
fun EditSiteDialogPreview() {
    LearnactivityappTheme {
        EditSiteDialog(
            site = Site(1, "Nezihpark Sitesi", 0),
            onDismiss = {},
            onSiteUpdate = { _, _ -> },
            onSiteDelete = {}
        )
    }
}