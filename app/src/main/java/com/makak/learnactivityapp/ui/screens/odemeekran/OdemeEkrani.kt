package com.makak.learnactivityapp.ui.screens.odemeekran

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.makak.learnactivityapp.ui.screens.odemeekran.viewmodel.OdemeViewModel
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.delay

@Composable
fun OdemeEkrani(
    navController: NavController,
    siteName: String,
    selectedMonthId: Long,
    selectedBlock: String,
    selectedPersonId: Long,
    viewModel: OdemeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (uiState.errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = uiState.errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else if (uiState.site != null && uiState.month != null && uiState.person != null) {
        OdemeEkraniContent(
            siteName = siteName,
            selectedMonth = uiState.month!!.name,
            selectedBlock = selectedBlock,
            selectedPersonName = uiState.person!!.name,
            existingPayment = uiState.currentPayment,
            onSaveClick = { paidAmount, willPayAmount, isPaidEnabled, isWillPayEnabled ->
                viewModel.savePayment(paidAmount, willPayAmount, isPaidEnabled, isWillPayEnabled)
                navController.popBackStack()
            },
            onResetClick = {
                viewModel.resetPayment()
            }
        )
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
fun OdemeEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Kasım 2025",
    selectedBlock: String = "2B",
    selectedPersonName: String = "B Kişisi",
    existingPayment: com.makak.learnactivityapp.database.entities.Payment? = null,
    onSaveClick: (String, String, Boolean, Boolean) -> Unit = { _, _, _, _ -> },
    onResetClick: () -> Unit = {}
) {
    var paidAmount by remember { mutableStateOf(existingPayment?.paidAmount ?: "") }
    var willPayAmount by remember { mutableStateOf(existingPayment?.willPayAmount ?: "") }
    var isPaidEnabled by remember { mutableStateOf(existingPayment?.isPaidEnabled ?: false) }
    var isWillPayEnabled by remember { mutableStateOf(existingPayment?.isWillPayEnabled ?: false) }

    // Reset state when existingPayment changes
    LaunchedEffect(existingPayment) {
        paidAmount = existingPayment?.paidAmount ?: ""
        willPayAmount = existingPayment?.willPayAmount ?: ""
        isPaidEnabled = existingPayment?.isPaidEnabled ?: false
        isWillPayEnabled = existingPayment?.isWillPayEnabled ?: false
    }

    // Number only filter function
    fun filterNumbers(text: String): String {
        return text.filter { it.isDigit() || it == '.' || it == ',' }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section with Sıfırla Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
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
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Text(
                    text = selectedPersonName,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            // Sıfırla Button (sadece kayıtlı veri varsa göster)
            if (existingPayment != null) {
                Button(
                    onClick = {
                        onResetClick()
                        // State'leri sıfırla
                        paidAmount = ""
                        willPayAmount = ""
                        isPaidEnabled = false
                        isWillPayEnabled = false
                    },
                    modifier = Modifier
                        .width(80.dp)
                        .height(36.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFEB3B)
                    )
                ) {
                    Text(
                        text = "Sıfırla",
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                }
            }
        }

        // Ödedi Section
        Column(
            modifier = Modifier.padding(bottom = 24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                RadioButton(
                    selected = isPaidEnabled,
                    onClick = { isPaidEnabled = !isPaidEnabled }
                )
                Text(
                    text = "Ödedi",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    OutlinedTextField(
                        value = paidAmount,
                        onValueChange = { newValue ->
                            paidAmount = filterNumbers(newValue)
                        },
                        enabled = isPaidEnabled,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF4CAF50)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        placeholder = {
                            Text(
                                text = "0,00",
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
                Text(
                    text = "TL",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        // Ödeyecek Section
        Column(
            modifier = Modifier.padding(bottom = 48.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                RadioButton(
                    selected = isWillPayEnabled,
                    onClick = { isWillPayEnabled = !isWillPayEnabled }
                )
                Text(
                    text = "Ödeyecek",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFF44336)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    OutlinedTextField(
                        value = willPayAmount,
                        onValueChange = { newValue ->
                            willPayAmount = filterNumbers(newValue)
                        },
                        enabled = isWillPayEnabled,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF44336)),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            disabledTextColor = Color.White,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            disabledBorderColor = Color.Transparent
                        ),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        ),
                        placeholder = {
                            Text(
                                text = "0,00",
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    )
                }
                Text(
                    text = "TL",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Kaydet Button
        Button(
            onClick = {
                onSaveClick(paidAmount, willPayAmount, isPaidEnabled, isWillPayEnabled)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = isPaidEnabled || isWillPayEnabled
        ) {
            Text(
                text = "Kaydet",
                fontSize = 16.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OdemeEkraniPreview() {
    LearnactivityappTheme {
        OdemeEkraniContent()
    }
}