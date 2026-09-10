package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchase_invoices")
data class PurchaseInvoice(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val supplierId: Long? = null,
    val supplierName: String = "مورد نقدي",
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val total: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val paymentType: String = "نقدي",
    val notes: String = ""
)
