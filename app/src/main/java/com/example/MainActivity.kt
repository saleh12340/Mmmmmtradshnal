package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.screens.CreateInvoiceScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.ExpensesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProductsScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesInvoicesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SuppliersScreen
import com.example.ui.theme.AlezziGroceryTheme
import com.example.ui.viewmodel.GroceryViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlezziGroceryTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        AlezziGroceryApp()
                    }
                }
            }
        }
    }
}

@Composable
fun AlezziGroceryApp(
    viewModel: GroceryViewModel = viewModel()
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigate = { route -> navController.navigate(route) }
            )
        }

        composable("create_sale") {
            CreateInvoiceScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("sales") {
            SalesInvoicesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNewInvoice = { navController.navigate("create_sale") }
            )
        }

        composable("products") {
            ProductsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("customers") {
            CustomersScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("purchases") {
            PurchasesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("suppliers") {
            SuppliersScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("expenses") {
            ExpensesScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("reports") {
            ReportsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable("settings") {
            SettingsScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
