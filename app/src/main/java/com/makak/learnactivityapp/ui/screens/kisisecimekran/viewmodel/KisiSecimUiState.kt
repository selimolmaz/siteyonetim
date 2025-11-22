package com.makak.learnactivityapp.ui.screens.kisisecimekran.viewmodel

import com.makak.learnactivityapp.database.entities.Block
import com.makak.learnactivityapp.database.entities.Month
import com.makak.learnactivityapp.database.entities.Person
import com.makak.learnactivityapp.database.entities.Site

data class KisiSecimUiState(
    val site: Site? = null,
    val month: Month? = null,
    val block: Block? = null,
    val people: List<Person> = emptyList(),
    val peopleStatus: Map<Long, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)