package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val price: Double,
    val purchasePrice: Double = 0.0,
    val quantity: Double = 0.0,
    val minQuantity: Double = 5.0,
    val unit: String = "حبة",
    val barcode: String = "",
    val category: String = "مواد غذائية",
    val createdAt: Long = System.currentTimeMillis()
)
