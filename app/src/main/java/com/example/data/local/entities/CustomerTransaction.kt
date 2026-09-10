package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customer_transactions",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class CustomerTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val invoiceId: Long? = null,
    val date: String,
    val time: String,
    val timestamp: Long = System.currentTimeMillis(),
    val statement: String,
    val amount: Double,
    val paid: Double,
    val remaining: Double
)
