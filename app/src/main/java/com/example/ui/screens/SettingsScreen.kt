package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GroceryTopBar
import com.example.ui.viewmodel.GroceryViewModel
import com.example.util.PdfReceiptGenerator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: GroceryViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val currentPaperWidth by viewModel.selectedPaperWidth.collectAsState()
    val showUnitPrice by viewModel.showUnitPriceOnReceipt.collectAsState()

    Scaffold(
        topBar = {
            GroceryTopBar(
                title = "الإعدادات العامة",
                subtitle = "خيارات الطباعة والمتجر",
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
            // Store Info Card
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "بيانات المتجر والترويسة",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "اسم المنشأة: بقالة العزي للمواد الغذائية",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "العنوان: صعدة - الشارع العام",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "الهاتف: 777000000",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "شعار الفاتورة: خدمة سريعة • أسعار منافسة • جودة عالية",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                    )
                }
            }

            // Receipt & Thermal Printer Settings
            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Print, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "إعدادات الطابعة الحرارية والفواتير",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    Text("عرض ورق الطابعة الحرارية:", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = currentPaperWidth == PdfReceiptGenerator.PaperWidth.MM80,
                            onClick = {
                                viewModel.selectedPaperWidth.value = PdfReceiptGenerator.PaperWidth.MM80
                                Toast.makeText(context, "تم ضبط الورق على 80mm", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("ورق 80mm (قياسي كبير)") }
                        )

                        FilterChip(
                            selected = currentPaperWidth == PdfReceiptGenerator.PaperWidth.MM58,
                            onClick = {
                                viewModel.selectedPaperWidth.value = PdfReceiptGenerator.PaperWidth.MM58
                                Toast.makeText(context, "تم ضبط الورق على 58mm", Toast.LENGTH_SHORT).show()
                            },
                            label = { Text("ورق 58mm (بلوتوث صغير)") }
                        )
                    }

                    HorizontalDivider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("إظهار سعر الوحدة في الفاتورة", fontWeight = FontWeight.Medium)
                            Text("عرض سعر الحبة بجانب الإجمالي في سند القبض", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        }
                        Switch(
                            checked = showUnitPrice,
                            onCheckedChange = { viewModel.showUnitPriceOnReceipt.value = it }
                        )
                    }
                }
            }

            // Database & System Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text(
                            text = "عن النظام والتخزين",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                    Text(
                        text = "قاعدة البيانات: Room Database (تخزين محلي آمن ومشفر داخل الهاتف بدون إنترنت)",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "محرك PDF: Vector Native Android Canvas Engine",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "الإصدار: 1.0.0 (بقالة العزي للمواد الغذائية)",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}
