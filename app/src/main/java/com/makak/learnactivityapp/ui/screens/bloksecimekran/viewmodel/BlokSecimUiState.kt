package com.makak.learnactivityapp.ui.screens.bloksecimekran.viewmodel

import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.entities.Site

data class BlokSecimUiState(
    val site: Site? = null,
    val month: Month? = null,
    val blocks: List<Block> = emptyList(),
    val blocksStatus: Map<Long, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)