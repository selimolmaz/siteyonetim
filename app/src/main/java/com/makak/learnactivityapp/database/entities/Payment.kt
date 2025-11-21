package com.makak.learnactivityapp.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Person::class,
            parentColumns = ["id"],
            childColumns = ["person_id"],
            onDelete = ForeignKey.CASCADE // Kişi silinince ödemelerini de sil
        ),
        ForeignKey(
            entity = Month::class,
            parentColumns = ["id"],
            childColumns = ["month_id"],
            onDelete = ForeignKey.CASCADE // Ay silinince ödemelerini de sil
        )
    ],
    indices = [
        Index(value = ["person_id"]),
        Index(value = ["month_id"]),
        Index(value = ["person_id", "month_id"], unique = true) // Bir kişi bir ayda sadece bir ödeme kaydı
    ]
)
data class Payment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "person_id")
    val personId: Long,

    @ColumnInfo(name = "month_id")
    val monthId: Long,

    @ColumnInfo(name = "paid_amount")
    val paidAmount: String = "0", // Ödenen miktar

    @ColumnInfo(name = "will_pay_amount")
    val willPayAmount: String = "0", // Ödeyeceği miktar

    @ColumnInfo(name = "is_paid_enabled")
    val isPaidEnabled: Boolean = false, // "Ödedi" seçeneği aktif mi

    @ColumnInfo(name = "is_will_pay_enabled")
    val isWillPayEnabled: Boolean = false, // "Ödeyecek" seçeneği aktif mi

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)