package com.makak.learnactivityapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.makak.learnactivityapp.database.entities.Block

@Dao
interface BlockDao {
    @Query("SELECT * FROM blocks WHERE site_id = :siteId ORDER BY created_at ASC")
    suspend fun getBlocksBySiteId(siteId: Long): List<Block>

    @Query("SELECT * FROM blocks WHERE site_id = :siteId AND name = :blockName LIMIT 1")
    suspend fun getBlockBySiteAndName(siteId: Long, blockName: String): Block?

    @Insert
    suspend fun insertBlock(block: Block): Long

    @Update
    suspend fun updateBlock(block: Block)

    @Query("DELETE FROM blocks WHERE id = :id")
    suspend fun deleteBlock(id: Long)

    @Query("SELECT COUNT(*) FROM blocks WHERE site_id = :siteId AND name = :blockName")
    suspend fun getBlockCountBySiteAndName(siteId: Long, blockName: String): Int

    @Query("SELECT COUNT(*) FROM blocks WHERE site_id = :siteId AND name = :blockName AND id != :excludeId")
    suspend fun getBlockCountBySiteAndNameExcludingId(siteId: Long, blockName: String, excludeId: Long): Int
}