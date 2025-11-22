package com.makak.learnactivityapp.ui.screens.sitesecimekran.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.makak.learnactivityapp.database.entities.Site
import com.makak.learnactivityapp.database.repository.SiteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SiteViewModel @Inject constructor(
    private val siteRepository: SiteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SiteUiState())
    val uiState: StateFlow<SiteUiState> = _uiState.asStateFlow()

    init {
        loadSites()
    }

    private fun loadSites() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val sites = siteRepository.getAllSites()
                _uiState.update {
                    it.copy(
                        sites = sites,
                        isLoading = false,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Site listesi yüklenirken hata oluştu"
                    )
                }
            }
        }
    }

    fun addSite(siteName: String) {
        viewModelScope.launch {
            try {
                if (siteRepository.isSiteNameExists(siteName)) {
                    _uiState.update { it.copy(errorMessage = "Bu site adı zaten mevcut") }
                    return@launch
                }

                val newSite = Site(name = siteName)
                siteRepository.insertSite(newSite)
                loadSites()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Site eklenirken hata oluştu") }
            }
        }
    }

    fun updateSite(site: Site, newName: String) {
        viewModelScope.launch {
            try {
                if (siteRepository.isSiteNameExistsForUpdate(newName, site.id)) {
                    _uiState.update { it.copy(errorMessage = "Bu site adı zaten mevcut") }
                    return@launch
                }

                val updatedSite = site.copy(name = newName)
                siteRepository.updateSite(updatedSite)
                loadSites()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Site güncellenirken hata oluştu") }
            }
        }
    }

    fun deleteSite(siteId: Long) {
        viewModelScope.launch {
            try {
                siteRepository.deleteSite(siteId)
                loadSites()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Site silinirken hata oluştu") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}