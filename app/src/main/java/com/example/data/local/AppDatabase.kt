package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Customer::class,
        CustomerTransaction::class,
        Supplier::class,
        SupplierTransaction::class,
        SaleInvoice::class,
        SaleInvoiceItem::class,
        PurchaseInvoice::class,
        PurchaseInvoiceItem::class,
        Expense::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun groceryDao(): GroceryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alezzi_grocery_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateInitialData(database.groceryDao())
                }
            }
        }

        suspend fun populateInitialData(dao: GroceryDao) {
            val sampleProducts = listOf(
                Product(name = "أرز بسمتي الشعلان 5 كجم", price = 8500.0, purchasePrice = 7500.0, quantity = 35.0, minQuantity = 5.0, unit = "كيس", category = "أرز وحبوب"),
                Product(name = "سكر الأسرة 10 كجم", price = 12000.0, purchasePrice = 11000.0, quantity = 20.0, minQuantity = 4.0, unit = "كيس", category = "سكر"),
                Product(name = "زيت طبخ عافية 1.5 لتر", price = 3400.0, purchasePrice = 3000.0, quantity = 45.0, minQuantity = 10.0, unit = "حبة", category = "زيوت"),
                Product(name = "حليب الممتاز مجفف 900 جم", price = 4800.0, purchasePrice = 4300.0, quantity = 30.0, minQuantity = 6.0, unit = "علبة", category = "ألبان"),
                Product(name = "شاي الكبوس أحمر 225 جم", price = 1200.0, purchasePrice = 1000.0, quantity = 60.0, minQuantity = 12.0, unit = "باكت", category = "شاي وقهوة"),
                Product(name = "تونة هناء قطعة واحدة 185 جم", price = 950.0, purchasePrice = 800.0, quantity = 50.0, minQuantity = 15.0, unit = "حبة", category = "معلبات"),
                Product(name = "مكرونة قودي 400 جم", price = 600.0, purchasePrice = 480.0, quantity = 70.0, minQuantity = 20.0, unit = "كيس", category = "معكرونة"),
                Product(name = "صلصة طماطم هاينز 135 جم", price = 300.0, purchasePrice = 240.0, quantity = 100.0, minQuantity = 24.0, unit = "حبة", category = "صلصة ومعلبات"),
                Product(name = "صابون غسيل تايد 2.5 كجم", price = 4200.0, purchasePrice = 3700.0, quantity = 25.0, minQuantity = 5.0, unit = "كيس", category = "منظفات"),
                Product(name = "ماء هناء صحي 750 مل", price = 250.0, purchasePrice = 180.0, quantity = 120.0, minQuantity = 30.0, unit = "قارورة", category = "مشروبات")
            )
            dao.insertProducts(sampleProducts)

            val sampleCustomers = listOf(
                Customer(name = "أحمد محمد الحاشدي", phone = "771234567", balance = 6500.0, totalSales = 15000.0, totalPaid = 8500.0),
                Customer(name = "علي بن ناصر العولقي", phone = "773456789", balance = 0.0, totalSales = 22000.0, totalPaid = 22000.0),
                Customer(name = "صالح حسين الريمي", phone = "775678901", balance = 12400.0, totalSales = 35000.0, totalPaid = 22600.0)
            )
            sampleCustomers.forEach { dao.insertCustomer(it) }

            val sampleSuppliers = listOf(
                Supplier(name = "شركة الأغذية الشاملة", company = "مجموعة هائل سعيد أنعم", phone = "770112233", balance = 45000.0, totalPurchases = 150000.0, totalPaid = 105000.0),
                Supplier(name = "مؤسسة البركة للمواد الغذائية", company = "البركة للتجارة", phone = "772233445", balance = 0.0, totalPurchases = 80000.0, totalPaid = 80000.0)
            )
            sampleSuppliers.forEach { dao.insertSupplier(it) }
        }
    }
}
