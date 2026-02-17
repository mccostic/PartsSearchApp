package com.app.partssearchapp.screens.cart

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.cart.compose.CartView
import kotlinx.serialization.Serializable

@Serializable
data class CartParams(
  val fromScreen: String = "parts",
)

@Composable
fun CartRoute(backStackEntry: NavBackStackEntry) {
  BaseRoute<CartViewModel, CartState, CartUIEvent, CartNavEvent, CartUIEffect, CartParams>(
    backStackEntry = backStackEntry,
    router = { navEvents ->
      CartRouter(navEvents = navEvents)
    }
  ) { state, onEvent, uiEffects ->
    CartView(
      state = state,
      onEvent = onEvent,
      uiEffects = uiEffects,
    )
  }
}
