package com.makak.learnactivityapp.ui.screens.odemeekran.viewmodel

import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.entities.Payment
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.database.entities.Site

data class OdemeUiState(
    val site: Site? = null,
    val month: Month? = null,
    val person: Person? = null,
    val currentPayment: Payment? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)