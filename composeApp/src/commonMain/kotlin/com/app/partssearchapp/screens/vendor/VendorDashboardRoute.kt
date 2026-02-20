package com.app.partssearchapp.screens.vendor

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.vendor.compose.VendorDashboardView
import kotlinx.serialization.Serializable

@Serializable
data class VendorDashboardParams(val vendorId: Int, val vendorName: String = "",)

@Composable
fun VendorDashboardRoute(backStackEntry: NavBackStackEntry) {
    BaseRoute<VendorDashboardViewModel, VendorDashboardState, VendorDashboardUIEvent, VendorDashboardNavEvent, VendorDashboardUIEffect, VendorDashboardParams>(
        backStackEntry = backStackEntry,
        router = { navEvents ->
            VendorDashboardRouter(navEvents = navEvents)
        }
    ) { state, onEvent, uiEffects ->
        VendorDashboardView(
            state = state,
            onEvent = onEvent,
            uiEffects = uiEffects,
        )
    }
}
