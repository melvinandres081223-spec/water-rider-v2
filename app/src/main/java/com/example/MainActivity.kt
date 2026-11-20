package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.CustomersScreen
import com.example.ui.ExpensesScreen
import com.example.ui.SahodScreen
import com.example.ui.TodayScreen
import com.example.viewmodel.WaterRiderViewModel

private enum class RiderTab(
    val label: String,
    val icon: ImageVector
) {
    TODAY("Today", Icons.Default.Home),
    CUSTOMERS("Customers", Icons.Default.People),
    EXPENSES("Expenses", Icons.Default.ReceiptLong),
    SAHOD("Sahod", Icons.Default.AccountBalanceWallet)
}

class MainActivity : ComponentActivity() {
    private val viewModel: WaterRiderViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WaterRiderAppRoot(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaterRiderAppRoot(viewModel: WaterRiderViewModel) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
    val selectedDate by viewModel.selectedDate.collectAsState()
    val tabs = RiderTab.entries

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Melvin WaterRider",
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.previousDay() }) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Previous day")
                        }
                        Text(
                            text = viewModel.formatHeaderDate(selectedDate),
                            style = MaterialTheme.typography.labelLarge
                        )
                        IconButton(onClick = { viewModel.nextDay() }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next day")
                        }
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        val screenModifier = Modifier.padding(innerPadding)
        when (tabs[selectedTabIndex]) {
            RiderTab.TODAY -> TodayScreen(viewModel = viewModel, modifier = screenModifier)
            RiderTab.CUSTOMERS -> CustomersScreen(viewModel = viewModel, modifier = screenModifier)
            RiderTab.EXPENSES -> ExpensesScreen(viewModel = viewModel, modifier = screenModifier)
            RiderTab.SAHOD -> SahodScreen(viewModel = viewModel, modifier = screenModifier)
        }
    }
}
