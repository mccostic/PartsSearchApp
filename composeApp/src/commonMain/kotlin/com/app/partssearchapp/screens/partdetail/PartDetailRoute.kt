package com.app.partssearchapp.screens.partdetail

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.partdetail.compose.PartDetailView
import kotlinx.serialization.Serializable

@Serializable
data class PartDetailParams(
    val partId: Int,
    val partName: String,
    val vehicleBreadcrumb: String,
    val categoryName: String,
)

@Composable
fun PartDetailRoute(backStackEntry: NavBackStackEntry) {
    BaseRoute<PartDetailViewModel, PartDetailState, PartDetailUIEvent, PartDetailNavEvent, PartDetailUIEffect, PartDetailParams>(
        backStackEntry = backStackEntry,
        router = { navEvents ->
            PartDetailRouter(navEvents = navEvents)
        }
    ) { state, onEvent, uiEffects ->
        PartDetailView(
            state = state,
            onEvent = onEvent,
            uiEffects = uiEffects,
        )
    }
}
