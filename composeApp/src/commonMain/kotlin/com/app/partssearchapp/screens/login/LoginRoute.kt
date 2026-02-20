package com.app.partssearchapp.screens.login

import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import com.app.partssearchapp.arch.BaseRoute
import com.app.partssearchapp.screens.login.compose.LoginView
import kotlinx.serialization.Serializable

@Serializable
data class LoginParams(val fromScreen: String = "home", val prefillEmail: String = "",)

@Composable
fun LoginRoute(backStackEntry: NavBackStackEntry) {
    BaseRoute<LoginViewModel, LoginState, LoginUIEvent, LoginNavEvent, LoginUIEffect, LoginParams>(
        backStackEntry = backStackEntry,
        router = { navEvents ->
            LoginRouter(navEvents = navEvents)
        }
    ) { state, onEvent, uiEffects ->
        LoginView(
            state = state,
            onEvent = onEvent,
            uiEffects = uiEffects
        )
    }
}
