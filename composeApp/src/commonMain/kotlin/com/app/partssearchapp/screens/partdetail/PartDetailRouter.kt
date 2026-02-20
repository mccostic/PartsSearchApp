package com.app.partssearchapp.screens.partdetail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.partssearchapp.LocalNavController
import com.app.partssearchapp.screens.cart.CartParams
import kotlinx.coroutines.flow.Flow

@Composable
fun PartDetailRouter(navEvents: Flow<PartDetailNavEvent>,) {
    val navController = LocalNavController.current
    LaunchedEffect(Unit) {
        navEvents.collect {
            when (it) {
                is PartDetailNavEvent.NavigateBack -> {
                    navController.popBackStack()
                }
                is PartDetailNavEvent.NavigateToCart -> {
                    navController.navigate(CartParams())
                }
            }
        }
    }
}
