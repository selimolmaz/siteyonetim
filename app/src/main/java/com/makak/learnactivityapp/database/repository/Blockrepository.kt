package com.makak.learnactivityapp.database.repository

import com.makak.learnactivityapp.database.dao.BlockDao
import com.makak.learnactivityapp.database.entities.Block
import kotlinx.coroutines.flow.Flow

class BlockRepository(private val blockDao: BlockDao) {

    // Flow fonksiyonu
    fun observeBlocksBySiteId(siteId: Long): Flow<List<Block>> {
        return blockDao.observeBlocksBySiteId(siteId)
    }

    suspend fun getBlocksBySiteId(siteId: Long): List<Block> {
        return blockDao.getBlocksBySiteId(siteId)
    }

    suspend fun insertBlock(block: Block): Long {
        return blockDao.insertBlock(block)
    }

    suspend fun updateBlock(block: Block) {
        blockDao.updateBlock(block)
    }

    suspend fun deleteBlock(id: Long) {
        blockDao.deleteBlock(id)
    }

    suspend fun isBlockNameExists(siteId: Long, blockName: String): Boolean {
        return blockDao.getBlockCountBySiteAndName(siteId, blockName) > 0
    }

    suspend fun isBlockNameExistsForUpdate(siteId: Long, blockName: String, excludeId: Long): Boolean {
        return blockDao.getBlockCountBySiteAndNameExcludingId(siteId, blockName, excludeId) > 0
    }
}