package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.withTransaction
import com.example.model.CustomerType
import com.example.model.DeliveryEntry
import com.example.model.ExpenseCategory
import com.example.model.PaymentMethod
import com.example.model.RouteExpense
import com.example.model.SukiCustomer
import com.example.model.WaterType
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter fun fromCustomerType(v: CustomerType): String = v.name
    @TypeConverter fun toCustomerType(v: String): CustomerType = CustomerType.valueOf(v)

    @TypeConverter fun fromWaterType(v: WaterType): String = v.name
    @TypeConverter fun toWaterType(v: String): WaterType = WaterType.valueOf(v)

    @TypeConverter fun fromPaymentMethod(v: PaymentMethod): String = v.name
    @TypeConverter fun toPaymentMethod(v: String): PaymentMethod = PaymentMethod.valueOf(v)

    @TypeConverter fun fromExpenseCategory(v: ExpenseCategory): String = v.name
    @TypeConverter fun toExpenseCategory(v: String): ExpenseCategory = ExpenseCategory.valueOf(v)
}

@Dao
interface WaterRiderDao {
    @Query("SELECT * FROM suki_customers ORDER BY name ASC")
    fun observeCustomers(): Flow<List<SukiCustomer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCustomer(customer: SukiCustomer): Long

    @Delete
    suspend fun deleteCustomer(customer: SukiCustomer)

    @Query("SELECT * FROM deliveries WHERE date = :date ORDER BY id DESC")
    fun observeDeliveriesByDate(date: String): Flow<List<DeliveryEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDelivery(entry: DeliveryEntry): Long

    @Query("SELECT * FROM route_expenses WHERE date = :date ORDER BY id DESC")
    fun observeExpensesByDate(date: String): Flow<List<RouteExpense>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: RouteExpense): Long

    @Delete
    suspend fun deleteExpense(expense: RouteExpense)
}

@Database(
    entities = [SukiCustomer::class, DeliveryEntry::class, RouteExpense::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): WaterRiderDao

    suspend fun recordDeliveryTransaction(
        updatedCustomer: SukiCustomer,
        entry: DeliveryEntry
    ) {
        withTransaction {
            dao().upsertCustomer(updatedCustomer)
            dao().insertDelivery(entry)
        }
    }
}
