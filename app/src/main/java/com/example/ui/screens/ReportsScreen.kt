package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.MoneyOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GroceryTopBar
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GreenLight
import com.example.ui.theme.GreenPrimary
import com.example.ui.theme.RedExpense
import com.example.ui.theme.RedLight
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.ReportPeriod
import com.example.util.Formatters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val reportSummary by viewModel.reportSummary.collectAsState()
    val selectedPeriod by viewModel.selectedReportPeriod.collectAsState()

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "التقارير والأداء المالي",
                subtitle = "تحليل المبيعات والمشتريات والأرباح",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Period Selector Chips
            Text(
                text = "الفترة الزمنية للتقرير:",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReportPeriod.values().take(4).forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.setReportPeriod(period) },
                        label = { Text(period.title, fontSize = 12.sp) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                ReportPeriod.values().drop(4).forEach { period ->
                    FilterChip(
                        selected = selectedPeriod == period,
                        onClick = { viewModel.setReportPeriod(period) },
                        label = { Text(period.title, fontSize = 12.sp) }
                    )
                }
            }

            // Highlight Profit / Net Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.elevatedCardColors(
                    containerColor = if (reportSummary.estimatedProfit >= 0) GreenLight else RedLight
                )
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "الربح التقديري للفترة (${selectedPeriod.title}):",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = Formatters.formatCurrency(reportSummary.estimatedProfit),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (reportSummary.estimatedProfit >= 0) GreenPrimary else RedExpense
                        )
                    )
                    Text(
                        text = "محسوب كـ (إجمالي المبيعات - المشتريات - المصروفات)",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    )
                }
            }

            // Financial Summary Breakdown Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "تفاصيل الحسابات للفترة المحددة:",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )

                    ReportMetricRow(
                        title = "إجمالي المبيعات",
                        value = Formatters.formatCurrency(reportSummary.totalSales),
                        icon = Icons.Default.TrendingUp,
                        iconColor = GreenPrimary
                    )

                    ReportMetricRow(
                        title = "إجمالي المشتريات (الوارد)",
                        value = Formatters.formatCurrency(reportSummary.totalPurchases),
                        icon = Icons.Default.ShoppingCart,
                        iconColor = Color(0xFF5E35B1)
                    )

                    ReportMetricRow(
                        title = "إجمالي المصروفات والنثريات",
                        value = Formatters.formatCurrency(reportSummary.totalExpenses),
                        icon = Icons.Default.MoneyOff,
                        iconColor = RedExpense
                    )

                    ReportMetricRow(
                        title = "عدد فواتير البيع المصدرة",
                        value = "${reportSummary.invoiceCount} فاتورة",
                        icon = Icons.Default.Receipt,
                        iconColor = Color(0xFF00796B)
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    ReportMetricRow(
                        title = "إجمالي ديون العملاء (تراكمي)",
                        value = Formatters.formatCurrency(reportSummary.totalCustomerDebts),
                        icon = Icons.Default.AccountBalanceWallet,
                        iconColor = GoldAccent
                    )

                    ReportMetricRow(
                        title = "مستحقات الموردين (تراكمي)",
                        value = Formatters.formatCurrency(reportSummary.totalSupplierDebts),
                        icon = Icons.Default.Assessment,
                        iconColor = Color(0xFFD84315)
                    )
                }
            }
        }
    }
}

@Composable
fun ReportMetricRow(
    title: String,
    value: String,
    icon: ImageVector,
    iconColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = iconColor)
        )
    }
}
