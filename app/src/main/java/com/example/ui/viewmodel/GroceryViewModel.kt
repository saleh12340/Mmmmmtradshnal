package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.entities.Customer
import com.example.data.local.entities.CustomerTransaction
import com.example.data.local.entities.Expense
import com.example.data.local.entities.Product
import com.example.data.local.entities.PurchaseInvoice
import com.example.data.local.entities.PurchaseInvoiceItem
import com.example.data.local.entities.SaleInvoice
import com.example.data.local.entities.SaleInvoiceItem
import com.example.data.local.entities.Supplier
import com.example.data.local.entities.SupplierTransaction
import com.example.data.local.repository.GroceryRepository
import com.example.util.Formatters
import com.example.util.PdfReceiptGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class ReportPeriod(val title: String) {
    TODAY("اليوم"),
    YESTERDAY("أمس"),
    THIS_WEEK("هذا الأسبوع"),
    THIS_MONTH("هذا الشهر"),
    THIS_YEAR("هذه السنة"),
    ALL_TIME("الكل")
}

data class ReportSummary(
    val period: ReportPeriod = ReportPeriod.TODAY,
    val totalSales: Double = 0.0,
    val totalPurchases: Double = 0.0,
    val totalExpenses: Double = 0.0,
    val totalCustomerPayments: Double = 0.0,
    val totalCustomerDebts: Double = 0.0,
    val totalSupplierDebts: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val invoiceCount: Int = 0
)

class GroceryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: GroceryRepository

    init {
        val db = AppDatabase.getDatabase(application, viewModelScope)
        repository = GroceryRepository(db.groceryDao())
    }

    // --- Product States ---
    val products: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockProducts: StateFlow<List<Product>> = repository.lowStockProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Customer States ---
    val customers: StateFlow<List<Customer>> = repository.allCustomers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCustomerDebts: StateFlow<Double> = repository.totalCustomerDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Supplier States ---
    val suppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalSupplierDebts: StateFlow<Double> = repository.totalSupplierDebts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Sale Invoices ---
    val saleInvoices: StateFlow<List<SaleInvoice>> = repository.allSaleInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Purchase Invoices ---
    val purchaseInvoices: StateFlow<List<PurchaseInvoice>> = repository.allPurchaseInvoices
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Expenses ---
    val expenses: StateFlow<List<Expense>> = repository.allExpenses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dashboard Aggregations ---
    val todayDate = Formatters.getCurrentDate()

    val todaySales: StateFlow<Double> = repository.getTodaySalesTotal(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayExpenses: StateFlow<Double> = repository.getTodayExpensesTotal(todayDate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayNet: StateFlow<Double> = combine(todaySales, todayExpenses) { sales, exp ->
        sales - exp
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // --- Settings / Preferences ---
    var selectedPaperWidth = MutableStateFlow(PdfReceiptGenerator.PaperWidth.MM80)
    var showUnitPriceOnReceipt = MutableStateFlow(false)

    // --- Reports ---
    private val _selectedReportPeriod = MutableStateFlow(ReportPeriod.TODAY)
    val selectedReportPeriod: StateFlow<ReportPeriod> = _selectedReportPeriod.asStateFlow()

    val reportSummary: StateFlow<ReportSummary> = combine(
        _selectedReportPeriod,
        saleInvoices,
        purchaseInvoices,
        expenses
    ) { period: ReportPeriod, sales: List<SaleInvoice>, purchases: List<PurchaseInvoice>, expList: List<Expense> ->
        val (startTime, endTime) = getTimeBoundsForPeriod(period)

        val filteredSales = sales.filter { it.timestamp in startTime..endTime }
        val filteredPurchases = purchases.filter { it.timestamp in startTime..endTime }
        val filteredExpenses = expList.filter { it.timestamp in startTime..endTime }

        val salesSum = filteredSales.sumOf { it.total }
        val purchasesSum = filteredPurchases.sumOf { it.total }
        val expensesSum = filteredExpenses.sumOf { it.amount }
        val paidSum = filteredSales.sumOf { it.paidAmount }

        // Approximate Net Profit = Sales - Purchases - Expenses
        val estimatedProfit = salesSum - purchasesSum - expensesSum

        ReportSummary(
            period = period,
            totalSales = salesSum,
            totalPurchases = purchasesSum,
            totalExpenses = expensesSum,
            totalCustomerPayments = paidSum,
            totalCustomerDebts = totalCustomerDebts.value,
            totalSupplierDebts = totalSupplierDebts.value,
            estimatedProfit = estimatedProfit,
            invoiceCount = filteredSales.size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ReportSummary())

    fun setReportPeriod(period: ReportPeriod) {
        _selectedReportPeriod.value = period
    }

    private fun getTimeBoundsForPeriod(period: ReportPeriod): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        val now = calendar.timeInMillis

        return when (period) {
            ReportPeriod.TODAY -> {
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                Pair(start, now)
            }
            ReportPeriod.YESTERDAY -> {
                calendar.add(Calendar.DAY_OF_YEAR, -1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val start = calendar.timeInMillis
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                calendar.set(Calendar.SECOND, 59)
                calendar.set(Calendar.MILLISECOND, 999)
                val end = calendar.timeInMillis
                Pair(start, end)
            }
            ReportPeriod.THIS_WEEK -> {
                calendar.firstDayOfWeek = Calendar.SATURDAY
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis
                Pair(start, now)
            }
            ReportPeriod.THIS_MONTH -> {
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis
                Pair(start, now)
            }
            ReportPeriod.THIS_YEAR -> {
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                val start = calendar.timeInMillis
                Pair(start, now)
            }
            ReportPeriod.ALL_TIME -> {
                Pair(0L, Long.MAX_VALUE)
            }
        }
    }

    // --- Product Operations ---
    fun saveProduct(
        id: Long = 0,
        name: String,
        price: Double,
        purchasePrice: Double,
        quantity: Double,
        minQuantity: Double,
        unit: String,
        barcode: String = "",
        category: String = "مواد غذائية"
    ) {
        viewModelScope.launch {
            val product = Product(
                id = id,
                name = name.trim(),
                price = price,
                purchasePrice = purchasePrice,
                quantity = quantity,
                minQuantity = minQuantity,
                unit = unit.trim().ifBlank { "حبة" },
                barcode = barcode.trim(),
                category = category.trim()
            )
            if (id == 0L) {
                repository.insertProduct(product)
            } else {
                repository.updateProduct(product)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            repository.deleteProduct(product)
        }
    }

    fun adjustProductStock(product: Product, delta: Double) {
        viewModelScope.launch {
            repository.addStock(product.id, delta)
        }
    }

    fun addStock(product: Product, delta: Double) {
        adjustProductStock(product, delta)
    }

    // --- Customer Operations ---
    fun saveCustomer(
        id: Long = 0,
        name: String,
        phone: String,
        initialBalance: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val customer = Customer(
                    name = name.trim(),
                    phone = phone.trim(),
                    balance = initialBalance,
                    totalSales = initialBalance,
                    notes = notes.trim()
                )
                val customerId = repository.insertCustomer(customer)
                if (initialBalance > 0) {
                    repository.addCustomerPayment(
                        customerId = customerId,
                        amount = -initialBalance,
                        statement = "رصيد افتتاحي سابق",
                        date = Formatters.getCurrentDate(),
                        time = Formatters.getCurrentTime()
                    )
                }
            } else {
                val existing = repository.getCustomerById(id)
                if (existing != null) {
                    repository.updateCustomer(
                        existing.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            notes = notes.trim()
                        )
                    )
                }
            }
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    fun addCustomerPayment(
        customerId: Long,
        amount: Double,
        statement: String = "دفعة نقدية مسددة"
    ) {
        viewModelScope.launch {
            repository.addCustomerPayment(
                customerId = customerId,
                amount = amount,
                statement = statement,
                date = Formatters.getCurrentDate(),
                time = Formatters.getCurrentTime()
            )
        }
    }

    fun getCustomerTransactions(customerId: Long): StateFlow<List<CustomerTransaction>> {
        return repository.getCustomerTransactions(customerId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun deleteCustomerTransaction(transaction: CustomerTransaction) {
        viewModelScope.launch {
            repository.deleteCustomerTransaction(transaction)
        }
    }

    // --- Supplier Operations ---
    fun saveSupplier(
        id: Long = 0,
        name: String,
        phone: String,
        company: String = "",
        initialBalance: Double = 0.0,
        notes: String = ""
    ) {
        viewModelScope.launch {
            if (id == 0L) {
                val supplier = Supplier(
                    name = name.trim(),
                    phone = phone.trim(),
                    company = company.trim(),
                    balance = initialBalance,
                    totalPurchases = initialBalance,
                    notes = notes.trim()
                )
                val supplierId = repository.insertSupplier(supplier)
                if (initialBalance > 0) {
                    repository.addSupplierPayment(
                        supplierId = supplierId,
                        amount = -initialBalance,
                        statement = "رصيد افتتاحي سابق للمورد",
                        date = Formatters.getCurrentDate(),
                        time = Formatters.getCurrentTime()
                    )
                }
            } else {
                val existing = repository.getSupplierById(id)
                if (existing != null) {
                    repository.updateSupplier(
                        existing.copy(
                            name = name.trim(),
                            phone = phone.trim(),
                            company = company.trim(),
                            notes = notes.trim()
                        )
                    )
                }
            }
        }
    }

    fun deleteSupplier(supplier: Supplier) {
        viewModelScope.launch {
            repository.deleteSupplier(supplier)
        }
    }

    fun addSupplierPayment(
        supplierId: Long,
        amount: Double,
        statement: String = "دفعة مسددة للمورد"
    ) {
        viewModelScope.launch {
            repository.addSupplierPayment(
                supplierId = supplierId,
                amount = amount,
                statement = statement,
                date = Formatters.getCurrentDate(),
                time = Formatters.getCurrentTime()
            )
        }
    }

    fun getSupplierTransactions(supplierId: Long): StateFlow<List<SupplierTransaction>> {
        return repository.getSupplierTransactions(supplierId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    fun deleteSupplierTransaction(transaction: SupplierTransaction) {
        viewModelScope.launch {
            repository.deleteSupplierTransaction(transaction)
        }
    }

    // --- Sale Invoice Operations ---
    fun createSaleInvoice(
        customerId: Long?,
        customerName: String,
        items: List<SaleInvoiceItem>,
        discount: Double,
        paidAmount: Double,
        paymentType: String,
        notes: String = "",
        onComplete: (SaleInvoice) -> Unit
    ) {
        viewModelScope.launch {
            val subtotal = items.sumOf { it.totalPrice }
            val total = (subtotal - discount).coerceAtLeast(0.0)
            val remaining = (total - paidAmount).coerceAtLeast(0.0)
            val invoiceNumber = Formatters.generateInvoiceNumber("INV")

            val invoice = SaleInvoice(
                invoiceNumber = invoiceNumber,
                customerId = customerId,
                customerName = customerName.ifBlank { "عميل نقدي" },
                date = Formatters.getCurrentDate(),
                time = Formatters.getCurrentTime(),
                subtotal = subtotal,
                discount = discount,
                total = total,
                paidAmount = paidAmount,
                remainingAmount = remaining,
                paymentType = paymentType,
                notes = notes
            )

            val invoiceId = repository.createSaleInvoice(invoice, items)
            val savedInvoice = invoice.copy(id = invoiceId)
            onComplete(savedInvoice)
        }
    }

    fun deleteSaleInvoice(invoice: SaleInvoice) {
        viewModelScope.launch {
            repository.deleteSaleInvoice(invoice)
        }
    }

    fun getSaleInvoiceItems(invoiceId: Long): StateFlow<List<SaleInvoiceItem>> {
        return repository.getSaleInvoiceItems(invoiceId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    suspend fun getSaleInvoiceItemsDirect(invoiceId: Long): List<SaleInvoiceItem> {
        return repository.getSaleInvoiceItemsDirect(invoiceId)
    }

    // --- Purchase Invoice Operations ---
    fun createPurchaseInvoice(
        supplierId: Long?,
        supplierName: String,
        items: List<PurchaseInvoiceItem>,
        paidAmount: Double,
        paymentType: String,
        notes: String = "",
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val total = items.sumOf { it.totalPrice }
            val remaining = (total - paidAmount).coerceAtLeast(0.0)
            val invoiceNumber = Formatters.generateInvoiceNumber("PUR")

            val invoice = PurchaseInvoice(
                invoiceNumber = invoiceNumber,
                supplierId = supplierId,
                supplierName = supplierName.ifBlank { "مورد نقدي" },
                date = Formatters.getCurrentDate(),
                time = Formatters.getCurrentTime(),
                total = total,
                paidAmount = paidAmount,
                remainingAmount = remaining,
                paymentType = paymentType,
                notes = notes
            )

            repository.createPurchaseInvoice(invoice, items)
            onComplete()
        }
    }

    fun deletePurchaseInvoice(invoice: PurchaseInvoice) {
        viewModelScope.launch {
            repository.deletePurchaseInvoice(invoice)
        }
    }

    fun getPurchaseInvoiceItems(purchaseId: Long): StateFlow<List<PurchaseInvoiceItem>> {
        return repository.getPurchaseInvoiceItems(purchaseId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    }

    suspend fun getPurchaseInvoiceItemsDirect(purchaseId: Long): List<PurchaseInvoiceItem> {
        return repository.getPurchaseInvoiceItemsDirect(purchaseId)
    }

    // --- Expense Operations ---
    fun saveExpense(
        id: Long = 0,
        title: String,
        amount: Double,
        category: String,
        notes: String = ""
    ) {
        viewModelScope.launch {
            val expense = Expense(
                id = id,
                title = title.trim(),
                amount = amount,
                category = category.trim().ifBlank { "عام" },
                date = Formatters.getCurrentDate(),
                time = Formatters.getCurrentTime(),
                notes = notes.trim()
            )
            if (id == 0L) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
        }
    }
}
