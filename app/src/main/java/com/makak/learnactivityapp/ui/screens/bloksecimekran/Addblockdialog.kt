package com.makak.learnactivityapp.ui.screens.bloksecimekran

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
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme

@Composable
fun AddBlockDialog(
    onDismiss: () -> Unit,
    onBlockAdd: (String) -> Unit
) {
    var blockName by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

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
                    text = "Yeni Blok Ekle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Block Name Input
                OutlinedTextField(
                    value = blockName,
                    onValueChange = { blockName = it },
                    label = { Text("Blok Adı") },
                    placeholder = { Text("Örn: 1A, 2B, 3C") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    singleLine = true,
                    enabled = !isLoading
                )

                // Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("İptal")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Save Button
                    Button(
                        onClick = {
                            if (blockName.trim().isNotBlank()) {
                                isLoading = true
                                onBlockAdd(blockName.trim())
                            }
                        },
                        enabled = blockName.trim().isNotBlank() && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                        } else {
                            Text("Kaydet")
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun AddBlockDialogPreview() {
    LearnactivityappTheme {
        AddBlockDialog(
            onDismiss = {},
            onBlockAdd = {}
        )
    }
}