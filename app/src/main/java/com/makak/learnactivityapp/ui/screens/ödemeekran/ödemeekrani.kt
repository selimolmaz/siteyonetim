package com.makak.learnactivityapp.ui.screens.ödemeekran

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
import androidx.navigation.NavController
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme

// Dummy hafıza sistemi
object PaymentMemory {
    private val payments = mutableMapOf<String, PaymentData>()

    data class PaymentData(
        val paidAmount: String = "",
        val willPayAmount: String = "",
        val isPaidEnabled: Boolean = false,
        val isWillPayEnabled: Boolean = false
    )

    fun savePayment(key: String, data: PaymentData) {
        payments[key] = data
    }

    fun removePayment(key: String) {
        payments.remove(key)
    }

    fun getPayment(key: String): PaymentData? {
        return payments[key]
    }

    fun isSaved(key: String): Boolean {
        val payment = payments[key]
        return if (payment != null) {
            // Herhangi bir radio button aktif ve tutar girilmiş
            (payment.isPaidEnabled && payment.paidAmount.isNotBlank() && payment.paidAmount != "0") ||
                    (payment.isWillPayEnabled && payment.willPayAmount.isNotBlank() && payment.willPayAmount != "0")
        } else {
            false
        }
    }

    // Blokta tüm kişiler durumları kaydedildi mi kontrol et
    fun isBlockFullyPaid(siteName: String, selectedMonth: String, selectedBlock: String): Boolean {
        val people = listOf("A kişisi", "B kişisi", "C kişisi") // Aynı liste Screen4'te olduğu gibi
        return people.all { person ->
            val paymentKey = "$siteName-$selectedMonth-$selectedBlock-$person"
            isSaved(paymentKey)
        }
    }

    // Ay tamamen tamamlandı mı kontrol et (tüm bloklar tamamlandı mı)
    fun isMonthFullyPaid(siteName: String, selectedMonth: String): Boolean {
        val blocks = listOf("1A", "2B", "3C", "4D", "5E") // Aynı liste Screen3'te olduğu gibi
        return blocks.all { block ->
            isBlockFullyPaid(siteName, selectedMonth, block)
        }
    }
}

@Composable
fun Screen5(
    navController: NavController,
    siteName: String,
    selectedMonth: String,
    selectedBlock: String,
    selectedPerson: String
) {
    Screen5Content(
        siteName = siteName,
        selectedMonth = selectedMonth,
        selectedBlock = selectedBlock,
        selectedPerson = selectedPerson,
        onSaveClick = {
            navController.popBackStack()
        }
    )
}

@Composable
fun Screen5Content(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Kasım 2025",
    selectedBlock: String = "2B",
    selectedPerson: String = "B Kişisi",
    onSaveClick: () -> Unit = {}
) {
    val paymentKey = "$siteName-$selectedMonth-$selectedBlock-$selectedPerson"
    val existingPayment = PaymentMemory.getPayment(paymentKey)

    var paidAmount by remember { mutableStateOf(existingPayment?.paidAmount ?: "") }
    var willPayAmount by remember { mutableStateOf(existingPayment?.willPayAmount ?: "") }
    var isPaidEnabled by remember { mutableStateOf(existingPayment?.isPaidEnabled ?: false) }
    var isWillPayEnabled by remember { mutableStateOf(existingPayment?.isWillPayEnabled ?: false) }

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
                    text = selectedPerson,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 32.dp)
                )
            }

            // Sıfırla Button (sadece kayıtlı veri varsa göster)
            val existingData = PaymentMemory.getPayment(paymentKey)
            if (existingData != null) {
                Button(
                    onClick = {
                        // Veriyi hafızadan sil
                        PaymentMemory.removePayment(paymentKey)
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
                // Veriyi hafızaya kaydet
                PaymentMemory.savePayment(
                    paymentKey,
                    PaymentMemory.PaymentData(
                        paidAmount = if (isPaidEnabled) paidAmount else "0", // Seçilmezse 0
                        willPayAmount = if (isWillPayEnabled) willPayAmount else "0", // Seçilmezse 0
                        isPaidEnabled = isPaidEnabled,
                        isWillPayEnabled = isWillPayEnabled
                    )
                )
                onSaveClick()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            enabled = isPaidEnabled || isWillPayEnabled // Sadece radio button seçimi yeterli
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
fun Screen5Preview() {
    LearnactivityappTheme {
        Screen5Content()
    }
}