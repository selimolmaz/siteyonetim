package com.makak.learnactivityapp.database.repository

import com.makak.learnactivityapp.database.dao.PaymentDao
import com.makak.learnactivityapp.database.entities.Payment

class PaymentRepository(private val paymentDao: PaymentDao) {

    suspend fun savePayment(payment: Payment): Long {
        return paymentDao.insertOrUpdatePayment(payment)
    }

    suspend fun getPaymentByPersonAndMonth(personId: Long, monthId: Long): Payment? {
        return paymentDao.getPaymentByPersonAndMonth(personId, monthId)
    }

    suspend fun removePaymentByPersonAndMonth(personId: Long, monthId: Long) {
        paymentDao.deletePaymentByPersonAndMonth(personId, monthId)
    }

    suspend fun isPaymentSaved(personId: Long, monthId: Long): Boolean {
        return paymentDao.isPaymentSaved(personId, monthId) > 0
    }

    // Bir blokta tüm kişiler için belirli bir ayda ödeme tamamlandı mı
    suspend fun isBlockFullyPaid(blockId: Long, monthId: Long): Boolean {
        val totalPeopleCount = paymentDao.getPeopleCountInBlock(blockId)
        if (totalPeopleCount == 0) return true // Blokta kimse yoksa tamamlanmış sayılır

        val unpaidCount = paymentDao.getUnpaidPeopleCountInBlock(blockId, monthId)
        return unpaidCount == 0
    }

    // Bir sitede belirli ay için tüm bloklar tamamlandı mı
    suspend fun isMonthFullyPaidInSite(siteId: Long, monthId: Long): Boolean {
        val totalBlocksWithPeopleCount = paymentDao.getBlocksWithPeopleCountInSite(siteId)
        if (totalBlocksWithPeopleCount == 0) return true // Sitede kişi yoksa tamamlanmış sayılır

        val incompleteBlocksCount = paymentDao.getIncompleteBlocksCountInSite(siteId, monthId)
        return incompleteBlocksCount == 0
    }

    suspend fun getPaymentsByMonth(monthId: Long): List<Payment> {
        return paymentDao.getPaymentsByMonth(monthId)
    }

    suspend fun getPaymentsByPerson(personId: Long): List<Payment> {
        return paymentDao.getPaymentsByPerson(personId)
    }

    suspend fun updatePayment(payment: Payment) {
        paymentDao.updatePayment(payment)
    }

    suspend fun deletePayment(id: Long) {
        paymentDao.deletePayment(id)
    }
}