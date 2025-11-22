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
        loadData()
    }

    private fun loadData() {
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
                            val people = personRepository.getPeopleByBlockId(foundBlock.id)

                            val statusMap = mutableMapOf<Long, Boolean>()
                            people.forEach { person ->
                                statusMap[person.id] = paymentRepository.isPaymentSaved(person.id, foundMonth.id)
                            }

                            _uiState.update {
                                it.copy(
                                    site = foundSite,
                                    month = foundMonth,
                                    block = foundBlock,
                                    people = people,
                                    peopleStatus = statusMap,
                                    isLoading = false,
                                    errorMessage = null
                                )
                            }
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
                loadData()
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
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Kişi güncellenirken hata oluştu") }
            }
        }
    }

    fun deletePerson(personId: Long) {
        viewModelScope.launch {
            try {
                personRepository.deletePerson(personId)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Kişi silinirken hata oluştu: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}