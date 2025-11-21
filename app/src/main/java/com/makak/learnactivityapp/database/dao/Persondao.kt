package com.makak.learnactivityapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.makak.learnactivityapp.database.entities.Person

@Dao
interface PersonDao {
    @Query("SELECT * FROM people WHERE block_id = :blockId ORDER BY created_at ASC")
    suspend fun getPeopleByBlockId(blockId: Long): List<Person>

    @Query("SELECT * FROM people WHERE block_id = :blockId AND name = :personName LIMIT 1")
    suspend fun getPersonByBlockAndName(blockId: Long, personName: String): Person?

    @Insert
    suspend fun insertPerson(person: Person): Long

    @Update
    suspend fun updatePerson(person: Person)

    @Query("DELETE FROM people WHERE id = :id")
    suspend fun deletePerson(id: Long)

    @Query("SELECT COUNT(*) FROM people WHERE block_id = :blockId AND name = :personName")
    suspend fun getPersonCountByBlockAndName(blockId: Long, personName: String): Int

    @Query("SELECT COUNT(*) FROM people WHERE block_id = :blockId AND name = :personName AND id != :excludeId")
    suspend fun getPersonCountByBlockAndNameExcludingId(blockId: Long, personName: String, excludeId: Long): Int
}