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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.StatSummaryCard
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.ui.viewmodel.GroceryViewModel
import com.example.util.Formatters

data class DashboardMenuItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val route: String,
    val color: Color,
    val bgColor: Color,
    val badge: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: GroceryViewModel,
    onNavigate: (String) -> Unit
) {
    val todaySales by viewModel.todaySales.collectAsState()
    val todayExpenses by viewModel.todayExpenses.collectAsState()
    val todayNet by viewModel.todayNet.collectAsState()
    val totalCustomerDebts by viewModel.totalCustomerDebts.collectAsState()
    val totalSupplierDebts by viewModel.totalSupplierDebts.collectAsState()
    val lowStockProducts by viewModel.lowStockProducts.collectAsState()
    val products by viewModel.products.collectAsState()
    val customers by viewModel.customers.collectAsState()

    val menuItems = listOf(
        DashboardMenuItem(
            title = "المبيعات والفواتير",
            subtitle = "إصدار وإدارة فواتير البيع",
            icon = Icons.Default.Receipt,
            route = "sales",
            color = GreenPrimary,
            bgColor = GreenLight
        ),
        DashboardMenuItem(
            title = "الأصناف والمخزون",
            subtitle = "${products.size} صنف مسجل",
            icon = Icons.Default.Inventory2,
            route = "products",
            color = Color(0xFF00796B),
            bgColor = Color(0xFFE0F2F1),
            badge = if (lowStockProducts.isNotEmpty()) "${lowStockProducts.size} ناقص" else null
        ),
        DashboardMenuItem(
            title = "العملاء والديون",
            subtitle = "${customers.size} عميل",
            icon = Icons.Default.People,
            route = "customers",
            color = GoldAccent,
            bgColor = GoldLight
        ),
        DashboardMenuItem(
            title = "المشتريات والتوريد",
            subtitle = "فواتير البضاعة الواردة",
            icon = Icons.Default.ShoppingCart,
            route = "purchases",
            color = Color(0xFF5E35B1),
            bgColor = Color(0xFFEDE7F6)
        ),
        DashboardMenuItem(
            title = "الموردون والشركات",
            subtitle = "حسابات ومستحقات الموردين",
            icon = Icons.Default.Store,
            route = "suppliers",
            color = Color(0xFFD84315),
            bgColor = Color(0xFFFBE9E7)
        ),
        DashboardMenuItem(
            title = "المصروفات",
            subtitle = "إيجار، كهرباء، عمالة...",
            icon = Icons.Default.MoneyOff,
            route = "expenses",
            color = RedExpense,
            bgColor = RedLight
        ),
        DashboardMenuItem(
            title = "التقارير والأرباح",
            subtitle = "تحليل الأداء المالي",
            icon = Icons.Default.Assessment,
            route = "reports",
            color = Color(0xFF1565C0),
            bgColor = Color(0xFFE3F2FD)
        ),
        DashboardMenuItem(
            title = "الإعدادات العامة",
            subtitle = "الطباعة والبيانات",
            icon = Icons.Default.Settings,
            route = "settings",
            color = Color(0xFF455A64),
            bgColor = Color(0xFFECEFF1)
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "بقالة العزي للمواد الغذائية",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = "نظام المبيعات والمخزون والحسابات",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate("create_sale") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.testTag("fab_new_invoice")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("فاتورة جديدة +", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("home_screen"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Quick Invoice Action Banner
            item {
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .clickable { onNavigate("create_sale") }
                        .testTag("quick_new_invoice_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "إصدار فاتورة بيع جديدة",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                                Text(
                                    text = "كاشير سريع • طباعة حرارية فورية",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                )
                            }
                        }

                        Button(
                            onClick = { onNavigate("create_sale") },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("فتح الكاشير", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Real-time Summary Cards
            item {
                Text(
                    text = "ملخص حركة اليوم (${viewModel.todayDate}):",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryCard(
                        title = "مبيعات اليوم",
                        value = Formatters.formatCurrency(todaySales),
                        icon = Icons.Default.TrendingUp,
                        accentColor = GreenPrimary,
                        backgroundColor = GreenLight,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_today_sales"
                    )

                    StatSummaryCard(
                        title = "مصروفات اليوم",
                        value = Formatters.formatCurrency(todayExpenses),
                        icon = Icons.Default.MoneyOff,
                        accentColor = RedExpense,
                        backgroundColor = RedLight,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_today_expenses"
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatSummaryCard(
                        title = "صافي اليوم",
                        value = Formatters.formatCurrency(todayNet),
                        icon = Icons.Default.PointOfSale,
                        accentColor = if (todayNet >= 0) GreenPrimary else RedExpense,
                        backgroundColor = if (todayNet >= 0) GreenLight else RedLight,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_today_net"
                    )

                    StatSummaryCard(
                        title = "إجمالي ديون العملاء",
                        value = Formatters.formatCurrency(totalCustomerDebts),
                        icon = Icons.Default.AccountBalanceWallet,
                        accentColor = GoldAccent,
                        backgroundColor = GoldLight,
                        modifier = Modifier.weight(1f),
                        testTag = "stat_customer_debts"
                    )
                }
            }

            // Low Stock Alert Banner
            if (lowStockProducts.isNotEmpty()) {
                item {
                    ElevatedCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onNavigate("products") }
                            .testTag("low_stock_warning_card"),
                        colors = CardDefaults.elevatedCardColors(containerColor = Color(0xFFFFF3E0)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF9800).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFE65100),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "تنبيه: أصناف قريبة من النفاد (${lowStockProducts.size} صنف)",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE65100)
                                    )
                                )
                                Text(
                                    text = "اضغط لعرض النواقص وإجراء طلبات التوريد",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFFE65100).copy(alpha = 0.8f),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Navigation Grid Header
            item {
                Text(
                    text = "أقسام الإدارة والعمليات:",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            // 8 Modules Grid
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    menuItems.chunked(2).forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowItems.forEach { item ->
                                DashboardMenuCard(
                                    item = item,
                                    onClick = { onNavigate(item.route) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowItems.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun DashboardMenuCard(
    item: DashboardMenuItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("menu_card_${item.route}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(item.bgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = item.color,
                        modifier = Modifier.size(22.dp)
                    )
                }

                if (item.badge != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(RedExpense)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.badge,
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = item.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontSize = 11.sp
                )
            )
        }
    }
}
