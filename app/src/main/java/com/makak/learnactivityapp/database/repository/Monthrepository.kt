package com.makak.learnactivityapp.database.repository

import com.makak.learnactivityapp.database.dao.MonthDao
import com.makak.learnactivityapp.database.entities.Month
import kotlinx.coroutines.flow.Flow

class MonthRepository(private val monthDao: MonthDao) {

    // Flow fonksiyonu
    fun observeMonthsBySiteId(siteId: Long): Flow<List<Month>> {
        return monthDao.observeMonthsBySiteId(siteId)
    }

    suspend fun getMonthsBySiteId(siteId: Long): List<Month> {
        return monthDao.getMonthsBySiteId(siteId)
    }

    suspend fun getMonthById(monthId: Long): Month? {
        return monthDao.getMonthById(monthId)
    }

    suspend fun insertMonth(month: Month): Long {
        return monthDao.insertMonth(month)
    }

    suspend fun updateMonth(month: Month) {
        monthDao.updateMonth(month)
    }

    suspend fun deleteMonth(id: Long) {
        monthDao.deleteMonth(id)
    }

    suspend fun isMonthNameExists(siteId: Long, monthName: String): Boolean {
        return monthDao.getMonthCountBySiteAndName(siteId, monthName) > 0
    }

    suspend fun isMonthNameExistsForUpdate(siteId: Long, monthName: String, excludeId: Long): Boolean {
        return monthDao.getMonthCountBySiteAndNameExcludingId(siteId, monthName, excludeId) > 0
    }
}