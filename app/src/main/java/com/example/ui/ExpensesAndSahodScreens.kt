package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.model.ExpenseCategory
import com.example.model.RouteExpense
import com.example.viewmodel.WaterRiderViewModel

@Composable
fun ExpensesScreen(
    viewModel: WaterRiderViewModel,
    modifier: Modifier = Modifier
) {
    val expenses by viewModel.expensesForDate.collectAsState()
    val isAddExpanded by viewModel.isAddExpenseExpanded.collectAsState()
    val totalExpenses = expenses.sumOf { it.amount }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "TOTAL ROUTE EXPENSES",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            formatPeso(totalExpenses),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Deducted from Daily Remit",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    Button(
                        onClick = { viewModel.setAddExpenseExpanded(!isAddExpanded) }
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("Add Expense", modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }

        // Inline Add Expense Form (bumubuka pababa sa loob ng screen)
        item {
            AnimatedVisibility(visible = isAddExpanded) {
                InlineAddExpenseCard(
                    onSave = { category, amount, notes ->
                        viewModel.addExpense(category, amount, notes)
                    },
                    onCancel = { viewModel.setAddExpenseExpanded(false) }
                )
            }
        }

        if (expenses.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Wala pang route expense sa napiling petsa.")
                    }
                }
            }
        } else {
            items(expenses, key = { it.id }) { expense ->
                RouteExpenseCard(
                    expense = expense,
                    onDelete = { viewModel.deleteExpense(expense) }
                )
            }
        }
    }
}

@Composable
private fun RouteExpenseCard(
    expense: RouteExpense,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.category.name.replace('_', ' '),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (expense.notes.isNotBlank()) {
                    Text(expense.notes, style = MaterialTheme.typography.bodySmall)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    formatPeso(expense.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete expense")
                }
            }
        }
    }
}

@Composable
private fun InlineAddExpenseCard(
    onSave: (ExpenseCategory, Double, String) -> Unit,
    onCancel: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.GAS) }
    var amountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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
            Text("Add Route Expense", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ExpenseCategory.entries) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        label = { Text(category.name.replace('_', ' ')) }
                    )
                }
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Amount (₱)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Details / Notes (optional)") },
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
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        if (amt > 0.0) {
                            onSave(selectedCategory, amt, notes)
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text("Save Expense", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}

@Composable
fun SahodScreen(
    viewModel: WaterRiderViewModel,
    modifier: Modifier = Modifier
) {
    val deliveries by viewModel.deliveriesForDate.collectAsState()
    val commissionRate by viewModel.commissionRate.collectAsState()
    val summary = viewModel.buildSahodSummary(deliveries)

    var rateText by remember(commissionRate) { mutableStateOf(commissionRate.toString()) }
    var valeAddText by remember { mutableStateOf("") }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "NET SAHOD SUMMARY",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        formatPeso(summary.netSahod),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Paid Gallons (Cash + Bank): ${summary.totalGallonsPaid} gals @ ${formatPeso(summary.commissionRate)}/gal",
                        style = MaterialTheme.typography.bodySmall
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    MathRow("Gross Commission", formatPeso(summary.grossCommission))
                    MathRow("Less: Vale / Cash Advance", "-${formatPeso(summary.valeDeduction)}")
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        "Commission Rate & Vale Adjustment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = rateText,
                            onValueChange = { rateText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Rate / Gal (₱)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val parsed = rateText.toDoubleOrNull()
                                if (parsed != null && parsed >= 0.0) {
                                    viewModel.updateCommissionRate(parsed)
                                }
                            }
                        ) {
                            Text("Set Rate")
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = valeAddText,
                            onValueChange = { valeAddText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Add Vale Amount (₱)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val valeAmt = valeAddText.toDoubleOrNull() ?: 0.0
                                if (valeAmt > 0.0) {
                                    viewModel.addVale(valeAmt)
                                    valeAddText = ""
                                }
                            }
                        ) {
                            Text("Add Vale")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MathRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
