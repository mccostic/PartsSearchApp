package com.app.partssearchapp.screens.home

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.home.compose.HomeView
import kotlinx.serialization.Serializable

/**
 * Navigation parameters for Home screen
 */
@Serializable
data class HomeParams(val userEmail: String, val userName: String, val loginType: String,)

@Composable
fun HomeRoute(backStackEntry: NavBackStackEntry) {
    BaseRoute<HomeViewModel, HomeState, HomeUIEvent, HomeNavEvent, HomeUIEffect, HomeParams>(
        backStackEntry = backStackEntry,
        router = { navEvents ->
            HomeRouter(navEvents = navEvents)
        }
    ) { state, onEvent, uiEffects ->
        HomeView(
            state = state,
            onEvent = onEvent,
            uiEffects = uiEffects
        )
    }
}
