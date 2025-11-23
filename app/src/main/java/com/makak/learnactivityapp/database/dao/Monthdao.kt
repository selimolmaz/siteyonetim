package com.makak.learnactivityapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.makak.learnactivityapp.database.entities.Month
import kotlinx.coroutines.flow.Flow

@Dao
interface MonthDao {

    // Flow ile reactive month tracking
    @Query("SELECT * FROM months WHERE site_id = :siteId ORDER BY year DESC, month_number DESC")
    fun observeMonthsBySiteId(siteId: Long): Flow<List<Month>>

    @Query("SELECT * FROM months WHERE site_id = :siteId ORDER BY year DESC, month_number DESC")
    suspend fun getMonthsBySiteId(siteId: Long): List<Month>

    @Query("SELECT * FROM months WHERE id = :monthId LIMIT 1")
    suspend fun getMonthById(monthId: Long): Month?

    @Query("SELECT * FROM months WHERE site_id = :siteId AND name = :monthName LIMIT 1")
    suspend fun getMonthBySiteAndName(siteId: Long, monthName: String): Month?

    @Insert
    suspend fun insertMonth(month: Month): Long

    @Update
    suspend fun updateMonth(month: Month)

    @Query("DELETE FROM months WHERE id = :id")
    suspend fun deleteMonth(id: Long)

    @Query("SELECT COUNT(*) FROM months WHERE site_id = :siteId AND name = :monthName")
    suspend fun getMonthCountBySiteAndName(siteId: Long, monthName: String): Int

    @Query("SELECT COUNT(*) FROM months WHERE site_id = :siteId AND name = :monthName AND id != :excludeId")
    suspend fun getMonthCountBySiteAndNameExcludingId(siteId: Long, monthName: String, excludeId: Long): Int
}