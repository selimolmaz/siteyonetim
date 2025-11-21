package com.makak.learnactivityapp.ui.screens.aysecimekran

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
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditMonthDialog(
    month: Month,
    onDismiss: () -> Unit,
    onMonthUpdate: (Month, Int, Int) -> Unit,
    onMonthDelete: (Month) -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(month.monthNumber) }
    var selectedYear by remember { mutableIntStateOf(month.year) }
    var isLoading by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val months = listOf(
        1 to "Ocak", 2 to "Şubat", 3 to "Mart", 4 to "Nisan",
        5 to "Mayıs", 6 to "Haziran", 7 to "Temmuz", 8 to "Ağustos",
        9 to "Eylül", 10 to "Ekim", 11 to "Kasım", 12 to "Aralık"
    )

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
                    text = "Ay Düzenle",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Month Selection
                Text(
                    text = "Ay:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                var expandedMonth by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedMonth,
                    onExpandedChange = { expandedMonth = !expandedMonth },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = months.find { it.first == selectedMonth }?.second ?: "",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMonth) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading
                    )

                    ExposedDropdownMenu(
                        expanded = expandedMonth,
                        onDismissRequest = { expandedMonth = false }
                    ) {
                        months.forEach { monthItem ->
                            DropdownMenuItem(
                                text = { Text(monthItem.second) },
                                onClick = {
                                    selectedMonth = monthItem.first
                                    expandedMonth = false
                                }
                            )
                        }
                    }
                }

                // Year Selection
                Text(
                    text = "Yıl:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                val years = (2020..2030).toList()
                var expandedYear by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedYear,
                    onExpandedChange = { expandedYear = !expandedYear },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = selectedYear.toString(),
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedYear) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        enabled = !isLoading
                    )

                    ExposedDropdownMenu(
                        expanded = expandedYear,
                        onDismissRequest = { expandedYear = false }
                    ) {
                        years.forEach { year ->
                            DropdownMenuItem(
                                text = { Text(year.toString()) },
                                onClick = {
                                    selectedYear = year
                                    expandedYear = false
                                }
                            )
                        }
                    }
                }

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
                            isLoading = true
                            onMonthUpdate(month, selectedMonth, selectedYear)
                        },
                        enabled = !isLoading,
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
            title = { Text("Ay Silme") },
            text = {
                Text("'${month.name}' ayını silmek istediğinizden emin misiniz?\n\nBu işlem geri alınamaz ve bu aya ait tüm blok, kişi ve ödeme kayıtları da silinecektir.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onMonthDelete(month)
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
fun EditMonthDialogPreview() {
    LearnactivityappTheme {
        EditMonthDialog(
            month = Month(1, 1, "Kasım 2025", 2025, 11, 0),
            onDismiss = {},
            onMonthUpdate = { _, _, _ -> },
            onMonthDelete = {}
        )
    }
}