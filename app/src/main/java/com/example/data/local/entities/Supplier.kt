package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val phone: String = "",
    val company: String = "",
    val balance: Double = 0.0, // positive means store owes supplier (مستحق له)
    val totalPurchases: Double = 0.0,
    val totalPaid: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
