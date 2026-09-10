package com.example.ui.screens

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
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
import com.example.data.local.entities.Expense
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GrocerySearchBar
import com.example.ui.components.GroceryTopBar
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.ui.viewmodel.GroceryViewModel
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpensesScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val expenses by viewModel.expenses.collectAsState()
    val todayExpenses by viewModel.todayExpenses.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }

    var isAddingNew by remember { mutableStateOf(false) }
    var editingExpense by remember { mutableStateOf<Expense?>(null) }

    val categories = listOf("الكل", "إيجار", "كهرباء ومياه", "رواتب وعمالة", "نثريات وضيافة", "صيانة", "أخرى")

    val filteredExpenses = expenses.filter { exp ->
        val matchesQuery = exp.title.contains(searchQuery, ignoreCase = true) ||
                exp.notes.contains(searchQuery, ignoreCase = true) ||
                exp.date.contains(searchQuery)
        val matchesCategory = if (selectedCategoryFilter == "الكل") true else exp.category == selectedCategoryFilter
        matchesQuery && matchesCategory
    }

    val totalExpensesSum = filteredExpenses.sumOf { it.amount }

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "سجل المصروفات",
                subtitle = "مصروفات اليوم: ${Formatters.formatCurrency(todayExpenses)}",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = RedExpense,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_expense")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "تسجيل مصروف")
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
                placeholder = "بحث في المصروفات..."
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.take(4).forEach { cat ->
                    FilterChip(
                        selected = selectedCategoryFilter == cat,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(cat, fontSize = 12.sp) }
                    )
                }
            }

            if (filteredExpenses.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MoneyOff,
                    title = if (expenses.isEmpty()) "لا توجد مصروفات مسجلة" else "لا توجد نتائج مطابقة",
                    subtitle = "اضغط على زر (+) لإضافة مصروفات البقالة اليومية والشهرية"
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredExpenses, key = { it.id }) { expense ->
                        ExpenseCard(
                            expense = expense,
                            onEdit = { editingExpense = expense },
                            onDelete = { viewModel.deleteExpense(expense) }
                        )
                    }
                }
            }
        }
    }

    if (isAddingNew || editingExpense != null) {
        ExpenseFormDialog(
            initialExpense = editingExpense,
            onDismiss = {
                isAddingNew = false
                editingExpense = null
            },
            onSave = { title, amount, category, notes ->
                viewModel.saveExpense(
                    id = editingExpense?.id ?: 0L,
                    title = title,
                    amount = amount,
                    category = category,
                    notes = notes
                )
                isAddingNew = false
                editingExpense = null
            }
        )
    }
}

@Composable
fun ExpenseCard(
    expense: Expense,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = expense.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${expense.category} • ${expense.date} ${expense.time}",
                    style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                )
                if (expense.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = expense.notes,
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f))
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = Formatters.formatCurrency(expense.amount),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = RedExpense)
                )
                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Edit, contentDescription = "تعديل", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedExpense, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun ExpenseFormDialog(
    initialExpense: Expense?,
    onDismiss: () -> Unit,
    onSave: (String, Double, String, String) -> Unit
) {
    var title by remember { mutableStateOf(initialExpense?.title ?: "") }
    var amountText by remember { mutableStateOf(if (initialExpense != null) initialExpense.amount.toString() else "") }
    var category by remember { mutableStateOf(initialExpense?.category ?: "نثريات وضيافة") }
    var notes by remember { mutableStateOf(initialExpense?.notes ?: "") }

    val categories = listOf("نثريات وضيافة", "إيجار", "كهرباء ومياه", "رواتب وعمالة", "صيانة", "أخرى")

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (initialExpense == null) "تسجيل مصروف جديد" else "تعديل المصروف",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("بيان المصروف * (مثال: فاتورة كهرباء)") },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("المبلغ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Text("التصنيف:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات إضافية") },
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
                            val amount = amountText.toDoubleOrNull() ?: 0.0
                            if (title.isNotBlank() && amount > 0) {
                                onSave(title, amount, category, notes)
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = RedExpense)
                    ) {
                        Text("حفظ المصروف")
                    }
                }
            }
        }
    }
}
