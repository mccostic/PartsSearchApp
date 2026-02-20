package com.app.partssearchapp.screens.categories

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.app.partssearchapp.LocalNavController
import com.app.partssearchapp.screens.partslisting.PartsListingParams
import com.app.partssearchapp.screens.cart.CartParams
import kotlinx.coroutines.flow.Flow

@Composable
fun CategoriesRouter(
  navEvents: Flow<CategoriesNavEvent>,
) {
  val navController = LocalNavController.current
  LaunchedEffect(Unit) {
    navEvents.collect {
      when (it) {
        is CategoriesNavEvent.NavigateToPartsListing -> {
          navController.navigate(
            PartsListingParams(
              categoryId = it.categoryId,
              categoryName = it.categoryName,
              engineId = it.engineId,
              vehicleBreadcrumb = it.vehicleBreadcrumb,
            )
          )
        }
        is CategoriesNavEvent.NavigateBack -> {
          navController.popBackStack()
        }
        is CategoriesNavEvent.NavigateToCart -> {
          navController.navigate(CartParams())
        }
      }
    }
  }
}
