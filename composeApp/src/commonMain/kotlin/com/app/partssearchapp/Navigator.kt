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
import com.app.partssearchapp.screens.home.HomeParams
import com.app.partssearchapp.screens.home.HomeRoute
import com.app.partssearchapp.screens.login.LoginParams
import com.app.partssearchapp.screens.login.LoginRoute
import com.app.partssearchapp.screens.profile.ProfileParams
import com.app.partssearchapp.screens.profile.ProfileRoute

@Composable
fun Navigator() {
  val navController = rememberNavController()
  CompositionLocalProvider(LocalNavController provides navController) {
    NavHost(
      navController = navController,
      startDestination = HomeParams(
        userEmail = "userEmail",
        userName = "User Name",
        loginType = "Default"
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
    }
  }
}

val LocalNavController = staticCompositionLocalOf<NavHostController> {
  error("NavController not provided")
}
