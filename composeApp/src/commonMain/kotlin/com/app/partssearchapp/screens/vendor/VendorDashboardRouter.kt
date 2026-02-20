package com.app.partssearchapp.screens.vendor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.partssearchapp.LocalNavController
import kotlinx.coroutines.flow.Flow

@Composable
fun VendorDashboardRouter(navEvents: Flow<VendorDashboardNavEvent>,) {
    val navController = LocalNavController.current
    LaunchedEffect(Unit) {
        navEvents.collect {
            when (it) {
                is VendorDashboardNavEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }
}
