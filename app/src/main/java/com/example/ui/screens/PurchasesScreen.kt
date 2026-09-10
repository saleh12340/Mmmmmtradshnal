package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.Product
import com.example.data.local.entities.PurchaseInvoice
import com.example.data.local.entities.PurchaseInvoiceItem
import com.example.data.local.entities.Supplier
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GrocerySearchBar
import com.example.ui.components.GroceryTopBar
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.ui.viewmodel.GroceryViewModel
import com.example.util.Formatters
import com.example.util.PdfReceiptGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val purchases by viewModel.purchaseInvoices.collectAsState()
    val suppliers by viewModel.suppliers.collectAsState()
    val products by viewModel.products.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isAddingPurchase by remember { mutableStateOf(false) }
    var selectedPurchaseForDetail by remember { mutableStateOf<PurchaseInvoice?>(null) }

    val filteredPurchases = purchases.filter { p ->
        p.invoiceNumber.contains(searchQuery, ignoreCase = true) ||
                p.supplierName.contains(searchQuery, ignoreCase = true) ||
                p.date.contains(searchQuery)
    }

    val totalPurchasesSum = purchases.sumOf { it.total }

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "فواتير المشتريات والتوريد",
                subtitle = "إجمالي المشتريات: ${Formatters.formatCurrency(totalPurchasesSum)} (${purchases.size} فاتورة)",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingPurchase = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_new_purchase")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "فاتورة مشتريات جديدة")
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
                    placeholder = "بحث برقم الفاتورة أو اسم المورد..."
                )
            }

            if (filteredPurchases.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.ShoppingCart,
                    title = "لا توجد فواتير مشتريات بعد",
                    subtitle = "اضغط على (+) لتسجيل بضاعة واردة من مورد.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPurchases, key = { it.id }) { purchase ->
                        PurchaseItemCard(
                            purchase = purchase,
                            onClick = { selectedPurchaseForDetail = purchase }
                        )
                    }
                }
            }
        }
    }

    if (isAddingPurchase) {
        CreatePurchaseInvoiceDialog(
            suppliers = suppliers,
            products = products,
            onDismiss = { isAddingPurchase = false },
            onSave = { supplierId, supplierName, items, total, paid, notes ->
                viewModel.createPurchaseInvoice(
                    supplierId = supplierId,
                    supplierName = supplierName,
                    items = items,
                    paidAmount = paid,
                    paymentType = if (paid >= total) "نقداً" else if (paid > 0) "دفعة جزئية" else "آجل",
                    notes = notes,
                    onComplete = {
                        isAddingPurchase = false
                    }
                )
            }
        )
    }

    selectedPurchaseForDetail?.let { purchase ->
        PurchaseDetailDialog(
            purchase = purchase,
            viewModel = viewModel,
            onDismiss = { selectedPurchaseForDetail = null },
            onDeleted = { selectedPurchaseForDetail = null }
        )
    }
}

@Composable
fun PurchaseItemCard(
    purchase: PurchaseInvoice,
    onClick: () -> Unit
) {
    val isCredit = purchase.remainingAmount > 0
    val badgeColor = if (isCredit) RedExpense else GreenPrimary
    val badgeBg = if (isCredit) RedLight else GreenLight

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("purchase_card_${purchase.id}"),
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
                Column {
                    Text(
                        text = "#${purchase.invoiceNumber}",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "المورد: ${purchase.supplierName}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(badgeBg)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isCredit) "متبقي له: ${Formatters.formatCurrency(purchase.remainingAmount)}" else purchase.paymentType,
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
                    text = "${purchase.date} • ${purchase.time}",
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray)
                )

                Text(
                    text = "الإجمالي: ${Formatters.formatCurrency(purchase.total)}",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePurchaseInvoiceDialog(
    suppliers: List<Supplier>,
    products: List<Product>,
    onDismiss: () -> Unit,
    onSave: (supplierId: Long?, supplierName: String, items: List<PurchaseInvoiceItem>, total: Double, paid: Double, notes: String) -> Unit
) {
    var selectedSupplier by remember { mutableStateOf<Supplier?>(null) }
    var supplierExpanded by remember { mutableStateOf(false) }

    val draftItems = remember { mutableStateListOf<PurchaseInvoiceItem>() }

    // Item input state
    var selectedProduct by remember { mutableStateOf<Product?>(null) }
    var productExpanded by remember { mutableStateOf(false) }
    var customItemName by remember { mutableStateOf("") }
    var itemQtyStr by remember { mutableStateOf("1") }
    var itemCostStr by remember { mutableStateOf("") }
    var itemUnit by remember { mutableStateOf("حبة") }

    var paidAmountStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val totalAmount = draftItems.sumOf { it.totalPrice }
    val paidAmount = paidAmountStr.toDoubleOrNull() ?: totalAmount

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(650.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "تسجيل فاتورة مشتريات / بضاعة",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Supplier Picker Dropdown
                ExposedDropdownMenuBox(
                    expanded = supplierExpanded,
                    onExpandedChange = { supplierExpanded = !supplierExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedSupplier?.name ?: "مورد نقدي عام",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("المورد") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = supplierExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = supplierExpanded,
                        onDismissRequest = { supplierExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("مورد نقدي عام") },
                            onClick = {
                                selectedSupplier = null
                                supplierExpanded = false
                            }
                        )
                        suppliers.forEach { supp ->
                            DropdownMenuItem(
                                text = { Text("${supp.name} (${supp.company})") },
                                onClick = {
                                    selectedSupplier = supp
                                    supplierExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Add Item Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("إضافة صنف للفاتورة:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp))

                        // Product selector
                        ExposedDropdownMenuBox(
                            expanded = productExpanded,
                            onExpandedChange = { productExpanded = !productExpanded }
                        ) {
                            OutlinedTextField(
                                value = selectedProduct?.name ?: customItemName,
                                onValueChange = {
                                    customItemName = it
                                    selectedProduct = null
                                },
                                label = { Text("اختر صنفاً أو اكتب اسماً جديداً") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = productExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = productExpanded,
                                onDismissRequest = { productExpanded = false }
                            ) {
                                products.forEach { prod ->
                                    DropdownMenuItem(
                                        text = { Text("${prod.name} (سعر الشراء: ${prod.purchasePrice})") },
                                        onClick = {
                                            selectedProduct = prod
                                            customItemName = prod.name
                                            itemCostStr = if (prod.purchasePrice > 0) prod.purchasePrice.toString() else prod.price.toString()
                                            itemUnit = prod.unit
                                            productExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = itemQtyStr,
                                onValueChange = { itemQtyStr = it },
                                label = { Text("الكمية") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            OutlinedTextField(
                                value = itemCostStr,
                                onValueChange = { itemCostStr = it },
                                label = { Text("سعر الشراء (ر.ي)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )

                            Button(
                                onClick = {
                                    val q = itemQtyStr.toDoubleOrNull() ?: 1.0
                                    val cost = itemCostStr.toDoubleOrNull() ?: 0.0
                                    val name = selectedProduct?.name ?: customItemName
                                    if (name.isNotBlank() && cost > 0 && q > 0) {
                                        draftItems.add(
                                            PurchaseInvoiceItem(
                                                purchaseId = 0,
                                                productId = selectedProduct?.id,
                                                productName = name,
                                                quantity = q,
                                                unitPrice = cost,
                                                totalPrice = q * cost,
                                                unit = itemUnit
                                            )
                                        )
                                        // Reset fields
                                        selectedProduct = null
                                        customItemName = ""
                                        itemQtyStr = "1"
                                        itemCostStr = ""
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.CenterVertically)
                            ) {
                                Text("+ إضافة")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Items list
                Text("قائمة الأصناف المضافة (${draftItems.size}):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(draftItems) { item ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.productName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${Formatters.formatNumber(item.quantity)} ${item.unit} × ${Formatters.formatNumber(item.unitPrice)} = ${Formatters.formatCurrency(item.totalPrice)}", fontSize = 11.sp, color = Color.Gray)
                                }
                                IconButton(onClick = { draftItems.remove(item) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedExpense)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Totals and Payment
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "الإجمالي: ${Formatters.formatCurrency(totalAmount)}",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    )

                    OutlinedTextField(
                        value = paidAmountStr,
                        onValueChange = { paidAmountStr = it },
                        placeholder = { Text("المسدد (افتراضي: الكل)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val paid = paidAmountStr.toDoubleOrNull() ?: totalAmount
                        onSave(
                            selectedSupplier?.id,
                            selectedSupplier?.name ?: "مورد نقدي",
                            draftItems.toList(),
                            totalAmount,
                            paid,
                            notes
                        )
                    },
                    enabled = draftItems.isNotEmpty(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("حفظ فاتورة المشتريات وتحديث المخزون", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun PurchaseDetailDialog(
    purchase: PurchaseInvoice,
    viewModel: GroceryViewModel,
    onDismiss: () -> Unit,
    onDeleted: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val items by viewModel.getPurchaseInvoiceItems(purchase.id).collectAsState(initial = emptyList())
    val paperWidth by viewModel.selectedPaperWidth.collectAsState()

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "فاتورة مشتريات #${purchase.invoiceNumber}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "المورد: ${purchase.supplierName} • ${purchase.date}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("الأصناف الواردة (${items.size}):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))

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
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(item.productName, fontWeight = FontWeight.Bold)
                                    Text("${Formatters.formatNumber(item.quantity)} ${item.unit} × ${Formatters.formatNumber(item.unitPrice)} ر.ي", fontSize = 11.sp, color = Color.Gray)
                                }
                                Text(Formatters.formatCurrency(item.totalPrice), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإجمالي:", fontWeight = FontWeight.Bold)
                            Text(Formatters.formatCurrency(purchase.total), fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المسدد للمورد:", style = MaterialTheme.typography.bodySmall)
                            Text(Formatters.formatCurrency(purchase.paidAmount), style = MaterialTheme.typography.bodySmall)
                        }
                        if (purchase.remainingAmount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("المتبقي له (دين):", color = RedExpense, fontWeight = FontWeight.Bold)
                                Text(Formatters.formatCurrency(purchase.remainingAmount), color = RedExpense, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            scope.launch {
                                isGeneratingPdf = true
                                val directItems = viewModel.getPurchaseInvoiceItemsDirect(purchase.id)
                                val pdfFile = PdfReceiptGenerator.generatePurchaseInvoicePdf(
                                    context = context,
                                    invoice = purchase,
                                    items = directItems,
                                    paperWidth = paperWidth
                                )
                                isGeneratingPdf = false
                                if (pdfFile != null) {
                                    PdfReceiptGenerator.sharePdf(context, pdfFile, "سند مشتريات #${purchase.invoiceNumber}")
                                } else {
                                    Toast.makeText(context, "فشل إنشاء الملف", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (isGeneratingPdf) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("طباعة سند استلام", fontSize = 12.sp)
                        }
                    }

                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = RedExpense),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف فاتورة المشتريات") },
            text = { Text("هل أنت متأكد من حذف الفاتورة رقم #${purchase.invoiceNumber}؟ سيتم خصم الكميات من المخزون وتعديل رصيد المورد.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePurchaseInvoice(purchase)
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
