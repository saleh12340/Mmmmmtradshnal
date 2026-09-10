package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
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

@Dao
interface GroceryDao {

    // --- Products ---
    @Query("SELECT * FROM products ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE quantity <= minQuantity ORDER BY quantity ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Delete
    suspend fun deleteProduct(product: Product)

    @Query("UPDATE products SET quantity = quantity + :delta WHERE id = :productId")
    suspend fun adjustProductStock(productId: Long, delta: Double)

    // --- Customers ---
    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<Customer>>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomerById(id: Long): Customer?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: Customer): Long

    @Update
    suspend fun updateCustomer(customer: Customer)

    @Delete
    suspend fun deleteCustomer(customer: Customer)

    @Query("UPDATE customers SET balance = balance + :delta, totalSales = totalSales + :salesDelta, totalPaid = totalPaid + :paidDelta WHERE id = :customerId")
    suspend fun updateCustomerBalances(customerId: Long, delta: Double, salesDelta: Double, paidDelta: Double)

    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM customers WHERE balance > 0")
    fun getTotalCustomerDebts(): Flow<Double>

    // --- Customer Transactions ---
    @Query("SELECT * FROM customer_transactions WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getCustomerTransactions(customerId: Long): Flow<List<CustomerTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomerTransaction(transaction: CustomerTransaction): Long

    @Delete
    suspend fun deleteCustomerTransaction(transaction: CustomerTransaction)

    // --- Suppliers ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Long): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Delete
    suspend fun deleteSupplier(supplier: Supplier)

    @Query("UPDATE suppliers SET balance = balance + :delta, totalPurchases = totalPurchases + :purchaseDelta, totalPaid = totalPaid + :paidDelta WHERE id = :supplierId")
    suspend fun updateSupplierBalances(supplierId: Long, delta: Double, purchaseDelta: Double, paidDelta: Double)

    @Query("SELECT COALESCE(SUM(balance), 0.0) FROM suppliers WHERE balance > 0")
    fun getTotalSupplierDebts(): Flow<Double>

    // --- Supplier Transactions ---
    @Query("SELECT * FROM supplier_transactions WHERE supplierId = :supplierId ORDER BY timestamp DESC")
    fun getSupplierTransactions(supplierId: Long): Flow<List<SupplierTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplierTransaction(transaction: SupplierTransaction): Long

    @Delete
    suspend fun deleteSupplierTransaction(transaction: SupplierTransaction)

    // --- Sale Invoices ---
    @Query("SELECT * FROM sale_invoices ORDER BY timestamp DESC")
    fun getAllSaleInvoices(): Flow<List<SaleInvoice>>

    @Query("SELECT * FROM sale_invoices WHERE id = :id")
    suspend fun getSaleInvoiceById(id: Long): SaleInvoice?

    @Query("SELECT * FROM sale_invoices WHERE date = :date")
    fun getSaleInvoicesByDate(date: String): Flow<List<SaleInvoice>>

    @Query("SELECT COALESCE(SUM(total), 0.0) FROM sale_invoices WHERE date = :date")
    fun getTodaySalesTotal(date: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleInvoice(invoice: SaleInvoice): Long

    @Delete
    suspend fun deleteSaleInvoice(invoice: SaleInvoice)

    // --- Sale Invoice Items ---
    @Query("SELECT * FROM sale_invoice_items WHERE invoiceId = :invoiceId")
    fun getSaleInvoiceItems(invoiceId: Long): Flow<List<SaleInvoiceItem>>

    @Query("SELECT * FROM sale_invoice_items WHERE invoiceId = :invoiceId")
    suspend fun getSaleInvoiceItemsDirect(invoiceId: Long): List<SaleInvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSaleInvoiceItems(items: List<SaleInvoiceItem>)

    @Query("DELETE FROM sale_invoice_items WHERE invoiceId = :invoiceId")
    suspend fun deleteSaleInvoiceItems(invoiceId: Long)

    // --- Purchase Invoices ---
    @Query("SELECT * FROM purchase_invoices ORDER BY timestamp DESC")
    fun getAllPurchaseInvoices(): Flow<List<PurchaseInvoice>>

    @Query("SELECT * FROM purchase_invoices WHERE id = :id")
    suspend fun getPurchaseInvoiceById(id: Long): PurchaseInvoice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseInvoice(invoice: PurchaseInvoice): Long

    @Delete
    suspend fun deletePurchaseInvoice(invoice: PurchaseInvoice)

    // --- Purchase Invoice Items ---
    @Query("SELECT * FROM purchase_invoice_items WHERE purchaseId = :purchaseId")
    fun getPurchaseInvoiceItems(purchaseId: Long): Flow<List<PurchaseInvoiceItem>>

    @Query("SELECT * FROM purchase_invoice_items WHERE purchaseId = :purchaseId")
    suspend fun getPurchaseInvoiceItemsDirect(purchaseId: Long): List<PurchaseInvoiceItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchaseInvoiceItems(items: List<PurchaseInvoiceItem>)

    // --- Expenses ---
    @Query("SELECT * FROM expenses ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE date = :date")
    fun getTodayExpensesTotal(date: String): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense): Long

    @Update
    suspend fun updateExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)
}
