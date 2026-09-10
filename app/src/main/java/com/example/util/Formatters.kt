package com.example.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    private val decimalFormat = DecimalFormat("#,##0.##", DecimalFormatSymbols(Locale.US))
    private val integerFormat = DecimalFormat("#,##0", DecimalFormatSymbols(Locale.US))

    fun formatCurrency(amount: Double): String {
        return "${decimalFormat.format(amount)} ر.ي"
    }

    fun formatNumber(number: Double): String {
        return decimalFormat.format(number)
    }

    fun formatInteger(number: Long): String {
        return integerFormat.format(number)
    }

    fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    fun getCurrentTime(): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.US)
        return sdf.format(Date())
    }

    fun generateInvoiceNumber(prefix: String = "INV"): String {
        val sdf = SimpleDateFormat("yyMMddHHmmss", Locale.US)
        return "$prefix-${sdf.format(Date())}"
    }
}
