package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sale_invoices")
data class SaleInvoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "عميل نقدي",
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val subtotal: Double,
    val discount: Double = 0.0,
    val total: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val paymentType: String = "نقدي", // نقدي, آجل, تحويل, أخرى
    val notes: String = ""
)
