package com.makak.learnactivityapp.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "people",
    foreignKeys = [
        ForeignKey(
            entity = Block::class,
            parentColumns = ["id"],
            childColumns = ["block_id"],
            onDelete = ForeignKey.CASCADE // Block silinince kişileri de sil
        )
    ],
    indices = [Index(value = ["block_id"])] // Performance için index
)
data class Person(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "block_id")
    val blockId: Long,

    @ColumnInfo(name = "name")
    val name: String, // "Ali Yılmaz", "Mehmet Demir", etc.

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)