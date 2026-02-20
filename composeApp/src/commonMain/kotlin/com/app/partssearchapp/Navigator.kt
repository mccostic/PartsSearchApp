package com.app.partssearchapp

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.app.partssearchapp.screens.cart.CartParams
import com.app.partssearchapp.screens.cart.CartRoute
import com.app.partssearchapp.screens.categories.CategoriesParams
import com.app.partssearchapp.screens.categories.CategoriesRoute
import com.app.partssearchapp.screens.home.HomeParams
import com.app.partssearchapp.screens.home.HomeRoute
import com.app.partssearchapp.screens.login.LoginParams
import com.app.partssearchapp.screens.login.LoginRoute
import com.app.partssearchapp.screens.partdetail.PartDetailParams
import com.app.partssearchapp.screens.partdetail.PartDetailRoute
import com.app.partssearchapp.screens.partslisting.PartsListingParams
import com.app.partssearchapp.screens.partslisting.PartsListingRoute
import com.app.partssearchapp.screens.profile.ProfileParams
import com.app.partssearchapp.screens.profile.ProfileRoute
import com.app.partssearchapp.screens.vehicleselection.VehicleSelectionParams
import com.app.partssearchapp.screens.vehicleselection.VehicleSelectionRoute
import com.app.partssearchapp.screens.vendor.VendorDashboardParams
import com.app.partssearchapp.screens.vendor.VendorDashboardRoute

@Composable
fun Navigator() {
    val navController = rememberNavController()
    CompositionLocalProvider(LocalNavController provides navController) {
        NavHost(
            navController = navController,
            startDestination = HomeParams(
                userEmail = "",
                userName = "",
                loginType = ""
            )
        ) {
            composable<LoginParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                LoginRoute(backStackEntry)
            }

            composable<HomeParams>(
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) }
            ) { backStackEntry ->
                HomeRoute(backStackEntry)
            }

            composable<ProfileParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                ProfileRoute(backStackEntry)
            }

            composable<VehicleSelectionParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                VehicleSelectionRoute(backStackEntry)
            }

            composable<CategoriesParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                CategoriesRoute(backStackEntry)
            }

            composable<PartsListingParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                PartsListingRoute(backStackEntry)
            }

            composable<PartDetailParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { -it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                PartDetailRoute(backStackEntry)
            }

            composable<CartParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                CartRoute(backStackEntry)
            }

            composable<VendorDashboardParams>(
                enterTransition = { slideInHorizontally { it } },
                exitTransition = { slideOutHorizontally { it } },
                popEnterTransition = { slideInHorizontally { -it } },
                popExitTransition = { slideOutHorizontally { it } }
            ) { backStackEntry ->
                VendorDashboardRoute(backStackEntry)
            }
        }
    }
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
    error("NavController not provided")
}
