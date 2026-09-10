package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.local.entities.Customer
import com.example.data.local.entities.CustomerTransaction
import com.example.data.local.entities.PurchaseInvoice
import com.example.data.local.entities.PurchaseInvoiceItem
import com.example.data.local.entities.SaleInvoice
import com.example.data.local.entities.SaleInvoiceItem
import java.io.File
import java.io.FileOutputStream

object PdfReceiptGenerator {

    enum class PaperWidth(val widthPoints: Int, val label: String) {
        MM58(164, "58 مم"),
        MM80(226, "80 مم")
    }

    const val STORE_NAME = "بقالة العزي للمواد الغذائية"
    const val STORE_PHONE = "776425052"
    const val STORE_ADDRESS = "اليمن - صنعاء"
    const val STORE_FOOTER = "شكراً لزيارتكم • أهلاً وسهلاً بكم"

    fun generateSaleInvoicePdf(
        context: Context,
        invoice: SaleInvoice,
        items: List<SaleInvoiceItem>,
        paperWidth: PaperWidth = PaperWidth.MM80,
        showUnitPrice: Boolean = false
    ): File? {
        try {
            val width = paperWidth.widthPoints
            val baseHeight = 220
            val itemHeight = 18
            val calculatedHeight = baseHeight + (items.size * itemHeight) + (if (invoice.discount > 0) 18 else 0) + (if (invoice.remainingAmount > 0) 18 else 0)

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(width, calculatedHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = if (paperWidth == PaperWidth.MM58) 7.5f else 8.5f
            }

            val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = if (paperWidth == PaperWidth.MM58) 8.5f else 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = if (paperWidth == PaperWidth.MM58) 11f else 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val centerPaint = TextPaint(textPaint).apply { textAlign = Paint.Align.CENTER }
            val centerBoldPaint = TextPaint(boldPaint).apply { textAlign = Paint.Align.CENTER }
            val rightInfoPaint = TextPaint(textPaint).apply { textAlign = Paint.Align.RIGHT }
            val rightBoldPaint = TextPaint(boldPaint).apply { textAlign = Paint.Align.RIGHT }
            val leftInfoPaint = TextPaint(textPaint).apply { textAlign = Paint.Align.LEFT }

            val linePaint = Paint().apply {
                color = Color.DKGRAY
                strokeWidth = 0.8f
                style = Paint.Style.STROKE
            }

            val solidLinePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 1f
            }

            val margin = 8f
            val contentWidth = width - (margin * 2)
            var currentY = 22f
            val centerX = width / 2f

            // 1. Header
            canvas.drawText(STORE_NAME, centerX, currentY, titlePaint)
            currentY += 13f
            canvas.drawText("هاتف: $STORE_PHONE", centerX, currentY, centerPaint)
            currentY += 11f
            canvas.drawText(STORE_ADDRESS, centerX, currentY, centerPaint)
            currentY += 13f

            // Double line
            canvas.drawLine(margin, currentY, width - margin, currentY, solidLinePaint)
            currentY += 2f
            canvas.drawLine(margin, currentY, width - margin, currentY, solidLinePaint)
            currentY += 14f

            // 2. Invoice Meta Info
            canvas.drawText("فاتورة مبيعات #${invoice.invoiceNumber}", centerX, currentY, centerBoldPaint)
            currentY += 14f

            canvas.drawText("العميل: ${invoice.customerName}", width - margin, currentY, rightBoldPaint)
            currentY += 12f

            canvas.drawText("التاريخ: ${invoice.date} ${invoice.time}", width - margin, currentY, rightInfoPaint)
            currentY += 12f

            canvas.drawText("نوع الدفع: ${invoice.paymentType}", width - margin, currentY, rightInfoPaint)
            currentY += 16f

            // Divider before table
            canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)
            currentY += 14f

            // 3. Table Header
            val headerCol1 = width - margin // Item name
            val headerCol2 = if (showUnitPrice) margin + (contentWidth * 0.45f) else margin + (contentWidth * 0.35f) // Qty
            val headerCol3 = margin // Total

            canvas.drawText("الصنف", headerCol1, currentY, rightBoldPaint)
            canvas.drawText("الكمية", headerCol2, currentY, Paint(boldPaint).apply { textAlign = Paint.Align.CENTER })
            canvas.drawText("الإجمالي", headerCol3, currentY, Paint(boldPaint).apply { textAlign = Paint.Align.LEFT })
            currentY += 12f

            canvas.drawLine(margin, currentY, width - margin, currentY, solidLinePaint)
            currentY += 14f

            // 4. Items List
            items.forEach { item ->
                val qtyText = "${Formatters.formatNumber(item.quantity)} ${item.unit}"
                val totalText = "${Formatters.formatNumber(item.totalPrice)} ر.ي"

                val itemTitle = if (showUnitPrice) {
                    "${item.productName} (@${Formatters.formatNumber(item.unitPrice)})"
                } else {
                    item.productName
                }

                canvas.drawText(itemTitle, headerCol1, currentY, rightInfoPaint)
                canvas.drawText(qtyText, headerCol2, currentY, Paint(textPaint).apply { textAlign = Paint.Align.CENTER })
                canvas.drawText(totalText, headerCol3, currentY, leftInfoPaint)
                currentY += 18f
            }

            // Divider after table
            canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)
            currentY += 16f

            // 5. Totals
            fun drawSummaryRow(label: String, value: String, isBold: Boolean = false) {
                val p = if (isBold) rightBoldPaint else rightInfoPaint
                val lp = if (isBold) Paint(boldPaint).apply { textAlign = Paint.Align.LEFT } else leftInfoPaint
                canvas.drawText(label, width - margin, currentY, p)
                canvas.drawText(value, margin, currentY, lp)
                currentY += 16f
            }

            if (invoice.discount > 0) {
                drawSummaryRow("المجموع الفرعي:", Formatters.formatCurrency(invoice.subtotal))
                drawSummaryRow("الخصم الممنوح:", "- ${Formatters.formatCurrency(invoice.discount)}")
            }

            drawSummaryRow("الصافي الإجمالي:", Formatters.formatCurrency(invoice.total), isBold = true)
            drawSummaryRow("المبلغ المدفوع:", Formatters.formatCurrency(invoice.paidAmount))

            if (invoice.remainingAmount > 0) {
                drawSummaryRow("المتبقي (آجل/دين):", Formatters.formatCurrency(invoice.remainingAmount), isBold = true)
            }

            // Divider before footer
            currentY += 4f
            canvas.drawLine(margin, currentY, width - margin, currentY, solidLinePaint)
            currentY += 16f

            // 6. Footer
            canvas.drawText(STORE_FOOTER, centerX, currentY, centerBoldPaint)
            currentY += 12f
            canvas.drawText("نظام بقالة العزي لإدارة نقاط البيع", centerX, currentY, TextPaint(textPaint).apply { textSize = 6.5f; textAlign = Paint.Align.CENTER; color = Color.GRAY })

            document.finishPage(page)

            // Save PDF to cache directory
            val outputDir = File(context.cacheDir, "receipts")
            if (!outputDir.exists()) outputDir.mkdirs()
            val pdfFile = File(outputDir, "invoice_${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun generatePurchaseInvoicePdf(
        context: Context,
        invoice: PurchaseInvoice,
        items: List<PurchaseInvoiceItem>,
        paperWidth: PaperWidth = PaperWidth.MM80
    ): File? {
        try {
            val width = paperWidth.widthPoints
            val baseHeight = 220
            val itemHeight = 18
            val calculatedHeight = baseHeight + (items.size * itemHeight) + (if (invoice.remainingAmount > 0) 18 else 0)

            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(width, calculatedHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = if (paperWidth == PaperWidth.MM58) 7.5f else 8.5f
            }

            val boldPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = if (paperWidth == PaperWidth.MM58) 8.5f else 9.5f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            }

            val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.BLACK
                textSize = if (paperWidth == PaperWidth.MM58) 11f else 13f
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }

            val centerPaint = TextPaint(textPaint).apply { textAlign = Paint.Align.CENTER }
            val centerBoldPaint = TextPaint(boldPaint).apply { textAlign = Paint.Align.CENTER }
            val rightInfoPaint = TextPaint(textPaint).apply { textAlign = Paint.Align.RIGHT }
            val rightBoldPaint = TextPaint(boldPaint).apply { textAlign = Paint.Align.RIGHT }
            val leftInfoPaint = TextPaint(textPaint).apply { textAlign = Paint.Align.LEFT }

            val linePaint = Paint().apply {
                color = Color.DKGRAY
                strokeWidth = 0.8f
                style = Paint.Style.STROKE
            }

            val solidLinePaint = Paint().apply {
                color = Color.BLACK
                strokeWidth = 1f
            }

            val margin = 8f
            val contentWidth = width - (margin * 2)
            var currentY = 22f
            val centerX = width / 2f

            canvas.drawText(STORE_NAME, centerX, currentY, titlePaint)
            currentY += 13f
            canvas.drawText("سند استلام بضاعة / مشتريات", centerX, currentY, centerBoldPaint)
            currentY += 13f

            canvas.drawLine(margin, currentY, width - margin, currentY, solidLinePaint)
            currentY += 14f

            canvas.drawText("فاتورة مشتريات #${invoice.invoiceNumber}", centerX, currentY, centerBoldPaint)
            currentY += 14f

            canvas.drawText("المورد: ${invoice.supplierName}", width - margin, currentY, rightBoldPaint)
            currentY += 12f

            canvas.drawText("التاريخ: ${invoice.date} ${invoice.time}", width - margin, currentY, rightInfoPaint)
            currentY += 16f

            canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)
            currentY += 14f

            val headerCol1 = width - margin
            val headerCol2 = margin + (contentWidth * 0.45f)
            val headerCol3 = margin

            canvas.drawText("الصنف", headerCol1, currentY, rightBoldPaint)
            canvas.drawText("الكمية", headerCol2, currentY, Paint(boldPaint).apply { textAlign = Paint.Align.CENTER })
            canvas.drawText("الإجمالي", headerCol3, currentY, Paint(boldPaint).apply { textAlign = Paint.Align.LEFT })
            currentY += 12f

            canvas.drawLine(margin, currentY, width - margin, currentY, solidLinePaint)
            currentY += 14f

            items.forEach { item ->
                canvas.drawText("${item.productName} (@${Formatters.formatNumber(item.unitPrice)})", headerCol1, currentY, rightInfoPaint)
                canvas.drawText("${Formatters.formatNumber(item.quantity)} ${item.unit}", headerCol2, currentY, Paint(textPaint).apply { textAlign = Paint.Align.CENTER })
                canvas.drawText("${Formatters.formatNumber(item.totalPrice)} ر.ي", headerCol3, currentY, leftInfoPaint)
                currentY += 18f
            }

            canvas.drawLine(margin, currentY, width - margin, currentY, linePaint)
            currentY += 16f

            canvas.drawText("إجمالي المشتريات:", width - margin, currentY, rightBoldPaint)
            canvas.drawText(Formatters.formatCurrency(invoice.total), margin, currentY, Paint(boldPaint).apply { textAlign = Paint.Align.LEFT })
            currentY += 16f

            canvas.drawText("المبلغ المسدد للمورد:", width - margin, currentY, rightInfoPaint)
            canvas.drawText(Formatters.formatCurrency(invoice.paidAmount), margin, currentY, leftInfoPaint)
            currentY += 16f

            if (invoice.remainingAmount > 0) {
                canvas.drawText("المتبقي له (دين):", width - margin, currentY, rightBoldPaint)
                canvas.drawText(Formatters.formatCurrency(invoice.remainingAmount), margin, currentY, Paint(boldPaint).apply { textAlign = Paint.Align.LEFT })
                currentY += 16f
            }

            document.finishPage(page)

            val outputDir = File(context.cacheDir, "receipts")
            if (!outputDir.exists()) outputDir.mkdirs()
            val pdfFile = File(outputDir, "purchase_${invoice.invoiceNumber}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            document.close()

            return pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun sharePdf(context: Context, pdfFile: File, title: String) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, title)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, title))
        } catch (e: Exception) {
            Toast.makeText(context, "فشل مشاركة الملف: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
