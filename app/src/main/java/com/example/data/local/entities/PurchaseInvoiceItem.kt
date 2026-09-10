package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "purchase_invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseInvoice::class,
            parentColumns = ["id"],
            childColumns = ["purchaseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("purchaseId")]
)
data class PurchaseInvoiceItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val purchaseId: Long,
    val productId: Long? = null,
    val productName: String,
    val quantity: Double,
    val unitPrice: Double,
    val totalPrice: Double,
    val unit: String = "حبة"
)
