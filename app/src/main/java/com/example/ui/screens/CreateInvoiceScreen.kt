package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.Customer
import com.example.data.local.entities.Product
import com.example.data.local.entities.SaleInvoice
import com.example.data.local.entities.SaleInvoiceItem
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

data class InvoiceDraftItem(
    val productId: Long? = null,
    var productName: String = "",
    var quantity: Double = 1.0,
    var unitPrice: Double = 0.0,
    var costPrice: Double = 0.0,
    var unit: String = "حبة"
) {
    val totalPrice: Double get() = quantity * unitPrice
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateInvoiceScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val customers by viewModel.customers.collectAsState()
    val products by viewModel.products.collectAsState()

    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var customerNameInput by remember { mutableStateOf("عميل نقدي") }
    var showCustomerPicker by remember { mutableStateOf(false) }

    val items = remember { mutableStateListOf<InvoiceDraftItem>() }
    var showProductPicker by remember { mutableStateOf(false) }
    var showCustomItemDialog by remember { mutableStateOf(false) }

    var discountInput by remember { mutableStateOf("0") }
    var paidInput by remember { mutableStateOf("") }
    var paymentType by remember { mutableStateOf("نقدي") }
    var notesInput by remember { mutableStateOf("") }

    var savedInvoiceForPreview by remember { mutableStateOf<SaleInvoice?>(null) }

    val subtotal by remember {
        derivedStateOf { items.sumOf { it.totalPrice } }
    }

    val discount by remember {
        derivedStateOf { discountInput.toDoubleOrNull() ?: 0.0 }
    }

    val total by remember {
        derivedStateOf { (subtotal - discount).coerceAtLeast(0.0) }
    }

    // Default paid amount is total when paymentType == "نقدي", 0 when "آجل"
    val paidAmount by remember {
        derivedStateOf {
            if (paidInput.isBlank()) {
                if (paymentType == "نقدي" || paymentType == "تحويل") total else 0.0
            } else {
                paidInput.toDoubleOrNull() ?: 0.0
            }
        }
    }

    val remainingAmount by remember {
        derivedStateOf { (total - paidAmount).coerceAtLeast(0.0) }
    }

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "فاتورة بيع جديدة",
                subtitle = "رقم الفاتورة يتولد تلقائياً",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Customer Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "العميل",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = customerNameInput,
                            onValueChange = {
                                customerNameInput = it
                                selectedCustomer = null
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("customer_name_input"),
                            label = { Text("اسم العميل") },
                            leadingIcon = {
                                Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Button(
                            onClick = { showCustomerPicker = true },
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("pick_customer_btn"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Text("اختيار عميل", color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 13.sp)
                        }
                    }

                    selectedCustomer?.let { cust ->
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (cust.balance > 0) GoldLight else GreenLight)
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "الهاتف: ${cust.phone.ifBlank { "غير مسجل" }}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "الديون السابقة: ${Formatters.formatCurrency(cust.balance)}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (cust.balance > 0) RedExpense else GreenPrimary
                                )
                            )
                        }
                    }
                }
            }

            // 2. Items List & Add Product Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "الأصناف (${items.size})",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = { showCustomItemDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("صنف يدوي +", fontSize = 12.sp)
                            }

                            Button(
                                onClick = { showProductPicker = true },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .testTag("add_item_btn")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("إضافة صنف", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (items.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "لم تتم إضافة أي صنف بعد. اضغط «إضافة صنف» لاختيار المنتجات.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            items.forEachIndexed { index, item ->
                                DraftItemRow(
                                    item = item,
                                    onQtyChange = { newQty ->
                                        if (newQty > 0) {
                                            items[index] = item.copy(quantity = newQty)
                                        }
                                    },
                                    onPriceChange = { newPrice ->
                                        items[index] = item.copy(unitPrice = newPrice)
                                    },
                                    onDelete = {
                                        items.removeAt(index)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // 3. Totals & Payment Method
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تفاصيل الحساب والدفع",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    // Payment Method Chips (نقدي / آجل / تحويل / أخرى)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("نقدي", "آجل", "تحويل", "أخرى").forEach { type ->
                            FilterChip(
                                selected = paymentType == type,
                                onClick = {
                                    paymentType = type
                                    if (type == "آجل") {
                                        paidInput = "0"
                                    } else if (type == "نقدي" || type == "تحويل") {
                                        paidInput = total.toString()
                                    }
                                },
                                label = { Text(type, fontSize = 12.sp) }
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))

                    // Discount & Paid Inputs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = discountInput,
                            onValueChange = { discountInput = it },
                            label = { Text("الخصم") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("discount_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = if (paidInput.isBlank()) total.toString() else paidInput,
                            onValueChange = { paidInput = it },
                            label = { Text("المدفوع") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("paid_input"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

                    // Summary Box
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(GreenLight)
                            .padding(14.dp)
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "المجموع الفرعي:", style = MaterialTheme.typography.bodyMedium)
                                Text(text = Formatters.formatCurrency(subtotal), style = MaterialTheme.typography.bodyMedium)
                            }
                            if (discount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "الخصم:", style = MaterialTheme.typography.bodyMedium)
                                    Text(text = "- ${Formatters.formatCurrency(discount)}", style = MaterialTheme.typography.bodyMedium.copy(color = RedExpense))
                                }
                            }
                            HorizontalDivider(color = GreenPrimary.copy(alpha = 0.3f))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "إجمالي الفاتورة:", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text(
                                    text = Formatters.formatCurrency(total),
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                                )
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(text = "المدفوع:", style = MaterialTheme.typography.bodyMedium)
                                Text(text = Formatters.formatCurrency(paidAmount), style = MaterialTheme.typography.bodyMedium)
                            }
                            if (remainingAmount > 0) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(
                                        text = "المتبقي (دين / آجل):",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = RedExpense)
                                    )
                                    Text(
                                        text = Formatters.formatCurrency(remainingAmount),
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = RedExpense)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Save and Print Button
            Button(
                onClick = {
                    if (items.isEmpty()) {
                        Toast.makeText(context, "الرجاء إضافة أصناف للفاتورة أولاً", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val invoiceItems = items.map { draft ->
                        SaleInvoiceItem(
                            invoiceId = 0,
                            productId = draft.productId,
                            productName = draft.productName,
                            quantity = draft.quantity,
                            unitPrice = draft.unitPrice,
                            totalPrice = draft.totalPrice,
                            costPrice = draft.costPrice,
                            unit = draft.unit
                        )
                    }

                    viewModel.createSaleInvoice(
                        customerId = selectedCustomer?.id,
                        customerName = customerNameInput.trim().ifBlank { "عميل نقدي" },
                        items = invoiceItems,
                        discount = discount,
                        paidAmount = paidAmount,
                        paymentType = paymentType,
                        notes = notesInput
                    ) { savedInvoice ->
                        Toast.makeText(context, "تم حفظ الفاتورة بنجاح", Toast.LENGTH_SHORT).show()
                        savedInvoiceForPreview = savedInvoice
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("save_invoice_btn"),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "حفظ وإصدار الفاتورة",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }

    // Modal Product Picker Dialog
    if (showProductPicker) {
        ProductPickerModal(
            products = products,
            onDismiss = { showProductPicker = false },
            onSelectProduct = { prod ->
                val existingIndex = items.indexOfFirst { it.productId == prod.id }
                if (existingIndex >= 0) {
                    val existing = items[existingIndex]
                    items[existingIndex] = existing.copy(quantity = existing.quantity + 1.0)
                } else {
                    items.add(
                        InvoiceDraftItem(
                            productId = prod.id,
                            productName = prod.name,
                            quantity = 1.0,
                            unitPrice = prod.price,
                            costPrice = prod.purchasePrice,
                            unit = prod.unit
                        )
                    )
                }
                showProductPicker = false
            }
        )
    }

    // Modal Customer Picker Dialog
    if (showCustomerPicker) {
        CustomerPickerModal(
            customers = customers,
            onDismiss = { showCustomerPicker = false },
            onSelectCustomer = { cust ->
                selectedCustomer = cust
                customerNameInput = cust.name
                showCustomerPicker = false
            }
        )
    }

    // Custom Item Dialog
    if (showCustomItemDialog) {
        var customName by remember { mutableStateOf("") }
        var customPrice by remember { mutableStateOf("") }
        var customQty by remember { mutableStateOf("1") }
        var customUnit by remember { mutableStateOf("حبة") }

        AlertDialog(
            onDismissRequest = { showCustomItemDialog = false },
            title = { Text("إضافة صنف يدوي") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = customName,
                        onValueChange = { customName = it },
                        label = { Text("اسم الصنف") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customPrice,
                        onValueChange = { customPrice = it },
                        label = { Text("سعر البيع") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = customQty,
                        onValueChange = { customQty = it },
                        label = { Text("الكمية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val price = customPrice.toDoubleOrNull() ?: 0.0
                        val qty = customQty.toDoubleOrNull() ?: 1.0
                        if (customName.isNotBlank() && price > 0) {
                            items.add(
                                InvoiceDraftItem(
                                    productId = null,
                                    productName = customName.trim(),
                                    quantity = qty,
                                    unitPrice = price,
                                    costPrice = price,
                                    unit = customUnit
                                )
                            )
                            showCustomItemDialog = false
                        }
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomItemDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }

    // Invoice Detail & Print dialog on saved
    savedInvoiceForPreview?.let { saved ->
        InvoiceDetailDialog(
            invoice = saved,
            viewModel = viewModel,
            onDismiss = {
                savedInvoiceForPreview = null
                onNavigateBack()
            },
            onDeleted = {
                savedInvoiceForPreview = null
                onNavigateBack()
            }
        )
    }
}

@Composable
fun DraftItemRow(
    item: InvoiceDraftItem,
    onQtyChange: (Double) -> Unit,
    onPriceChange: (Double) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.productName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = Formatters.formatCurrency(item.totalPrice),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary
                    )
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "حذف الصنف",
                        tint = RedExpense,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quantity Stepper
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val newQ = item.quantity - 1.0
                            if (newQ > 0) onQtyChange(newQ)
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "إنقاص", modifier = Modifier.size(14.dp))
                    }

                    Text(
                        text = "${Formatters.formatNumber(item.quantity)} ${item.unit}",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )

                    IconButton(
                        onClick = { onQtyChange(item.quantity + 1.0) },
                        modifier = Modifier
                            .size(28.dp)
                            .background(MaterialTheme.colorScheme.surface, CircleShape)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "زيادة", modifier = Modifier.size(14.dp))
                    }
                }

                // Unit Price Editor
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "السعر: ${Formatters.formatCurrency(item.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                }
            }
        }
    }
}

@Composable
fun ProductPickerModal(
    products: List<Product>,
    onDismiss: () -> Unit,
    onSelectProduct: (Product) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = products.filter {
        it.name.contains(query, ignoreCase = true) ||
                it.barcode.contains(query) ||
                it.category.contains(query, ignoreCase = true)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(520.dp)
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
                        text = "اختيار صنف من المخزون",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("بحث بالاسم أو الباركود...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (products.isEmpty()) "لا توجد أصناف في المخزون بعد" else "لا توجد نتائج مطابقة",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { prod ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectProduct(prod) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = prod.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "المتوفر: ${Formatters.formatNumber(prod.quantity)} ${prod.unit} • ${prod.category}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                color = if (prod.quantity <= prod.minQuantity) RedExpense else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                            )
                                        )
                                    }
                                    Text(
                                        text = Formatters.formatCurrency(prod.price),
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = GreenPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerPickerModal(
    customers: List<Customer>,
    onDismiss: () -> Unit,
    onSelectCustomer: (Customer) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val filtered = customers.filter {
        it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(480.dp)
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
                        text = "اختيار عميل",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("بحث باسم العميل أو رقم الهاتف...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (customers.isEmpty()) "لا يوجد عملاء مسجلين" else "لا توجد نتائج مطابقة",
                            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(filtered) { cust ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectCustomer(cust) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = cust.name,
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                        if (cust.phone.isNotBlank()) {
                                            Text(
                                                text = cust.phone,
                                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                            )
                                        }
                                    }
                                    if (cust.balance > 0) {
                                        Text(
                                            text = "دين: ${Formatters.formatCurrency(cust.balance)}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = RedExpense
                                            )
                                        )
                                    } else {
                                        Text(
                                            text = "لا ديون",
                                            style = MaterialTheme.typography.bodySmall.copy(color = GreenPrimary)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

