package com.makak.learnactivityapp.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "blocks",
    foreignKeys = [
        ForeignKey(
            entity = Site::class,
            parentColumns = ["id"],
            childColumns = ["site_id"],
            onDelete = ForeignKey.CASCADE // Site silinince blokları da sil
        )
    ],
    indices = [Index(value = ["site_id"])] // Performance için index
)
data class Block(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "site_id")
    val siteId: Long, // Month değil, Site'e bağlı!

    @ColumnInfo(name = "name")
    val name: String, // "1A", "2B", "3C", etc.

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)