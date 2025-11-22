package com.makak.learnactivityapp.ui.screens.aysecimekran.viewmodel

import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.entities.Site

data class AySecimUiState(
    val site: Site? = null,
    val months: List<Month> = emptyList(),
    val monthsStatus: Map<Long, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)