package com.example.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class CustomerType {
    OUTLET,
    RESIDENTIAL
}

enum class WaterType {
    PURIFIED,
    ALKALINE
}

enum class PaymentMethod {
    CASH,
    BANK_TRANSFER,
    UTANG
}

enum class ExpenseCategory {
    FOOD,
    GAS,
    VULCANIZING,
    MOTOR_REPAIR
}

@Entity(tableName = "suki_customers")
data class SukiCustomer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val name: String,
    val phone: String = "",
    val address: String = "",
    val customerType: CustomerType = CustomerType.RESIDENTIAL,
    val purifiedPrice: Double = 25.0,
    val alkalinePrice: Double = 35.0,
    val utangBalance: Double = 0.0,
    val lastRefillDate: String? = null
)

@Entity(tableName = "deliveries")
data class DeliveryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String,
    val customerId: Long,
    val customerName: String,
    val customerType: CustomerType,
    val waterType: WaterType,
    val gallons: Int,
    val amount: Double,
    val paymentMethod: PaymentMethod,
    val commissionEarned: Double,
    val notes: String = ""
)

@Entity(tableName = "route_expenses")
data class RouteExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val date: String,
    val category: ExpenseCategory,
    val amount: Double,
    val notes: String = ""
)

data class SahodSummary(
    val weekLabel: String,
    val totalGallonsPaid: Int,
    val commissionRate: Double = 8.0,
    val grossCommission: Double,
    val valeDeduction: Double,
    val netSahod: Double,
    val unpaidBossBalance: Double
)
