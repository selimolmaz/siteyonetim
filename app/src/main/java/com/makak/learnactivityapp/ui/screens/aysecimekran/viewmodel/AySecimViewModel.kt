package com.makak.learnactivityapp.ui.screens.aysecimekran.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.PaymentRepository
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
class AySecimViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val monthRepository: MonthRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteName: String = savedStateHandle.get<String>("siteName")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    } ?: ""

    private val _uiState = MutableStateFlow(AySecimUiState())
    val uiState: StateFlow<AySecimUiState> = _uiState.asStateFlow()

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
                    val months = monthRepository.getMonthsBySiteId(foundSite.id)

                    val statusMap = mutableMapOf<Long, Boolean>()
                    months.forEach { month ->
                        statusMap[month.id] = paymentRepository.isMonthFullyPaidInSite(foundSite.id, month.id)
                    }

                    _uiState.update {
                        it.copy(
                            site = foundSite,
                            months = months,
                            monthsStatus = statusMap,
                            isLoading = false,
                            errorMessage = null
                        )
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
                        errorMessage = "Veriler yüklenirken hata oluştu"
                    )
                }
            }
        }
    }

    fun addMonth(monthNumber: Int, year: Int) {
        viewModelScope.launch {
            try {
                val site = _uiState.value.site ?: return@launch

                val monthName = getMonthName(monthNumber, year)

                if (monthRepository.isMonthNameExists(site.id, monthName)) {
                    _uiState.update { it.copy(errorMessage = "Bu ay zaten mevcut") }
                    return@launch
                }

                val newMonth = Month(
                    siteId = site.id,
                    name = monthName,
                    year = year,
                    monthNumber = monthNumber
                )
                monthRepository.insertMonth(newMonth)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ay eklenirken hata oluştu") }
            }
        }
    }

    fun updateMonth(month: Month, newMonthNumber: Int, newYear: Int) {
        viewModelScope.launch {
            try {
                val site = _uiState.value.site ?: return@launch

                val newMonthName = getMonthName(newMonthNumber, newYear)

                if (monthRepository.isMonthNameExistsForUpdate(site.id, newMonthName, month.id)) {
                    _uiState.update { it.copy(errorMessage = "Bu ay zaten mevcut") }
                    return@launch
                }

                val updatedMonth = month.copy(
                    name = newMonthName,
                    year = newYear,
                    monthNumber = newMonthNumber
                )
                monthRepository.updateMonth(updatedMonth)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ay güncellenirken hata oluştu") }
            }
        }
    }

    fun deleteMonth(monthId: Long) {
        viewModelScope.launch {
            try {
                monthRepository.deleteMonth(monthId)
                loadData()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Ay silinirken hata oluştu") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun getMonthName(monthNumber: Int, year: Int): String {
        val months = listOf(
            "Ocak", "Şubat", "Mart", "Nisan",
            "Mayıs", "Haziran", "Temmuz", "Ağustos",
            "Eylül", "Ekim", "Kasım", "Aralık"
        )
        return "${months[monthNumber - 1]} $year"
    }
}