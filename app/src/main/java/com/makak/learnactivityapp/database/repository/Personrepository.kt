package com.makak.learnactivityapp.database.repository

import com.makak.learnactivityapp.database.dao.PersonDao
import com.makak.learnactivityapp.database.entities.Person
import kotlinx.coroutines.flow.Flow

class PersonRepository(private val personDao: PersonDao) {

    // Flow fonksiyonu
    fun observePeopleByBlockId(blockId: Long): Flow<List<Person>> {
        return personDao.observePeopleByBlockId(blockId)
    }

    suspend fun getPeopleByBlockId(blockId: Long): List<Person> {
        return personDao.getPeopleByBlockId(blockId)
    }

    suspend fun getPersonById(personId: Long): Person? {
        return personDao.getPersonById(personId)
    }

    suspend fun insertPerson(person: Person): Long {
        return personDao.insertPerson(person)
    }

    suspend fun updatePerson(person: Person) {
        personDao.updatePerson(person)
    }

    suspend fun deletePerson(id: Long) {
        personDao.deletePerson(id)
    }

    suspend fun isPersonNameExists(blockId: Long, personName: String): Boolean {
        return personDao.getPersonCountByBlockAndName(blockId, personName) > 0
    }

    suspend fun isPersonNameExistsForUpdate(blockId: Long, personName: String, excludeId: Long): Boolean {
        return personDao.getPersonCountByBlockAndNameExcludingId(blockId, personName, excludeId) > 0
    }
}