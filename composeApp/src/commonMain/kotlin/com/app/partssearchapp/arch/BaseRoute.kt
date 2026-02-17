package com.app.partssearchapp.arch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.Flow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
inline fun <
    reified VM : BaseViewModel<UIState, UIEvent, NavEvent, UIEffect, Params>,
    UIState,
    UIEvent,
    NavEvent,
    UIEffect,
    reified Params,
    > BaseRoute(
  backStackEntry: NavBackStackEntry,
  noinline router: @Composable (Flow<NavEvent>) -> Unit,
  content: @Composable (UIState, (UIEvent) -> Unit, Flow<UIEffect>) -> Unit,
) {
  val params = backStackEntry.toRoute<Params>()
  val viewModel: VM = koinViewModel(parameters = { parametersOf(params) })
  val state by viewModel.stateFlow.collectAsState()
  content(state, viewModel::emitUIEvent, viewModel.uiEffects)
  router(viewModel.navEvents)
}
