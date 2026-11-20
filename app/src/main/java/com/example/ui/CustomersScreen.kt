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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Surface
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
import com.example.model.CustomerType
import com.example.model.SukiCustomer
import com.example.viewmodel.CustomerFilter
import com.example.viewmodel.WaterRiderViewModel

@Composable
fun CustomersScreen(
    viewModel: WaterRiderViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val suggestions by viewModel.searchSuggestions.collectAsState()
    val filteredCustomers by viewModel.filteredCustomers.collectAsState()
    val selectedFilter by viewModel.customerFilter.collectAsState()
    val isAddExpanded by viewModel.isAddSukiExpanded.collectAsState()

    var editingCustomer by remember { mutableStateOf<SukiCustomer?>(null) }
    var prefillName by remember { mutableStateOf("") }
    var showSuggestionsDropdown by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Header + Right-Aligned "+ Add Suki" Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Suki Directory & Utang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${filteredCustomers.size} suki listed",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = {
                        editingCustomer = null
                        prefillName = ""
                        viewModel.setAddSukiExpanded(!isAddExpanded)
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Text("Add Suki", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }

        // Live Search Box + Chrome-Style Inline Autocomplete Dropdown
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = {
                        viewModel.updateSearchQuery(it)
                        showSuggestionsDropdown = it.isNotBlank()
                    },
                    placeholder = { Text("Search suki name, phone, or address...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.clearSearchQuery()
                                    showSuggestionsDropdown = false
                                }
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Live Dropdown Suggestions habang nagta-type kahit hindi pa buo ang pangalan
                AnimatedVisibility(visible = showSuggestionsDropdown && searchQuery.isNotBlank()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            suggestions.forEach { match ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.updateSearchQuery(match.name)
                                            showSuggestionsDropdown = false
                                        }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(match.name, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${if (match.customerType == CustomerType.OUTLET) "Outlet" else "Residential"} · Pur: ${formatPeso(match.purifiedPrice)}",
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                    if (match.utangBalance > 0.0) {
                                        Text(
                                            "Utang: ${formatPeso(match.utangBalance)}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                                HorizontalDivider()
                            }

                            // Instant "+ Create new suki" option mula sa tinype na pangalan
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        editingCustomer = null
                                        prefillName = searchQuery.trim()
                                        showSuggestionsDropdown = false
                                        viewModel.setAddSukiExpanded(true)
                                    }
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Create new suki \"${searchQuery.trim()}\"",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(start = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Filter Chips Row
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.ALL,
                        onClick = { viewModel.setCustomerFilter(CustomerFilter.ALL) },
                        label = { Text("All Suki") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.MAY_UTANG,
                        onClick = { viewModel.setCustomerFilter(CustomerFilter.MAY_UTANG) },
                        label = { Text("May Utang") }
                    )
                }
                item {
                    FilterChip(
                        selected = selectedFilter == CustomerFilter.NEEDS_REFILL,
                        onClick = { viewModel.setCustomerFilter(CustomerFilter.NEEDS_REFILL) },
                        label = { Text("Needs Refill (3+ Days)") }
                    )
                }
            }
        }

        // Inline Add / Edit Suki Card (walang pop-up dialog)
        item {
            AnimatedVisibility(visible = isAddExpanded) {
                InlineSukiFormCard(
                    existing = editingCustomer,
                    initialName = prefillName,
                    onSave = { name, phone, address, type, purPrice, alkPrice ->
                        viewModel.saveCustomer(
                            existing = editingCustomer,
                            name = name,
                            phone = phone,
                            address = address,
                            customerType = type,
                            purifiedPrice = purPrice,
                            alkalinePrice = alkPrice
                        )
                        editingCustomer = null
                        prefillName = ""
                    },
                    onCancel = {
                        editingCustomer = null
                        prefillName = ""
                        viewModel.setAddSukiExpanded(false)
                    }
                )
            }
        }

        // Suki Customer List
        if (filteredCustomers.isEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Walang nakitang suki sa filter o search na ito.")
                    }
                }
            }
        } else {
            items(filteredCustomers, key = { it.id }) { suki ->
                SukiCustomerCard(
                    customer = suki,
                    onEdit = {
                        editingCustomer = suki
                        prefillName = ""
                        viewModel.setAddSukiExpanded(true)
                    },
                    onDelete = { viewModel.deleteCustomer(suki) }
                )
            }
        }
    }
}

@Composable
private fun SukiCustomerCard(
    customer: SukiCustomer,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        customer.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = if (customer.customerType == CustomerType.OUTLET) "Outlet" else "Residential",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Right-thumb Edit & Delete Icons
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit suki")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete suki")
                    }
                }
            }

            if (customer.address.isNotBlank() || customer.phone.isNotBlank()) {
                Text(
                    listOf(customer.address, customer.phone).filter { it.isNotBlank() }.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Purified: ${formatPeso(customer.purifiedPrice)} · Alkaline: ${formatPeso(customer.alkalinePrice)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = if (customer.utangBalance > 0.0) "Utang: ${formatPeso(customer.utangBalance)}" else "Clean / Paid",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (customer.utangBalance > 0.0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun InlineSukiFormCard(
    existing: SukiCustomer?,
    initialName: String,
    onSave: (String, String, String, CustomerType, Double, Double) -> Unit,
    onCancel: () -> Unit
) {
    var name by remember(existing, initialName) { mutableStateOf(existing?.name ?: initialName) }
    var phone by remember(existing) { mutableStateOf(existing?.phone ?: "") }
    var address by remember(existing) { mutableStateOf(existing?.address ?: "") }
    var customerType by remember(existing) { mutableStateOf(existing?.customerType ?: CustomerType.RESIDENTIAL) }
    var purifiedText by remember(existing) { mutableStateOf((existing?.purifiedPrice ?: 25.0).toInt().toString()) }
    var alkalineText by remember(existing) { mutableStateOf((existing?.alkalinePrice ?: 35.0).toInt().toString()) }

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
            Text(
                if (existing == null) "New Suki Customer" else "Edit Suki Customer",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Suki / Canteen Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = customerType == CustomerType.RESIDENTIAL,
                    onClick = { customerType = CustomerType.RESIDENTIAL },
                    label = { Text("Residential") }
                )
                FilterChip(
                    selected = customerType == CustomerType.OUTLET,
                    onClick = { customerType = CustomerType.OUTLET },
                    label = { Text("Outlet (Canteen)") }
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = purifiedText,
                    onValueChange = { purifiedText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Purified Price (₱)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = alkalineText,
                    onValueChange = { alkalineText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Alkaline Price (₱)") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
            }

            OutlinedTextField(
                value = address,
                onValueChange = { address = it },
                label = { Text("Address / Landmark (optional)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number (optional)") },
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
                        val pur = purifiedText.toDoubleOrNull() ?: 25.0
                        val alk = alkalineText.toDoubleOrNull() ?: 35.0
                        if (name.isNotBlank()) {
                            onSave(name, phone, address, customerType, pur, alk)
                        }
                    }
                ) {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Text("Save Suki", modifier = Modifier.padding(start = 4.dp))
                }
            }
        }
    }
}
