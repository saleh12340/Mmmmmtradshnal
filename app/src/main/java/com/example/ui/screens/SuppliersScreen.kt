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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.entities.Supplier
import com.example.data.local.entities.SupplierTransaction
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GrocerySearchBar
import com.example.ui.components.GroceryTopBar
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
fun SuppliersScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val suppliers by viewModel.suppliers.collectAsState()
    val totalSupplierDebts by viewModel.totalSupplierDebts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var filterWithDebtsOnly by remember { mutableStateOf(false) }

    var isAddingNew by remember { mutableStateOf(false) }
    var editingSupplier by remember { mutableStateOf<Supplier?>(null) }
    var selectedSupplierForAccount by remember { mutableStateOf<Supplier?>(null) }
    var payingSupplier by remember { mutableStateOf<Supplier?>(null) }

    val filteredSuppliers = suppliers.filter { sup ->
        val matchesQuery = sup.name.contains(searchQuery, ignoreCase = true) ||
                sup.phone.contains(searchQuery) ||
                sup.company.contains(searchQuery, ignoreCase = true)
        val matchesDebt = if (filterWithDebtsOnly) sup.balance > 0 else true
        matchesQuery && matchesDebt
    }

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "الموردون والشركات",
                subtitle = "مستحقات للموردين: ${Formatters.formatCurrency(totalSupplierDebts)}",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_supplier")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة مورد")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GrocerySearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                placeholder = "بحث باسم المورد، الشركة، أو رقم الهاتف..."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilterChip(
                    selected = !filterWithDebtsOnly,
                    onClick = { filterWithDebtsOnly = false },
                    label = { Text("جميع الموردين (${suppliers.size})") }
                )
                FilterChip(
                    selected = filterWithDebtsOnly,
                    onClick = { filterWithDebtsOnly = true },
                    label = { Text("عليهم مستحقات (${suppliers.count { it.balance > 0 }})") }
                )
            }

            if (filteredSuppliers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Store,
                    title = if (suppliers.isEmpty()) "لا يوجد موردون مسجلون" else "لا توجد نتائج مطابقة",
                    subtitle = "اضغط على زر (+) لإضافة مورد أو شركة جديدة لتسجيل المشتريات والدفعات"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            onClick = { selectedSupplierForAccount = supplier },
                            onEdit = { editingSupplier = supplier },
                            onPay = { payingSupplier = supplier },
                            onDelete = { viewModel.deleteSupplier(supplier) }
                        )
                    }
                }
            }
        }
    }

    if (isAddingNew || editingSupplier != null) {
        SupplierFormDialog(
            initialSupplier = editingSupplier,
            onDismiss = {
                isAddingNew = false
                editingSupplier = null
            },
            onSave = { name, phone, company, initialBalance, notes ->
                viewModel.saveSupplier(
                    id = editingSupplier?.id ?: 0L,
                    name = name,
                    phone = phone,
                    company = company,
                    initialBalance = initialBalance,
                    notes = notes
                )
                isAddingNew = false
                editingSupplier = null
            }
        )
    }

    payingSupplier?.let { sup ->
        SupplierPaymentDialog(
            supplier = sup,
            onDismiss = { payingSupplier = null },
            onConfirm = { amount, statement ->
                viewModel.addSupplierPayment(sup.id, amount, statement)
                payingSupplier = null
            }
        )
    }

    selectedSupplierForAccount?.let { sup ->
        SupplierAccountStatementDialog(
            supplier = sup,
            viewModel = viewModel,
            onDismiss = { selectedSupplierForAccount = null },
            onAddPayment = {
                payingSupplier = sup
            }
        )
    }
}

@Composable
fun SupplierCard(
    supplier: Supplier,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onPay: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("supplier_card_${supplier.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFFCCBC)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Store,
                            contentDescription = null,
                            tint = Color(0xFFD84315),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = supplier.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (supplier.company.isNotBlank()) {
                            Text(
                                text = supplier.company,
                                style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = if (supplier.balance > 0) "مستحق له:" else "الرصيد:",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                    Text(
                        text = Formatters.formatCurrency(supplier.balance),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (supplier.balance > 0) RedExpense else GreenPrimary
                        )
                    )
                }
            }

            if (supplier.phone.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = supplier.phone,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onPay,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("سداد دفعة", fontSize = 12.sp)
                }

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "حذف", tint = RedExpense, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierFormDialog(
    initialSupplier: Supplier?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, Double, String) -> Unit
) {
    var name by remember { mutableStateOf(initialSupplier?.name ?: "") }
    var phone by remember { mutableStateOf(initialSupplier?.phone ?: "") }
    var company by remember { mutableStateOf(initialSupplier?.company ?: "") }
    var initialBalance by remember { mutableStateOf(if (initialSupplier == null) "" else "") }
    var notes by remember { mutableStateOf(initialSupplier?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialSupplier == null) "إضافة مورد / شركة جديدة" else "تعديل بيانات المورد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم المندوب أو الشخص المسؤول *") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = company,
                    onValueChange = { company = it },
                    label = { Text("اسم الشركة أو المؤسسة") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialSupplier == null) {
                    OutlinedTextField(
                        value = initialBalance,
                        onValueChange = { initialBalance = it },
                        label = { Text("رصيد افتتاحي سابق له (إن وجد)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("إلغاء") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val initBal = initialBalance.toDoubleOrNull() ?: 0.0
                                onSave(name, phone, company, initBal, notes)
                            }
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

@Composable
fun SupplierPaymentDialog(
    supplier: Supplier,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    var amountText by remember { mutableStateOf("") }
    var statementText by remember { mutableStateOf("سداد دفعة نقدية للمورد") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("تسديد دفعة للمورد: ${supplier.name}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "المستحق له الحالي: ${Formatters.formatCurrency(supplier.balance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = RedExpense)
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ المسدد *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = statementText,
                    onValueChange = { statementText = it },
                    label = { Text("البيان / ملاحظة") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (amount > 0) {
                        onConfirm(amount, statementText)
                    }
                }
            ) {
                Text("تأكيد السداد")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("إلغاء") }
        }
    )
}

@Composable
fun SupplierAccountStatementDialog(
    supplier: Supplier,
    viewModel: GroceryViewModel,
    onDismiss: () -> Unit,
    onAddPayment: () -> Unit
) {
    val transactions by viewModel.getSupplierTransactions(supplier.id).collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .height(550.dp)
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
                            text = "كشف حساب: ${supplier.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "المتبقي له: ${Formatters.formatCurrency(supplier.balance)}",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (supplier.balance > 0) RedExpense else GreenPrimary
                            )
                        )
                    }
                    Button(
                        onClick = onAddPayment,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("سداد دفعة", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider()

                if (transactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد حركات مسجلة لهذا المورد بعد", style = MaterialTheme.typography.bodyMedium)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(transactions) { tx ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(text = tx.statement, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(text = "${tx.date} ${tx.time}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (tx.amount > 0) {
                                            Text(text = "مشتريات: ${Formatters.formatCurrency(tx.amount)}", fontSize = 12.sp, color = RedExpense)
                                        }
                                        if (tx.paid > 0) {
                                            Text(text = "مسدد له: ${Formatters.formatCurrency(tx.paid)}", fontSize = 12.sp, color = GreenPrimary, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("إغلاق")
                }
            }
        }
    }
}
