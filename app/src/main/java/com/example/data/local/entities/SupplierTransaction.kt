package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "supplier_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Supplier::class,
            parentColumns = ["id"],
            childColumns = ["supplierId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("supplierId")]
)
data class SupplierTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long,
    val purchaseId: Long? = null,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val statement: String,
    val amount: Double,
    val paid: Double,
    val remaining: Double
)
