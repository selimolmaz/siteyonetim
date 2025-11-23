package com.makak.learnactivityapp.ui.screens.bloksecimekran.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.database.repository.BlockRepository
import com.makak.learnactivityapp.database.repository.MonthRepository
import com.makak.learnactivityapp.database.repository.PaymentRepository
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
class BlokSecimViewModel @Inject constructor(
    private val siteRepository: SiteRepository,
    private val monthRepository: MonthRepository,
    private val blockRepository: BlockRepository,
    private val paymentRepository: PaymentRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val siteName: String = savedStateHandle.get<String>("siteName")?.let {
        URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
    } ?: ""

    private val selectedMonthId: Long = savedStateHandle.get<Long>("selectedMonthId") ?: 0L

    private val _uiState = MutableStateFlow(BlokSecimUiState())
    val uiState: StateFlow<BlokSecimUiState> = _uiState.asStateFlow()

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
                        _uiState.update {
                            it.copy(
                                site = foundSite,
                                month = foundMonth,
                                isLoading = false,
                                errorMessage = null
                            )
                        }

                        // İlk data yüklendikten sonra reactive updates başlat
                        observeBlocksAndPayments(foundSite.id, foundMonth.id)
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

    // Reaktif: Bloklar VE ödemeler birlikte dinleniyor
    private fun observeBlocksAndPayments(siteId: Long, monthId: Long) {
        viewModelScope.launch {
            combine(
                blockRepository.observeBlocksBySiteId(siteId),
                paymentRepository.observePaymentsByMonth(monthId)
            ) { blocks, _ ->
                // Her blok için ödeme durumunu hesapla
                val statusMap = mutableMapOf<Long, Boolean>()
                blocks.forEach { block ->
                    // Bloğun tüm kişileri için ödeme kontrolü
                    val isFullyPaid = paymentRepository.isBlockFullyPaid(block.id, monthId)
                    statusMap[block.id] = isFullyPaid
                }

                Pair(blocks, statusMap)
            }.collect { (blocks, statusMap) ->
                // Otomatik güncelleme
                _uiState.update {
                    it.copy(
                        blocks = blocks,
                        blocksStatus = statusMap
                    )
                }
            }
        }
    }

    fun addBlock(blockName: String) {
        viewModelScope.launch {
            try {
                val site = _uiState.value.site ?: return@launch

                if (blockRepository.isBlockNameExists(site.id, blockName)) {
                    _uiState.update { it.copy(errorMessage = "Bu blok adı zaten mevcut") }
                    return@launch
                }

                val newBlock = Block(
                    siteId = site.id,
                    name = blockName
                )
                blockRepository.insertBlock(newBlock)
                // Manuel refresh YOK - observeBlocksAndPayments otomatik günceller!
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Blok eklenirken hata oluştu") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}