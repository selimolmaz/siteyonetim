package com.makak.learnactivityapp.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "months",
    foreignKeys = [
        ForeignKey(
            entity = Site::class,
            parentColumns = ["id"],
            childColumns = ["site_id"],
            onDelete = ForeignKey.CASCADE // Site silinince ayları da sil
        )
    ],
    indices = [Index(value = ["site_id"])] // Performance için index
)
data class Month(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "site_id")
    val siteId: Long,

    @ColumnInfo(name = "name")
    val name: String, // "Kasım 2025"

    @ColumnInfo(name = "year")
    val year: Int, // 2025

    @ColumnInfo(name = "month_number")
    val monthNumber: Int, // 11 (Kasım)

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)