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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Phone
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.entities.Customer
import com.example.data.local.entities.CustomerTransaction
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
fun CustomersScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val customers by viewModel.customers.collectAsState()
    val totalCustomerDebts by viewModel.totalCustomerDebts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var filterWithDebtOnly by remember { mutableStateOf(false) }

    var editingCustomer by remember { mutableStateOf<Customer?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var selectedCustomerForStatement by remember { mutableStateOf<Customer?>(null) }
    var payingCustomer by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = customers.filter { cust ->
        val matchesQuery = cust.name.contains(searchQuery, ignoreCase = true) ||
                cust.phone.contains(searchQuery)
        val matchesDebt = if (filterWithDebtOnly) cust.balance > 0 else true
        matchesQuery && matchesDebt
    }

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "حسابات العملاء والديون",
                subtitle = "إجمالي الديون القائمة: ${Formatters.formatCurrency(totalCustomerDebts)}",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_customer")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة عميل جديد")
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
                    placeholder = "بحث باسم العميل أو رقم الهاتف..."
                )

                Spacer(modifier = Modifier.height(8.dp))

                FilterChip(
                    selected = filterWithDebtOnly,
                    onClick = { filterWithDebtOnly = !filterWithDebtOnly },
                    label = { Text("عليهم ديون فقط (${customers.count { it.balance > 0 }})", fontSize = 12.sp) }
                )
            }

            if (filteredCustomers.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.People,
                    title = "لا يوجد عملاء مسجلون",
                    subtitle = "اضغط على (+) لإضافة عميل جديد.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredCustomers, key = { it.id }) { customer ->
                        CustomerItemCard(
                            customer = customer,
                            onClick = { selectedCustomerForStatement = customer },
                            onEdit = { editingCustomer = customer },
                            onDelete = { viewModel.deleteCustomer(customer) },
                            onPayment = { payingCustomer = customer }
                        )
                    }
                }
            }
        }
    }

    if (isAddingNew || editingCustomer != null) {
        CustomerFormDialog(
            initialCustomer = editingCustomer,
            onDismiss = {
                isAddingNew = false
                editingCustomer = null
            },
            onSave = { name, phone, initialBalance, notes ->
                viewModel.saveCustomer(
                    id = editingCustomer?.id ?: 0L,
                    name = name,
                    phone = phone,
                    initialBalance = initialBalance,
                    notes = notes
                )
                isAddingNew = false
                editingCustomer = null
            }
        )
    }

    payingCustomer?.let { cust ->
        CustomerPaymentDialog(
            customer = cust,
            onDismiss = { payingCustomer = null },
            onConfirm = { amount, statement ->
                viewModel.addCustomerPayment(cust.id, amount, statement)
                payingCustomer = null
            }
        )
    }

    selectedCustomerForStatement?.let { cust ->
        CustomerStatementDialog(
            customer = cust,
            viewModel = viewModel,
            onDismiss = { selectedCustomerForStatement = null },
            onAddPayment = {
                payingCustomer = cust
            }
        )
    }
}

@Composable
fun CustomerItemCard(
    customer: Customer,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPayment: () -> Unit
) {
    val hasDebt = customer.balance > 0
    val debtColor = if (hasDebt) RedExpense else GreenPrimary
    val debtBg = if (hasDebt) RedLight else GreenLight

    var showDeleteConfirm by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("customer_card_${customer.id}"),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    if (customer.phone.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(debtBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (hasDebt) "دين عليه" else "خالص الحساب",
                            color = debtColor,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp)
                        )
                        Text(
                            text = Formatters.formatCurrency(customer.balance),
                            color = debtColor,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("المبيعات: ${Formatters.formatCurrency(customer.totalSales)}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = Color.Gray))
                    Text("المسدد: ${Formatters.formatCurrency(customer.totalPaid)}", style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp, color = GreenPrimary))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (customer.balance > 0) {
                        IconButton(onClick = onPayment, modifier = Modifier.size(36.dp)) {
                            Icon(Icons.Default.Payments, contentDescription = "سداد دفعة", tint = GreenPrimary)
                        }
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { showDeleteConfirm = true }, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedExpense)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف العميل") },
            text = { Text("هل أنت متأكد من حذف (${customer.name}) وكافة معاملاته؟") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
                ) {
                    Text("حذف")
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

@Composable
fun CustomerFormDialog(
    initialCustomer: Customer?,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, initialBalance: Double, notes: String) -> Unit
) {
    var name by remember { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember { mutableStateOf(initialCustomer?.phone ?: "") }
    var balanceStr by remember { mutableStateOf(if (initialCustomer != null) initialCustomer.balance.toString() else "0") }
    var notes by remember { mutableStateOf(initialCustomer?.notes ?: "") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialCustomer == null) "إضافة عميل جديد" else "تعديل بيانات العميل",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم العميل *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("رقم الهاتف") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (initialCustomer == null) {
                    OutlinedTextField(
                        value = balanceStr,
                        onValueChange = { balanceStr = it },
                        label = { Text("رصيد سابق / دين أولي (ر.ي)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val bal = balanceStr.toDoubleOrNull() ?: 0.0
                            if (name.isNotBlank()) {
                                onSave(name, phone, bal, notes)
                            }
                        },
                        enabled = name.isNotBlank()
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerPaymentDialog(
    customer: Customer,
    onDismiss: () -> Unit,
    onConfirm: (amount: Double, statement: String) -> Unit
) {
    var amountStr by remember { mutableStateOf(customer.balance.toString()) }
    var statement by remember { mutableStateOf("سداد دفعة نقدية من الحساب") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "سداد دفعة من حساب: ${customer.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "الدين الحالي: ${Formatters.formatCurrency(customer.balance)}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = RedExpense, fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("المبلغ المدفوع (ر.ي) *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = statement,
                    onValueChange = { statement = it },
                    label = { Text("البيان / الملاحظة") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("إلغاء")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val a = amountStr.toDoubleOrNull() ?: 0.0
                            if (a > 0) {
                                onConfirm(a, statement)
                            }
                        },
                        enabled = (amountStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("تسجيل السداد")
                    }
                }
            }
        }
    }
}

@Composable
fun CustomerStatementDialog(
    customer: Customer,
    viewModel: GroceryViewModel,
    onDismiss: () -> Unit,
    onAddPayment: () -> Unit
) {
    val transactions by viewModel.getCustomerTransactions(customer.id).collectAsState(initial = emptyList())

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
                            text = "كشف حساب: ${customer.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        if (customer.phone.isNotBlank()) {
                            Text(text = "هاتف: ${customer.phone}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (customer.balance > 0) RedLight else GreenLight)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("الرصيد المتبقي (الدين):", style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = Formatters.formatCurrency(customer.balance),
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (customer.balance > 0) RedExpense else GreenPrimary
                                )
                            )
                        }

                        if (customer.balance > 0) {
                            Button(
                                onClick = onAddPayment,
                                colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("تسديد دفعة", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text("سجل الحركات والمعاملات (${transactions.size}):", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(6.dp))

                if (transactions.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("لا توجد حركات مسجلة لهذا العميل بعد", color = Color.Gray, fontSize = 13.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(transactions, key = { it.id }) { tx ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text(text = tx.statement, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                                        Text(text = "${tx.date} ${tx.time}", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 10.sp))
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        if (tx.amount > 0) Text("المبلغ: ${Formatters.formatCurrency(tx.amount)}", fontSize = 11.sp)
                                        if (tx.paid > 0) Text("المدفوع: ${Formatters.formatCurrency(tx.paid)}", fontSize = 11.sp, color = GreenPrimary)
                                        Text("الرصيد بعد الحركة: ${Formatters.formatCurrency(tx.remaining)}", fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
