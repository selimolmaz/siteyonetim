package com.makak.learnactivityapp.ui.screens.odemeekran.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makak.learnactivityapp.database.entities.Payment
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.PaymentRepository
import com.makak.learnactivityapp.database.repository.PersonRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class OdemeViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val monthRepository: MonthRepository,
    private val personRepository: PersonRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteName: String = savedStateHandle.get<String>("siteName")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    } ?: ""

    private val selectedMonthId: Long = savedStateHandle.get<Long>("selectedMonthId") ?: 0L
    private val selectedPersonId: Long = savedStateHandle.get<Long>("selectedPersonId") ?: 0L

    private val _uiState = MutableStateFlow(OdemeUiState())
    val uiState: StateFlow<OdemeUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allSites = siteRepository.getAllSites()
                val foundSite = allSites.find { it.name == siteName }

                if (foundSite != null) {
                    val foundMonth = monthRepository.getMonthById(selectedMonthId)

                    if (foundMonth != null) {
                        val foundPerson = personRepository.getPersonById(selectedPersonId)

                        if (foundPerson != null) {
                            _uiState.update {
                                it.copy(
                                    site = foundSite,
                                    month = foundMonth,
                                    person = foundPerson,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }

                            // İlk data yüklendikten sonra reactive updates başlat
                            observePayment(foundPerson.id, foundMonth.id)
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Kişi bulunamadı"
                                )
                            }
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = "Ay bulunamadı"
                            )
                        }
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Site bulunamadı"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Veriler yüklenirken hata oluştu: ${e.message}"
                    )
                }
            }
        }
    }

    // Reaktif: Bu kişinin bu aydaki ödemesini dinle
    private fun observePayment(personId: Long, monthId: Long) {
        viewModelScope.launch {
            paymentRepository.observePaymentByPersonAndMonth(personId, monthId)
                .collect { payment ->
                    // Ödeme değiştiğinde otomatik güncelleme
                    _uiState.update { it.copy(currentPayment = payment) }
                }
        }
    }

    fun savePayment(
        paidAmount: String,
        willPayAmount: String,
        isPaidEnabled: Boolean,
        isWillPayEnabled: Boolean
    ) {
        viewModelScope.launch {
            try {
                val person = _uiState.value.person ?: return@launch
                val month = _uiState.value.month ?: return@launch

                val newPayment = Payment(
                    id = _uiState.value.currentPayment?.id ?: 0,
                    personId = person.id,
                    monthId = month.id,
                    paidAmount = if (isPaidEnabled) paidAmount else "0",
                    willPayAmount = if (isWillPayEnabled) willPayAmount else "0",
                    isPaidEnabled = isPaidEnabled,
                    isWillPayEnabled = isWillPayEnabled,
                    updatedAt = System.currentTimeMillis()
                )

                paymentRepository.savePayment(newPayment)
                // Manuel update YOK - observePayment otomatik günceller!
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ödeme kaydedilirken hata oluştu: ${e.message}") }
            }
        }
    }

    fun resetPayment() {
        viewModelScope.launch {
            try {
                val person = _uiState.value.person ?: return@launch
                val month = _uiState.value.month ?: return@launch

                paymentRepository.removePaymentByPersonAndMonth(person.id, month.id)
                // Manuel update YOK - observePayment otomatik null döner!
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ödeme silinirken hata oluştu: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}