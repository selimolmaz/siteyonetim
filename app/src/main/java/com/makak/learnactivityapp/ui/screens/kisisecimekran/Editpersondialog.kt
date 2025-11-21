package com.makak.learnactivityapp.ui.screens.kisisecimekran

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
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme

@Composable
fun EditPersonDialog(
    person: Person,
    onDismiss: () -> Unit,
    onPersonUpdate: (Person, String) -> Unit,
    onPersonDelete: (Person) -> Unit
) {
    var personName by remember { mutableStateOf(person.name) }
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
                    text = "Kişi Düzenle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Person Name Input
                OutlinedTextField(
                    value = personName,
                    onValueChange = { personName = it },
                    label = { Text("Kişi Adı") },
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
                            if (personName.trim().isNotBlank()) {
                                isLoading = true
                                onPersonUpdate(person, personName.trim())
                            }
                        },
                        enabled = personName.trim().isNotBlank() && !isLoading,
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
            title = { Text("Kişi Silme") },
            text = {
                Text("'${person.name}' kişisini silmek istediğinizden emin misiniz?\n\nBu işlem geri alınamaz ve kişiye ait tüm ödeme kayıtları da silinecektir.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onPersonDelete(person)
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
fun EditPersonDialogPreview() {
    LearnactivityappTheme {
        EditPersonDialog(
            person = Person(1, 1, "Ali Yılmaz", 0),
            onDismiss = {},
            onPersonUpdate = { _, _ -> },
            onPersonDelete = {}
        )
    }
}