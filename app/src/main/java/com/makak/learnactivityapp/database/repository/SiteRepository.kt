package com.makak.learnactivityapp.database.repository

import com.makak.learnactivityapp.database.dao.SiteDao
import com.makak.learnactivityapp.database.entities.Site
import kotlinx.coroutines.flow.Flow

class SiteRepository(private val siteDao: SiteDao) {

    // Flow fonksiyonu
    fun observeAllSites(): Flow<List<Site>> {
        return siteDao.observeAllSites()
    }

    suspend fun getAllSites(): List<Site> {
        return siteDao.getAllSites()
    }

    suspend fun insertSite(site: Site): Long {
        return siteDao.insertSite(site)
    }

    suspend fun updateSite(site: Site) {
        siteDao.updateSite(site)
    }

    suspend fun deleteSite(id: Long) {
        siteDao.deleteSite(id)
    }

    suspend fun isSiteNameExists(name: String): Boolean {
        return siteDao.getSiteCountByName(name) > 0
    }

    suspend fun isSiteNameExistsForUpdate(name: String, excludeId: Long): Boolean {
        return siteDao.getSiteCountByNameExcludingId(name, excludeId) > 0
    }
}