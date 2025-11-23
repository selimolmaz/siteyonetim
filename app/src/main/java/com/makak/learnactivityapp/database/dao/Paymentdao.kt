package com.makak.learnactivityapp.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.makak.learnactivityapp.database.entities.Payment
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePayment(payment: Payment): Long

    @Update
    suspend fun updatePayment(payment: Payment)

    @Query("DELETE FROM payments WHERE id = :id")
    suspend fun deletePayment(id: Long)

    @Query("DELETE FROM payments WHERE person_id = :personId AND month_id = :monthId")
    suspend fun deletePaymentByPersonAndMonth(personId: Long, monthId: Long)

    // Flow ile reactive payment tracking
    @Query("""
        SELECT * FROM payments 
        WHERE person_id = :personId AND month_id = :monthId 
        LIMIT 1
    """)
    fun observePaymentByPersonAndMonth(personId: Long, monthId: Long): Flow<Payment?>

    // Belirli bir kişi ve ay için ödeme kaydını getir
    @Query("""
        SELECT * FROM payments 
        WHERE person_id = :personId AND month_id = :monthId 
        LIMIT 1
    """)
    suspend fun getPaymentByPersonAndMonth(personId: Long, monthId: Long): Payment?

    // Bir ay için tüm ödemeleri observe et
    @Query("SELECT * FROM payments WHERE month_id = :monthId")
    fun observePaymentsByMonth(monthId: Long): Flow<List<Payment>>

    // Belirli kişi ve ay için ödeme kaydedildi mi kontrol et
    @Query("""
        SELECT COUNT(*) FROM payments 
        WHERE person_id = :personId AND month_id = :monthId 
        AND (
            (is_paid_enabled = 1 AND paid_amount != '0' AND paid_amount != '') OR
            (is_will_pay_enabled = 1 AND will_pay_amount != '0' AND will_pay_amount != '')
        )
    """)
    suspend fun isPaymentSaved(personId: Long, monthId: Long): Int

    // Bir blokta tüm kişiler için belirli bir ayda ödeme tamamlandı mı
    @Query("""
        SELECT COUNT(DISTINCT p.id) FROM people p
        LEFT JOIN payments pay ON p.id = pay.person_id AND pay.month_id = :monthId
        WHERE p.block_id = :blockId
        AND (
            pay.id IS NULL OR 
            (
                pay.is_paid_enabled = 0 AND pay.is_will_pay_enabled = 0
            ) OR
            (
                pay.is_paid_enabled = 1 AND (pay.paid_amount = '0' OR pay.paid_amount = '')
            ) OR
            (
                pay.is_will_pay_enabled = 1 AND (pay.will_pay_amount = '0' OR pay.will_pay_amount = '')
            )
        )
    """)
    suspend fun getUnpaidPeopleCountInBlock(blockId: Long, monthId: Long): Int

    // Bir sitede belirli ay için tüm bloklar tamamlandı mı
    @Query("""
        SELECT COUNT(DISTINCT b.id) FROM blocks b
        LEFT JOIN people p ON b.id = p.block_id
        LEFT JOIN payments pay ON p.id = pay.person_id AND pay.month_id = :monthId
        WHERE b.site_id = :siteId
        AND p.id IS NOT NULL
        AND (
            pay.id IS NULL OR 
            (
                pay.is_paid_enabled = 0 AND pay.is_will_pay_enabled = 0
            ) OR
            (
                pay.is_paid_enabled = 1 AND (pay.paid_amount = '0' OR pay.paid_amount = '')
            ) OR
            (
                pay.is_will_pay_enabled = 1 AND (pay.will_pay_amount = '0' OR pay.will_pay_amount = '')
            )
        )
        GROUP BY b.id
        HAVING COUNT(p.id) > 0
    """)
    suspend fun getIncompleteBlocksCountInSite(siteId: Long, monthId: Long): Int

    // Bir blokta toplam kişi sayısı
    @Query("SELECT COUNT(*) FROM people WHERE block_id = :blockId")
    suspend fun getPeopleCountInBlock(blockId: Long): Int

    // Bir sitede kişisi olan blok sayısı
    @Query("""
        SELECT COUNT(DISTINCT block_id) FROM people p
        JOIN blocks b ON p.block_id = b.id
        WHERE b.site_id = :siteId
    """)
    suspend fun getBlocksWithPeopleCountInSite(siteId: Long): Int

    // Belirli bir aya ait tüm ödemeleri getir
    @Query("SELECT * FROM payments WHERE month_id = :monthId")
    suspend fun getPaymentsByMonth(monthId: Long): List<Payment>

    // Belirli bir kişiye ait tüm ödemeleri getir
    @Query("SELECT * FROM payments WHERE person_id = :personId ORDER BY created_at DESC")
    suspend fun getPaymentsByPerson(personId: Long): List<Payment>
}