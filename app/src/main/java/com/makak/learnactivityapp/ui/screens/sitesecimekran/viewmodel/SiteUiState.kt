package com.makak.learnactivityapp.ui.screens.sitesecimekran.viewmodel

import com.makak.learnactivityapp.database.entities.Site

data class SiteUiState(
    val sites: List<Site> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)