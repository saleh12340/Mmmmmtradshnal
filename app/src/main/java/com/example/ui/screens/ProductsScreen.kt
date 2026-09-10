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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.OutlinedButton
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
import com.example.data.local.entities.Product
import com.example.ui.components.EmptyStateView
import com.example.ui.components.GrocerySearchBar
import com.example.ui.components.GroceryTopBar
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.ui.viewmodel.GroceryViewModel
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val products by viewModel.products.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("الكل") }
    var showLowStockOnly by remember { mutableStateOf(false) }

    var editingProduct by remember { mutableStateOf<Product?>(null) }
    var isAddingNew by remember { mutableStateOf(false) }
    var quickStockProduct by remember { mutableStateOf<Product?>(null) }

    val categories = listOf("الكل") + products.map { it.category }.distinct()

    val filteredProducts = products.filter { prod ->
        val matchesQuery = prod.name.contains(searchQuery, ignoreCase = true) ||
                prod.barcode.contains(searchQuery, ignoreCase = true)
        val matchesCategory = if (selectedCategoryFilter == "الكل") true else prod.category == selectedCategoryFilter
        val matchesLowStock = if (showLowStockOnly) prod.quantity <= prod.minQuantity else true
        matchesQuery && matchesCategory && matchesLowStock
    }

    val totalStockValue = products.sumOf { it.quantity * it.price }

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "الأصناف والمخزون",
                subtitle = "القيمة البيعية للمخزون: ${Formatters.formatCurrency(totalStockValue)}",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { isAddingNew = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("fab_add_product")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "إضافة صنف جديد")
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
                    placeholder = "بحث باسم الصنف أو الباركود..."
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showLowStockOnly,
                        onClick = { showLowStockOnly = !showLowStockOnly },
                        label = { Text("النواقص (${lowStockProducts.size})", fontSize = 11.sp) },
                        leadingIcon = if (showLowStockOnly) {
                            { Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp), tint = RedExpense) }
                        } else null
                    )

                    categories.take(3).forEach { cat ->
                        FilterChip(
                            selected = selectedCategoryFilter == cat && !showLowStockOnly,
                            onClick = {
                                selectedCategoryFilter = cat
                                showLowStockOnly = false
                            },
                            label = { Text(cat, fontSize = 11.sp) }
                        )
                    }
                }
            }

            if (filteredProducts.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Inventory2,
                    title = "لا توجد أصناف مسجلة",
                    subtitle = "اضغط على (+) لإضافة صنف جديد للمخزون.",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { product ->
                        ProductItemCard(
                            product = product,
                            onEdit = { editingProduct = product },
                            onDelete = { viewModel.deleteProduct(product) },
                            onQuickAddStock = { quickStockProduct = product }
                        )
                    }
                }
            }
        }
    }

    if (isAddingNew || editingProduct != null) {
        ProductFormDialog(
            initialProduct = editingProduct,
            onDismiss = {
                isAddingNew = false
                editingProduct = null
            },
            onSave = { name, price, purchasePrice, quantity, minQuantity, unit, barcode, category ->
                viewModel.saveProduct(
                    id = editingProduct?.id ?: 0L,
                    name = name,
                    price = price,
                    purchasePrice = purchasePrice,
                    quantity = quantity,
                    minQuantity = minQuantity,
                    unit = unit,
                    barcode = barcode,
                    category = category
                )
                isAddingNew = false
                editingProduct = null
            }
        )
    }

    quickStockProduct?.let { prod ->
        QuickStockAdjustDialog(
            product = prod,
            onDismiss = { quickStockProduct = null },
            onConfirm = { qty ->
                viewModel.addStock(prod, qty)
                quickStockProduct = null
            }
        )
    }
}

@Composable
fun ProductItemCard(
    product: Product,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onQuickAddStock: () -> Unit
) {
    val isLowStock = product.quantity <= product.minQuantity
    val stockColor = if (isLowStock) RedExpense else GreenPrimary
    val stockBg = if (isLowStock) RedLight else GreenLight

    var showDeleteConfirm by remember { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
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
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "التصنيف: ${product.category}${if (product.barcode.isNotBlank()) " • كود: ${product.barcode}" else ""}",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(stockBg)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "${Formatters.formatNumber(product.quantity)} ${product.unit}",
                        color = stockColor,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp)
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("سعر البيع: ", style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray))
                        Text(
                            text = Formatters.formatCurrency(product.price),
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = GreenPrimary)
                        )
                    }
                    if (product.purchasePrice > 0) {
                        Text(
                            text = "سعر الشراء: ${Formatters.formatCurrency(product.purchasePrice)}",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray, fontSize = 11.sp)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(onClick = onQuickAddStock, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = "تزويد مخزون", tint = GreenPrimary)
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
            title = { Text("حذف الصنف") },
            text = { Text("هل أنت متأكد من حذف (${product.name})؟") },
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
fun ProductFormDialog(
    initialProduct: Product?,
    onDismiss: () -> Unit,
    onSave: (name: String, price: Double, purchasePrice: Double, quantity: Double, minQuantity: Double, unit: String, barcode: String, category: String) -> Unit
) {
    var name by remember { mutableStateOf(initialProduct?.name ?: "") }
    var priceStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.price.toString() else "") }
    var purchasePriceStr by remember { mutableStateOf(if (initialProduct != null && initialProduct.purchasePrice > 0) initialProduct.purchasePrice.toString() else "") }
    var quantityStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.quantity.toString() else "1") }
    var minQuantityStr by remember { mutableStateOf(if (initialProduct != null) initialProduct.minQuantity.toString() else "5") }
    var unit by remember { mutableStateOf(initialProduct?.unit ?: "حبة") }
    var barcode by remember { mutableStateOf(initialProduct?.barcode ?: "") }
    var category by remember { mutableStateOf(initialProduct?.category ?: "مواد غذائية") }

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
                    text = if (initialProduct == null) "إضافة صنف جديد" else "تعديل بيانات الصنف",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("اسم الصنف *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { priceStr = it },
                        label = { Text("سعر البيع (ر.ي) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = purchasePriceStr,
                        onValueChange = { purchasePriceStr = it },
                        label = { Text("سعر الشراء") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = quantityStr,
                        onValueChange = { quantityStr = it },
                        label = { Text("الكمية الحالية") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = unit,
                        onValueChange = { unit = it },
                        label = { Text("الوحدة (حبة/كيس..)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = { category = it },
                        label = { Text("التصنيف") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = minQuantityStr,
                        onValueChange = { minQuantityStr = it },
                        label = { Text("حد التنبيه (النواقص)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = barcode,
                    onValueChange = { barcode = it },
                    label = { Text("الباركود / الرمز") },
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
                            val p = priceStr.toDoubleOrNull() ?: 0.0
                            val pp = purchasePriceStr.toDoubleOrNull() ?: 0.0
                            val q = quantityStr.toDoubleOrNull() ?: 0.0
                            val mq = minQuantityStr.toDoubleOrNull() ?: 5.0
                            if (name.isNotBlank() && p > 0) {
                                onSave(name, p, pp, q, mq, unit, barcode, category)
                            }
                        },
                        enabled = name.isNotBlank() && (priceStr.toDoubleOrNull() ?: 0.0) > 0
                    ) {
                        Text("حفظ")
                    }
                }
            }
        }
    }
}

@Composable
fun QuickStockAdjustDialog(
    product: Product,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var addedQtyStr by remember { mutableStateOf("10") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "تزويد مخزون: ${product.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )

                Text(
                    text = "الرصيد الحالي: ${Formatters.formatNumber(product.quantity)} ${product.unit}",
                    style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray)
                )

                OutlinedTextField(
                    value = addedQtyStr,
                    onValueChange = { addedQtyStr = it },
                    label = { Text("الكمية المضافة") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                            val q = addedQtyStr.toDoubleOrNull() ?: 0.0
                            if (q > 0) {
                                onConfirm(q)
                            }
                        }
                    ) {
                        Text("إضافة للمخزون")
                    }
                }
            }
        }
    }
}
