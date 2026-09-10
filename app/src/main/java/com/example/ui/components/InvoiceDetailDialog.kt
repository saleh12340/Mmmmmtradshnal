package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.SaleInvoice
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.ui.viewmodel.GroceryViewModel
import com.example.util.Formatters
import com.example.util.PdfReceiptGenerator
import kotlinx.coroutines.launch

@Composable
fun InvoiceDetailDialog(
    invoice: SaleInvoice,
    viewModel: GroceryViewModel,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val items by viewModel.getSaleInvoiceItems(invoice.id).collectAsState()
    val paperWidth by viewModel.selectedPaperWidth.collectAsState()
    val showUnitPrice by viewModel.showUnitPriceOnReceipt.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isGeneratingPdf by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(600.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "فاتورة مبيعات #${invoice.invoiceNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${invoice.date} ${invoice.time} • ${invoice.paymentType}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Customer Info Banner
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "العميل: ${invoice.customerName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "طريقة الدفع: ${invoice.paymentType}",
                        style = MaterialTheme.typography.bodySmall.copy(color = GreenPrimary, fontWeight = FontWeight.Bold)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Items list
                Text(
                    text = "الأصناف (${items.size}):",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(items, key = { it.id }) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.productName, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                    Text(
                                        text = "${Formatters.formatNumber(item.quantity)} ${item.unit} × ${Formatters.formatNumber(item.unitPrice)} ر.ي",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                                    )
                                }
                                Text(
                                    text = "${Formatters.formatNumber(item.totalPrice)} ر.ي",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Financial Summary
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(GreenLight)
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإجمالي الصافي:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                            Text(Formatters.formatCurrency(invoice.total), style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary))
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المدفوع:", style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.formatCurrency(invoice.paidAmount), style = MaterialTheme.typography.bodySmall)
                        }
                        if (invoice.remainingAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المتبقي (آجل/دين):", style = MaterialTheme.typography.bodySmall.copy(color = RedExpense, fontWeight = FontWeight.Bold))
                                Text(Formatters.formatCurrency(invoice.remainingAmount), style = MaterialTheme.typography.bodySmall.copy(color = RedExpense, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons (Print / Share / Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isGeneratingPdf = true
                                val directItems = viewModel.getSaleInvoiceItemsDirect(invoice.id)
                                val pdfFile = PdfReceiptGenerator.generateSaleInvoicePdf(
                                    context = context,
                                    invoice = invoice,
                                    items = directItems,
                                    paperWidth = paperWidth,
                                    showUnitPrice = showUnitPrice
                                )
                                isGeneratingPdf = false
                                if (pdfFile != null) {
                                    PdfReceiptGenerator.sharePdf(context, pdfFile, "فاتورة مبيعات #${invoice.invoiceNumber}")
                                } else {
                                    Toast.makeText(context, "فشل إنشاء ملف الفاتورة", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("dialog_print_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة حرارية / PDF", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedExpense),
                        modifier = Modifier.testTag("dialog_delete_invoice_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف الفاتورة") },
            text = { Text("هل أنت متأكد من حذف الفاتورة رقم #${invoice.invoiceNumber}؟ سيتم استرجاع الكميات للمخزون وتعديل رصيد العميل.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSaleInvoice(invoice)
                        showDeleteConfirm = false
                        onDeleted()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
                ) {
                    Text("تأكيد الحذف")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
