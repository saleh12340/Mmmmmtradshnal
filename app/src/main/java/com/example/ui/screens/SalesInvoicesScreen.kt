package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.SaleInvoice
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GrocerySearchBar
import com.example.ui.components.GroceryTopBar
import com.example.ui.components.InvoiceDetailDialog
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.ui.viewmodel.GroceryViewModel
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesInvoicesScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit,
    onNewInvoice: () -> Unit
) {
    val invoices by viewModel.saleInvoices.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedPaymentFilter by remember { mutableStateOf("الكل") }
    var selectedInvoiceForDetail by remember { mutableStateOf<SaleInvoice?>(null) }

    val filteredInvoices = invoices.filter { inv ->
        val matchesQuery = inv.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                inv.customerName.contains(searchQuery, ignoreCase = true) ||
                inv.date.contains(searchQuery)
        val matchesPayment = if (selectedPaymentFilter == "الكل") true else inv.paymentType == selectedPaymentFilter
        matchesQuery && matchesPayment
    }

    val totalSalesSum = invoices.sumOf { it.total }

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "فواتير المبيعات",
                subtitle = "إجمالي المبيعات: ${Formatters.formatCurrency(totalSalesSum)} (${invoices.size} فاتورة)",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNewInvoice,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_create_invoice")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "فاتورة جديدة")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                GrocerySearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "بحث برقم الفاتورة أو اسم العميل أو التاريخ..."
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("الكل", "نقدي", "آجل", "تحويل").forEach { filter ->
                        FilterChip(
                            selected = selectedPaymentFilter == filter,
                            onClick = { selectedPaymentFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) }
                        )
                    }
                }
            }

            if (filteredInvoices.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Receipt,
                    title = if (invoices.isEmpty()) "لا توجد فواتير مبيعات بعد" else "لا توجد نتائج مطابقة للبحث",
                    subtitle = if (invoices.isEmpty()) "اضغط على زر (+) لإنشاء أول فاتورة بيع." else "جرب تغيير كلمات البحث أو الفلتر.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredInvoices, key = { it.id }) { invoice ->
                        InvoiceItemCard(
                            invoice = invoice,
                            onClick = { selectedInvoiceForDetail = invoice }
                        )
                    }
                }
            }
        }
    }

    selectedInvoiceForDetail?.let { inv ->
        InvoiceDetailDialog(
            invoice = inv,
            viewModel = viewModel,
            onDismiss = { selectedInvoiceForDetail = null },
            onDeleted = { selectedInvoiceForDetail = null }
        )
    }
}

@Composable
fun InvoiceItemCard(
    invoice: SaleInvoice,
    onClick: () -> Unit
) {
    val isCredit = invoice.remainingAmount > 0
    val badgeColor = if (isCredit) RedExpense else GreenPrimary
    val badgeBg = if (isCredit) RedLight else GreenLight

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("invoice_card_${invoice.invoiceNumber}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(badgeBg),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Receipt,
                            contentDescription = null,
                            tint = badgeColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "#${invoice.invoiceNumber}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "العميل: ${invoice.customerName}",
                            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCredit) "آجل / متبقي: ${Formatters.formatCurrency(invoice.remainingAmount)}" else invoice.paymentType,
                        color = badgeColor,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${invoice.date} • ${invoice.time}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "الإجمالي: ",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                    Text(
                        text = Formatters.formatCurrency(invoice.total),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = GreenPrimary
                        )
                    )
                }
            }
        }
    }
}
