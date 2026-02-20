package com.app.partssearchapp.screens.partslisting

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.partslisting.compose.PartsListingView
import kotlinx.serialization.Serializable

@Serializable
data class PartsListingParams(
    val categoryId: Int,
    val categoryName: String,
    val engineId: Int,
    val vehicleBreadcrumb: String,
)

@Composable
fun PartsListingRoute(backStackEntry: NavBackStackEntry) {
    BaseRoute<PartsListingViewModel, PartsListingState, PartsListingUIEvent, PartsListingNavEvent, PartsListingUIEffect, PartsListingParams>(
        backStackEntry = backStackEntry,
        router = { navEvents ->
            PartsListingRouter(navEvents = navEvents)
        }
    ) { state, onEvent, uiEffects ->
        PartsListingView(
            state = state,
            onEvent = onEvent,
            uiEffects = uiEffects,
        )
    }
}
