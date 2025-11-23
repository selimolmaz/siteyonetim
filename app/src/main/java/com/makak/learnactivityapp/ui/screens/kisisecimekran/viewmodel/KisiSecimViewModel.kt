package com.makak.learnactivityapp.ui.screens.kisisecimekran.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.database.repository.BlockRepository
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.PaymentRepository
import com.makak.learnactivityapp.database.repository.PersonRepository
import com.makak.learnactivityapp.database.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import javax.inject.Inject

@HiltViewModel
class KisiSecimViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val monthRepository: MonthRepository,
    private val blockRepository: BlockRepository,
    private val personRepository: PersonRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteName: String = savedStateHandle.get<String>("siteName")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    } ?: ""

    private val selectedMonthId: Long = savedStateHandle.get<Long>("selectedMonthId") ?: 0L

    private val selectedBlock: String = savedStateHandle.get<String>("selectedBlock")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    } ?: ""

    private val _uiState = MutableStateFlow(KisiSecimUiState())
    val uiState: StateFlow<KisiSecimUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
        // Reactive updates başlatılacak loadInitialData içinde
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
                        val siteBlocks = blockRepository.getBlocksBySiteId(foundSite.id)
                        val foundBlock = siteBlocks.find { it.name == selectedBlock }

                        if (foundBlock != null) {
                            _uiState.update {
                                it.copy(
                                    site = foundSite,
                                    month = foundMonth,
                                    block = foundBlock,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }

                            // İlk data yüklendikten sonra reactive updates başlat
                            observePeopleAndPayments(foundBlock.id, foundMonth.id)
                        } else {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    errorMessage = "Blok bulunamadı"
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

    // Reactive updates - Kişiler VE ödemeler birlikte dinleniyor
    private fun observePeopleAndPayments(blockId: Long, monthId: Long) {
        viewModelScope.launch {
            // İki Flow'u birleştir: people + payments
            combine(
                personRepository.observePeopleByBlockId(blockId),
                paymentRepository.observePaymentsByMonth(monthId)
            ) { people, payments ->
                // Her kişi için ödeme durumunu hesapla
                val statusMap = mutableMapOf<Long, Boolean>()
                people.forEach { person ->
                    val hasPayment = payments.any { payment ->
                        payment.personId == person.id &&
                                ((payment.isPaidEnabled && payment.paidAmount.isNotBlank() && payment.paidAmount != "0") ||
                                        (payment.isWillPayEnabled && payment.willPayAmount.isNotBlank() && payment.willPayAmount != "0"))
                    }
                    statusMap[person.id] = hasPayment
                }

                Pair(people, statusMap)
            }.collect { (people, statusMap) ->
                // Otomatik güncelleme - Her değişiklikte UI yenilenir
                _uiState.update {
                    it.copy(
                        people = people,
                        peopleStatus = statusMap
                    )
                }
            }
        }
    }

    fun addPerson(personName: String) {
        viewModelScope.launch {
            try {
                val block = _uiState.value.block ?: return@launch

                if (personRepository.isPersonNameExists(block.id, personName)) {
                    _uiState.update { it.copy(errorMessage = "Bu kişi adı zaten mevcut") }
                    return@launch
                }

                val newPerson = Person(
                    blockId = block.id,
                    name = personName
                )
                personRepository.insertPerson(newPerson)
                // Manuel refresh YOK - observePeopleAndPayments otomatik günceller!
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Kişi eklenirken hata oluştu") }
            }
        }
    }

    fun updatePerson(person: Person, newName: String) {
        viewModelScope.launch {
            try {
                val block = _uiState.value.block ?: return@launch

                if (personRepository.isPersonNameExistsForUpdate(block.id, newName, person.id)) {
                    _uiState.update { it.copy(errorMessage = "Bu kişi adı zaten mevcut") }
                    return@launch
                }

                val updatedPerson = person.copy(name = newName)
                personRepository.updatePerson(updatedPerson)
                // Manuel refresh YOK - observePeopleAndPayments otomatik günceller!
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Kişi güncellenirken hata oluştu") }
            }
        }
    }

    fun deletePerson(personId: Long) {
        viewModelScope.launch {
            try {
                personRepository.deletePerson(personId)
                // Manuel refresh YOK - observePeopleAndPayments otomatik günceller!
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Kişi silinirken hata oluştu: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}