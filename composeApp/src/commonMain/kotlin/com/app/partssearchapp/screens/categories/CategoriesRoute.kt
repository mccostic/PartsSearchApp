package com.app.partssearchapp.screens.categories

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.categories.compose.CategoriesView
import kotlinx.serialization.Serializable

@Serializable
data class CategoriesParams(val engineId: Int, val vehicleBreadcrumb: String,)

@Composable
fun CategoriesRoute(backStackEntry: NavBackStackEntry) {
    BaseRoute<CategoriesViewModel, CategoriesState, CategoriesUIEvent, CategoriesNavEvent, CategoriesUIEffect, CategoriesParams>(
        backStackEntry = backStackEntry,
        router = { navEvents ->
            CategoriesRouter(navEvents = navEvents)
        }
    ) { state, onEvent, uiEffects ->
        CategoriesView(
            state = state,
            onEvent = onEvent,
            uiEffects = uiEffects,
        )
    }
}
