package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.DeliveryEntry
import com.example.model.PaymentMethod
import com.example.model.RouteExpense
import com.example.model.SukiCustomer
import com.example.model.WaterType
import com.example.viewmodel.WaterRiderViewModel
import java.util.Locale

@Composable
fun TodayScreen(
    viewModel: WaterRiderViewModel,
    modifier: Modifier = Modifier
) {
    val customers by viewModel.allCustomers.collectAsState()
    val deliveries by viewModel.deliveriesForDate.collectAsState()
    val expenses by viewModel.expensesForDate.collectAsState()
    val isMathExpanded by viewModel.isMathExpanded.collectAsState()
    val isBankExpanded by viewModel.isBankTransfersExpanded.collectAsState()
    val isAddExpanded by viewModel.isAddDeliveryExpanded.collectAsState()

    val cashCollected = deliveries
        .filter { it.paymentMethod == PaymentMethod.CASH }
        .sumOf { it.amount }
    val bankTransfersToBoss = deliveries
        .filter { it.paymentMethod == PaymentMethod.BANK_TRANSFER }
        .sumOf { it.amount }
    val totalExpenses = expenses.sumOf { it.amount }
    val netBossRemittance = cashCollected - totalExpenses - bankTransfersToBoss

    val totalGallons = deliveries.sumOf { it.gallons }
    val purifiedGallons = deliveries.filter { it.waterType == WaterType.PURIFIED }.sumOf { it.gallons }
    val alkalineGallons = deliveries.filter { it.waterType == WaterType.ALKALINE }.sumOf { it.gallons }
    val totalCommission = deliveries
        .filter { it.paymentMethod != PaymentMethod.UTANG }
        .sumOf { it.commissionEarned }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.setAddDeliveryExpanded(!isAddExpanded) }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Log Order")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Net Boss Remittance Card with Inline Expand ("Tap for math")
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.toggleMathExpanded() }
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NET BOSS REMITTANCE",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = if (isMathExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                contentDescription = "Expand math"
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatPeso(netBossRemittance),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isMathExpanded) "Tap to hide math" else "Tap for math",
                            style = MaterialTheme.typography.bodySmall
                        )

                        AnimatedVisibility(visible = isMathExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                HorizontalDivider()
                                MathRow("Cash Collected", formatPeso(cashCollected))
                                MathRow("Less: Route Expenses", "-${formatPeso(totalExpenses)}")
                                MathRow("Less: Bank Transfers", "-${formatPeso(bankTransfersToBoss)}")
                                HorizontalDivider()
                                MathRow("Net to Remit", formatPeso(netBossRemittance), bold = true)
                            }
                        }
                    }
                }
            }

            // Delivered & Commission Summary Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("DELIVERED", style = MaterialTheme.typography.labelMedium)
                            Text(
                                "$totalGallons gals",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "$purifiedGallons Pur · $alkalineGallons Alk",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    Card(modifier = Modifier.weight(1f)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("COMMISSION", style = MaterialTheme.typography.labelMedium)
                            Text(
                                formatPeso(totalCommission),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text("Cash + Bank Transfer", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            // Inline Expandable Bank Transfers to Boss Section
            item {
                val bankEntries = deliveries.filter { it.paymentMethod == PaymentMethod.BANK_TRANSFER }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.toggleBankTransfersExpanded() }
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Bank Transfers to Boss (${bankEntries.size})",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    if (isBankExpanded) "Tap to collapse" else "Tap to expand",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    formatPeso(bankTransfersToBoss),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Icon(
                                    imageVector = if (isBankExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Expand bank transfers"
                                )
                            }
                        }

                        AnimatedVisibility(visible = isBankExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                HorizontalDivider()
                                if (bankEntries.isEmpty()) {
                                    Text("Walang bank transfer entry sa napiling petsa.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    bankEntries.forEach { item ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("${item.customerName} (${item.gallons} gals)", style = MaterialTheme.typography.bodyMedium)
                                            Text(formatPeso(item.amount), fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Inline Quick Log Order Form (bumubuka pababa sa loob ng screen)
            item {
                AnimatedVisibility(visible = isAddExpanded) {
                    InlineLogDeliveryCard(
                        customers = customers,
                        onSave = { suki, water, gals, payment, note ->
                            viewModel.logDelivery(suki, water, gals, payment, note)
                        },
                        onCancel = { viewModel.setAddDeliveryExpanded(false) }
                    )
                }
            }

            // Deliveries List or Empty State
            if (deliveries.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text("Wala pang logged delivery sa petsang ito.", style = MaterialTheme.typography.bodyMedium)
                            Button(onClick = { viewModel.setAddDeliveryExpanded(true) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Text("Log First Order", modifier = Modifier.padding(start = 6.dp))
                            }
                        }
                    }
                }
            } else {
                items(deliveries, key = { it.id }) { entry ->
                    DeliveryEntryCard(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun MathRow(label: String, value: String, bold: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium
        )
    }
}

@Composable
private fun DeliveryEntryCard(entry: DeliveryEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.customerName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    "${entry.gallons} gal · ${entry.waterType.name} · ${entry.paymentMethod.name}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                formatPeso(entry.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun InlineLogDeliveryCard(
    customers: List<SukiCustomer>,
    onSave: (SukiCustomer, WaterType, Int, PaymentMethod, String) -> Unit,
    onCancel: () -> Unit
) {
    var sukiSearch by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<SukiCustomer?>(null) }
    var waterType by remember { mutableStateOf(WaterType.PURIFIED) }
    var gallonsText by remember { mutableStateOf("1") }
    var paymentMethod by remember { mutableStateOf(PaymentMethod.CASH) }
    var notes by remember { mutableStateOf("") }

    val matchingCustomers = remember(customers, sukiSearch) {
        val q = sukiSearch.trim()
        if (q.isEmpty()) customers.take(4)
        else customers.filter { it.name.contains(q, ignoreCase = true) }.take(5)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Log Delivery Order", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            OutlinedTextField(
                value = sukiSearch,
                onValueChange = {
                    sukiSearch = it
                    selectedCustomer = null
                },
                label = { Text("Type Suki Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (selectedCustomer == null && matchingCustomers.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    matchingCustomers.forEach { suki ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCustomer = suki
                                    sukiSearch = suki.name
                                }
                                .padding(vertical = 6.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(suki.name, fontWeight = FontWeight.Medium)
                            Text(suki.customerType.name, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WaterType.entries.forEach { type ->
                    FilterChip(
                        selected = waterType == type,
                        onClick = { waterType = type },
                        label = { Text(type.name) }
                    )
                }
            }

            OutlinedTextField(
                value = gallonsText,
                onValueChange = { gallonsText = it.filter { ch -> ch.isDigit() } },
                label = { Text("Gallons") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { method ->
                    FilterChip(
                        selected = paymentMethod == method,
                        onClick = { paymentMethod = method },
                        label = { Text(method.name) }
                    )
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(onClick = onCancel) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                Button(
                    onClick = {
                        val target = selectedCustomer ?: customers.firstOrNull {
                            it.name.equals(sukiSearch.trim(), ignoreCase = true)
                        }
                        val gals = gallonsText.toIntOrNull() ?: 0
                        if (target != null && gals > 0) {
                            onSave(target, waterType, gals, paymentMethod, notes)
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text("Save Order", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

fun formatPeso(amount: Double): String =
    String.format(Locale.US, "₱%,.2f", amount)
