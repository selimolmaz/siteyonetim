package com.makak.learnactivityapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.makak.learnactivityapp.database.entities.Site
import kotlinx.coroutines.flow.Flow

@Dao
interface SiteDao {

    // Flow ile reactive site tracking
    @Query("SELECT * FROM sites ORDER BY created_at DESC")
    fun observeAllSites(): Flow<List<Site>>

    @Query("SELECT * FROM sites ORDER BY created_at DESC")
    suspend fun getAllSites(): List<Site>

    @Insert
    suspend fun insertSite(site: Site): Long

    @Update
    suspend fun updateSite(site: Site)

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun deleteSite(id: Long)

    @Query("SELECT COUNT(*) FROM sites WHERE name = :name")
    suspend fun getSiteCountByName(name: String): Int

    @Query("SELECT COUNT(*) FROM sites WHERE name = :name AND id != :excludeId")
    suspend fun getSiteCountByNameExcludingId(name: String, excludeId: Long): Int
}