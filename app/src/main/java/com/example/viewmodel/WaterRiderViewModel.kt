package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.model.CustomerType
import com.example.model.DeliveryEntry
import com.example.model.ExpenseCategory
import com.example.model.PaymentMethod
import com.example.model.RouteExpense
import com.example.model.SahodSummary
import com.example.model.SukiCustomer
import com.example.model.WaterType
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class CustomerFilter {
    ALL,
    MAY_UTANG,
    NEEDS_REFILL
}

@OptIn(ExperimentalCoroutinesApi::class)
class WaterRiderViewModel(application: Application) : AndroidViewModel(application) {

    private val db: AppDatabase = Room.databaseBuilder(
        application.applicationContext,
        AppDatabase::class.java,
        "melvin_water_rider_v2.db"
    ).fallbackToDestructiveMigration().build()

    private val dao = db.dao()

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

    private val _commissionRate = MutableStateFlow(8.0)
    val commissionRate: StateFlow<Double> = _commissionRate.asStateFlow()

    private val _valeAmount = MutableStateFlow(0.0)
    val valeAmount: StateFlow<Double> = _valeAmount.asStateFlow()

    private val _unpaidBossBalance = MutableStateFlow(0.0)
    val unpaidBossBalance: StateFlow<Double> = _unpaidBossBalance.asStateFlow()

    // Inline expand states (walang pop-up modal)
    private val _isMathExpanded = MutableStateFlow(false)
    val isMathExpanded: StateFlow<Boolean> = _isMathExpanded.asStateFlow()

    private val _isBankTransfersExpanded = MutableStateFlow(false)
    val isBankTransfersExpanded: StateFlow<Boolean> = _isBankTransfersExpanded.asStateFlow()

    private val _isAddDeliveryExpanded = MutableStateFlow(false)
    val isAddDeliveryExpanded: StateFlow<Boolean> = _isAddDeliveryExpanded.asStateFlow()

    private val _isAddExpenseExpanded = MutableStateFlow(false)
    val isAddExpenseExpanded: StateFlow<Boolean> = _isAddExpenseExpanded.asStateFlow()

    private val _isAddSukiExpanded = MutableStateFlow(false)
    val isAddSukiExpanded: StateFlow<Boolean> = _isAddSukiExpanded.asStateFlow()

    // Live search at filter states (parang Chrome autocomplete)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _customerFilter = MutableStateFlow(CustomerFilter.ALL)
    val customerFilter: StateFlow<CustomerFilter> = _customerFilter.asStateFlow()

    val allCustomers: StateFlow<List<SukiCustomer>> = dao.observeCustomers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val deliveriesForDate: StateFlow<List<DeliveryEntry>> = _selectedDate
        .flatMapLatest { date -> dao.observeDeliveriesByDate(date.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expensesForDate: StateFlow<List<RouteExpense>> = _selectedDate
        .flatMapLatest { date -> dao.observeExpensesByDate(date.toString()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Instant autocomplete matches habang nagta-type kahit hindi pa buo ang pangalan
    val searchSuggestions: StateFlow<List<SukiCustomer>> = combine(allCustomers, _searchQuery) { list, q ->
        val trimmed = q.trim()
        if (trimmed.isEmpty()) emptyList()
        else list.filter {
            it.name.contains(trimmed, ignoreCase = true) ||
                it.phone.contains(trimmed, ignoreCase = true) ||
                it.address.contains(trimmed, ignoreCase = true)
        }.take(6)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredCustomers: StateFlow<List<SukiCustomer>> = combine(
        allCustomers,
        _searchQuery,
        _customerFilter,
        _selectedDate
    ) { list, q, filter, currentDate ->
        val trimmed = q.trim()
        list.filter { suki ->
            val matchesQuery = trimmed.isEmpty() ||
                suki.name.contains(trimmed, ignoreCase = true) ||
                suki.phone.contains(trimmed, ignoreCase = true) ||
                suki.address.contains(trimmed, ignoreCase = true)

            val matchesFilter = when (filter) {
                CustomerFilter.ALL -> true
                CustomerFilter.MAY_UTANG -> suki.utangBalance > 0.0
                CustomerFilter.NEEDS_REFILL -> {
                    val last = suki.lastRefillDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
                    last == null || ChronoUnit.DAYS.between(last, currentDate) >= 3L
                }
            }
            matchesQuery && matchesFilter
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun previousDay() {
        _selectedDate.value = _selectedDate.value.minusDays(1)
    }

    fun nextDay() {
        _selectedDate.value = _selectedDate.value.plusDays(1)
    }

    fun setToday() {
        _selectedDate.value = LocalDate.now()
    }

    fun formatHeaderDate(date: LocalDate): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)
        return if (date == today) "Today (${date.format(formatter)})" else date.format(formatter)
    }

    fun toggleMathExpanded() {
        _isMathExpanded.value = !_isMathExpanded.value
    }

    fun toggleBankTransfersExpanded() {
        _isBankTransfersExpanded.value = !_isBankTransfersExpanded.value
    }

    fun setAddDeliveryExpanded(expanded: Boolean) {
        _isAddDeliveryExpanded.value = expanded
    }

    fun setAddExpenseExpanded(expanded: Boolean) {
        _isAddExpenseExpanded.value = expanded
    }

    fun setAddSukiExpanded(expanded: Boolean) {
        _isAddSukiExpanded.value = expanded
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearchQuery() {
        _searchQuery.value = ""
    }

    fun setCustomerFilter(filter: CustomerFilter) {
        _customerFilter.value = filter
    }

    fun saveCustomer(
        existing: SukiCustomer? = null,
        name: String,
        phone: String,
        address: String,
        customerType: CustomerType,
        purifiedPrice: Double,
        alkalinePrice: Double
    ) {
        val cleanName = name.trim()
        if (cleanName.isEmpty()) return
        viewModelScope.launch {
            val item = (existing ?: SukiCustomer(name = cleanName)).copy(
                name = cleanName,
                phone = phone.trim(),
                address = address.trim(),
                customerType = customerType,
                purifiedPrice = purifiedPrice,
                alkalinePrice = alkalinePrice
            )
            dao.upsertCustomer(item)
            _isAddSukiExpanded.value = false
        }
    }

    fun deleteCustomer(customer: SukiCustomer) {
        viewModelScope.launch {
            dao.deleteCustomer(customer)
        }
    }

    fun logDelivery(
        customer: SukiCustomer,
        waterType: WaterType,
        gallons: Int,
        paymentMethod: PaymentMethod,
        notes: String = ""
    ) {
        if (gallons <= 0) return
        viewModelScope.launch {
            val unitPrice = if (waterType == WaterType.PURIFIED) customer.purifiedPrice else customer.alkalinePrice
            val totalAmount = unitPrice * gallons
            val dateStr = _selectedDate.value.toString()

            // May commission sa Cash at Bank Transfer orders
            val commission = if (paymentMethod == PaymentMethod.UTANG) 0.0 else gallons * _commissionRate.value
            val updatedUtang = if (paymentMethod == PaymentMethod.UTANG) {
                customer.utangBalance + totalAmount
            } else {
                customer.utangBalance
            }

            val updatedCustomer = customer.copy(
                utangBalance = updatedUtang,
                lastRefillDate = dateStr
            )

            val entry = DeliveryEntry(
                date = dateStr,
                customerId = customer.id,
                customerName = customer.name,
                customerType = customer.customerType,
                waterType = waterType,
                gallons = gallons,
                amount = totalAmount,
                paymentMethod = paymentMethod,
                commissionEarned = commission,
                notes = notes.trim()
            )

            db.recordDeliveryTransaction(updatedCustomer, entry)
            _isAddDeliveryExpanded.value = false
        }
    }

    fun addExpense(category: ExpenseCategory, amount: Double, notes: String = "") {
        if (amount <= 0.0) return
        viewModelScope.launch {
            dao.insertExpense(
                RouteExpense(
                    date = _selectedDate.value.toString(),
                    category = category,
                    amount = amount,
                    notes = notes.trim()
                )
            )
            _isAddExpenseExpanded.value = false
        }
    }

    fun deleteExpense(expense: RouteExpense) {
        viewModelScope.launch {
            dao.deleteExpense(expense)
        }
    }

    fun updateCommissionRate(rate: Double) {
        if (rate >= 0.0) _commissionRate.value = rate
    }

    fun addVale(amount: Double) {
        if (amount > 0.0) _valeAmount.value += amount
    }

    fun buildSahodSummary(deliveries: List<DeliveryEntry>): SahodSummary {
        val paidDeliveries = deliveries.filter { it.paymentMethod != PaymentMethod.UTANG }
        val totalPaidGallons = paidDeliveries.sumOf { it.gallons }
        val gross = paidDeliveries.sumOf { it.commissionEarned }
        val vale = _valeAmount.value
        return SahodSummary(
            weekLabel = formatHeaderDate(_selectedDate.value),
            totalGallonsPaid = totalPaidGallons,
            commissionRate = _commissionRate.value,
            grossCommission = gross,
            valeDeduction = vale,
            netSahod = gross - vale,
            unpaidBossBalance = _unpaidBossBalance.value
        )
    }
}
