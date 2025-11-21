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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.makak.learnactivityapp.database.database.AppDatabase
import com.makak.learnactivityapp.database.entities.Payment
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.repository.PaymentRepository
import com.makak.learnactivityapp.database.repository.PersonRepository
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import com.makak.learnactivityapp.ui.theme.LearnactivityappTheme
import kotlinx.coroutines.launch

@Composable
fun OdemeEkrani(
    navController: NavController,
    siteName: String,
    selectedMonthId: Long,
    selectedBlock: String,
    selectedPersonId: Long
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Database setup
    val database = remember { AppDatabase.getDatabase(context) }
    val siteRepository = remember { SiteRepository(database.siteDao()) }
    val monthRepository = remember { MonthRepository(database.monthDao()) }
    val personRepository = remember { PersonRepository(database.personDao()) }
    val paymentRepository = remember { PaymentRepository(database.paymentDao()) }

    // State
    var site by remember { mutableStateOf<Site?>(null) }
    var month by remember { mutableStateOf<Month?>(null) }
    var person by remember { mutableStateOf<Person?>(null) }
    var currentPayment by remember { mutableStateOf<Payment?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Load site, month, and person data
    LaunchedEffect(siteName, selectedMonthId, selectedBlock, selectedPersonId) {
        scope.launch {
            try {
                println("🔍 OdemeEkrani Debug - MonthID: $selectedMonthId, PersonID: $selectedPersonId")

                // Find site
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

                        // Find person by ID directly
                        val foundPerson = personRepository.getPersonById(selectedPersonId)
                        println("🔍 Searching person with ID: $selectedPersonId")
                        println("🔍 Found person: $foundPerson")

                        if (foundPerson != null) {
                            person = foundPerson
                            println("✅ Person found: ${foundPerson.name} (ID: ${foundPerson.id})")

                            // Get existing payment
                            currentPayment = paymentRepository.getPaymentByPersonAndMonth(
                                foundPerson.id, foundMonth.id
                            )
                        } else {
                            errorMessage = "Kişi bulunamadı (ID: $selectedPersonId)"
                            println("❌ Person not found with ID: $selectedPersonId")
                            // Person bulunamazsa geri dön
                            navController.popBackStack()
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
                println("❌ Exception in OdemeEkrani: ${e.message}")
                e.printStackTrace()
                isLoading = false
            }
        }
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else if (errorMessage != null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = errorMessage!!,
                color = Color.Red,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    } else if (site != null && month != null && person != null) {
        // Safe copies to avoid smart cast issues
        val safeMonth = month!!
        val safePerson = person!!

        OdemeEkraniContent(
            siteName = siteName,
            selectedMonth = safeMonth.name,
            selectedBlock = selectedBlock,
            selectedPersonName = safePerson.name,
            existingPayment = currentPayment,
            onSaveClick = { paidAmount, willPayAmount, isPaidEnabled, isWillPayEnabled ->
                scope.launch {
                    try {
                        val newPayment = Payment(
                            id = currentPayment?.id ?: 0,
                            personId = safePerson.id,
                            monthId = safeMonth.id,
                            paidAmount = if (isPaidEnabled) paidAmount else "0",
                            willPayAmount = if (isWillPayEnabled) willPayAmount else "0",
                            isPaidEnabled = isPaidEnabled,
                            isWillPayEnabled = isWillPayEnabled,
                            updatedAt = System.currentTimeMillis()
                        )

                        paymentRepository.savePayment(newPayment)
                        navController.popBackStack()
                    } catch (e: Exception) {
                        errorMessage = "Ödeme kaydedilirken hata oluştu: ${e.message}"
                    }
                }
            },
            onResetClick = {
                if (currentPayment != null) {
                    scope.launch {
                        try {
                            paymentRepository.removePaymentByPersonAndMonth(safePerson.id, safeMonth.id)
                            currentPayment = null
                        } catch (e: Exception) {
                            errorMessage = "Ödeme silinirken hata oluştu: ${e.message}"
                        }
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
fun OdemeEkraniContent(
    siteName: String = "Nezihpark Sitesi",
    selectedMonth: String = "Kasım 2025",
    selectedBlock: String = "2B",
    selectedPersonName: String = "B Kişisi",
    existingPayment: Payment? = null,
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
fun OdemeEkraniPreview() {
    LearnactivityappTheme {
        OdemeEkraniContent()
    }
}