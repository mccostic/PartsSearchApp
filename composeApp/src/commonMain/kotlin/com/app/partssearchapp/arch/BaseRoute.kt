package com.app.partssearchapp.arch

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.toRoute
import com.app.partssearchapp.LocalApplicationComponent
import com.app.partssearchapp.di.ApplicationComponent
import kotlinx.coroutines.flow.Flow

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
  noinline componentProvider: (Params, ApplicationComponent) -> VM,
  noinline router: @Composable (Flow<NavEvent>) -> Unit,
  content: @Composable (UIState, (UIEvent) -> Unit, Flow<UIEffect>) -> Unit,
) {
  val applicationComponent = LocalApplicationComponent.current
  val params = backStackEntry.toRoute<Params>()
  val viewModel: VM = viewModel { componentProvider(params, applicationComponent) }
  val state by viewModel.stateFlow.collectAsState()
  content(state, viewModel::emitUIEvent, viewModel.uiEffects)
  router(viewModel.navEvents)
}