package com.example.data.local.repository

import com.example.data.local.dao.GroceryDao
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
import kotlinx.coroutines.flow.Flow

class GroceryRepository(private val dao: GroceryDao) {

    // --- Products ---
    val allProducts: Flow<List<Product>> = dao.getAllProducts()
    val lowStockProducts: Flow<List<Product>> = dao.getLowStockProducts()

    suspend fun getProductById(id: Long): Product? = dao.getProductById(id)
    suspend fun getProductByBarcode(barcode: String): Product? = dao.getProductByBarcode(barcode)
    suspend fun insertProduct(product: Product): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: Product) = dao.updateProduct(product)
    suspend fun deleteProduct(product: Product) = dao.deleteProduct(product)
    suspend fun addStock(productId: Long, quantity: Double) = dao.adjustProductStock(productId, quantity)

    // --- Customers ---
    val allCustomers: Flow<List<Customer>> = dao.getAllCustomers()
    val totalCustomerDebts: Flow<Double> = dao.getTotalCustomerDebts()

    suspend fun getCustomerById(id: Long): Customer? = dao.getCustomerById(id)
    suspend fun insertCustomer(customer: Customer): Long = dao.insertCustomer(customer)
    suspend fun updateCustomer(customer: Customer) = dao.updateCustomer(customer)
    suspend fun deleteCustomer(customer: Customer) = dao.deleteCustomer(customer)

    fun getCustomerTransactions(customerId: Long): Flow<List<CustomerTransaction>> =
        dao.getCustomerTransactions(customerId)

    suspend fun addCustomerPayment(customerId: Long, amount: Double, statement: String, date: String, time: String) {
        val customer = dao.getCustomerById(customerId) ?: return
        val newBalance = customer.balance - amount
        dao.updateCustomerBalances(customerId, delta = -amount, salesDelta = 0.0, paidDelta = amount)
        dao.insertCustomerTransaction(
            CustomerTransaction(
                customerId = customerId,
                date = date,
                time = time,
                statement = statement,
                amount = 0.0,
                paid = amount,
                remaining = newBalance
            )
        )
    }

    suspend fun deleteCustomerTransaction(transaction: CustomerTransaction) {
        dao.deleteCustomerTransaction(transaction)
    }

    // --- Suppliers ---
    val allSuppliers: Flow<List<Supplier>> = dao.getAllSuppliers()
    val totalSupplierDebts: Flow<Double> = dao.getTotalSupplierDebts()

    suspend fun getSupplierById(id: Long): Supplier? = dao.getSupplierById(id)
    suspend fun insertSupplier(supplier: Supplier): Long = dao.insertSupplier(supplier)
    suspend fun updateSupplier(supplier: Supplier) = dao.updateSupplier(supplier)
    suspend fun deleteSupplier(supplier: Supplier) = dao.deleteSupplier(supplier)

    fun getSupplierTransactions(supplierId: Long): Flow<List<SupplierTransaction>> =
        dao.getSupplierTransactions(supplierId)

    suspend fun addSupplierPayment(supplierId: Long, amount: Double, statement: String, date: String, time: String) {
        val supplier = dao.getSupplierById(supplierId) ?: return
        val newBalance = supplier.balance - amount
        dao.updateSupplierBalances(supplierId, delta = -amount, purchaseDelta = 0.0, paidDelta = amount)
        dao.insertSupplierTransaction(
            SupplierTransaction(
                supplierId = supplierId,
                date = date,
                time = time,
                statement = statement,
                amount = 0.0,
                paid = amount,
                remaining = newBalance
            )
        )
    }

    suspend fun deleteSupplierTransaction(transaction: SupplierTransaction) {
        dao.deleteSupplierTransaction(transaction)
    }

    // --- Sale Invoices ---
    val allSaleInvoices: Flow<List<SaleInvoice>> = dao.getAllSaleInvoices()
    fun getTodaySalesTotal(date: String): Flow<Double> = dao.getTodaySalesTotal(date)
    fun getSaleInvoiceItems(invoiceId: Long): Flow<List<SaleInvoiceItem>> = dao.getSaleInvoiceItems(invoiceId)
    suspend fun getSaleInvoiceItemsDirect(invoiceId: Long): List<SaleInvoiceItem> = dao.getSaleInvoiceItemsDirect(invoiceId)

    suspend fun createSaleInvoice(invoice: SaleInvoice, items: List<SaleInvoiceItem>): Long {
        val invoiceId = dao.insertSaleInvoice(invoice)
        val itemsWithId = items.map { it.copy(invoiceId = invoiceId) }
        dao.insertSaleInvoiceItems(itemsWithId)

        // Decrement product stocks
        items.forEach { item ->
            item.productId?.let { pid ->
                dao.adjustProductStock(pid, -item.quantity)
            }
        }

        // Update customer balance if customer selected
        invoice.customerId?.let { cid ->
            val cust = dao.getCustomerById(cid)
            if (cust != null) {
                val debtDelta = invoice.remainingAmount
                dao.updateCustomerBalances(cid, delta = debtDelta, salesDelta = invoice.total, paidDelta = invoice.paidAmount)
                dao.insertCustomerTransaction(
                    CustomerTransaction(
                        customerId = cid,
                        invoiceId = invoiceId,
                        date = invoice.date,
                        time = invoice.time,
                        statement = "فاتورة مبيعات #${invoice.invoiceNumber}",
                        amount = invoice.total,
                        paid = invoice.paidAmount,
                        remaining = cust.balance + debtDelta
                    )
                )
            }
        }
        return invoiceId
    }

    suspend fun deleteSaleInvoice(invoice: SaleInvoice) {
        val items = dao.getSaleInvoiceItemsDirect(invoice.id)
        // Restore stock
        items.forEach { item ->
            item.productId?.let { pid ->
                dao.adjustProductStock(pid, item.quantity)
            }
        }
        // Rollback customer balance
        invoice.customerId?.let { cid ->
            dao.updateCustomerBalances(cid, delta = -invoice.remainingAmount, salesDelta = -invoice.total, paidDelta = -invoice.paidAmount)
        }
        dao.deleteSaleInvoice(invoice)
    }

    // --- Purchase Invoices ---
    val allPurchaseInvoices: Flow<List<PurchaseInvoice>> = dao.getAllPurchaseInvoices()
    fun getPurchaseInvoiceItems(purchaseId: Long): Flow<List<PurchaseInvoiceItem>> = dao.getPurchaseInvoiceItems(purchaseId)
    suspend fun getPurchaseInvoiceItemsDirect(purchaseId: Long): List<PurchaseInvoiceItem> = dao.getPurchaseInvoiceItemsDirect(purchaseId)

    suspend fun createPurchaseInvoice(invoice: PurchaseInvoice, items: List<PurchaseInvoiceItem>): Long {
        val purchaseId = dao.insertPurchaseInvoice(invoice)
        val itemsWithId = items.map { it.copy(purchaseId = purchaseId) }
        dao.insertPurchaseInvoiceItems(itemsWithId)

        // Increment product stocks
        items.forEach { item ->
            item.productId?.let { pid ->
                dao.adjustProductStock(pid, item.quantity)
            }
        }

        // Update supplier balance if supplier selected
        invoice.supplierId?.let { sid ->
            val supp = dao.getSupplierById(sid)
            if (supp != null) {
                val debtDelta = invoice.remainingAmount
                dao.updateSupplierBalances(sid, delta = debtDelta, purchaseDelta = invoice.total, paidDelta = invoice.paidAmount)
                dao.insertSupplierTransaction(
                    SupplierTransaction(
                        supplierId = sid,
                        purchaseId = purchaseId,
                        date = invoice.date,
                        time = invoice.time,
                        statement = "فاتورة مشتريات #${invoice.invoiceNumber}",
                        amount = invoice.total,
                        paid = invoice.paidAmount,
                        remaining = supp.balance + debtDelta
                    )
                )
            }
        }
        return purchaseId
    }

    suspend fun deletePurchaseInvoice(invoice: PurchaseInvoice) {
        val items = dao.getPurchaseInvoiceItemsDirect(invoice.id)
        items.forEach { item ->
            item.productId?.let { pid ->
                dao.adjustProductStock(pid, -item.quantity)
            }
        }
        invoice.supplierId?.let { sid ->
            dao.updateSupplierBalances(sid, delta = -invoice.remainingAmount, purchaseDelta = -invoice.total, paidDelta = -invoice.paidAmount)
        }
        dao.deletePurchaseInvoice(invoice)
    }

    // --- Expenses ---
    val allExpenses: Flow<List<Expense>> = dao.getAllExpenses()
    fun getTodayExpensesTotal(date: String): Flow<Double> = dao.getTodayExpensesTotal(date)
    suspend fun insertExpense(expense: Expense): Long = dao.insertExpense(expense)
    suspend fun updateExpense(expense: Expense) = dao.updateExpense(expense)
    suspend fun deleteExpense(expense: Expense) = dao.deleteExpense(expense)
}
