package com.app.partssearchapp.arch

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.partssearchapp.global.DefaultGlobalError
import com.app.partssearchapp.global.GlobalPopupCenter
import com.app.partssearchapp.global.GlobalSnackbarCenter
import com.app.partssearchapp.global.fullScreenPopup
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * Base ViewModel with common functionality including generic parameter handling
 */

abstract class BaseViewModel<UIState, UIEvent, NavEvent, UIEffect, Params>(
  protected val params: Params,
  initialState: UIState,
) : ViewModel() {

  // Non-suspending-friendly events pipe for UI -> ViewModel
  private val _uiEvents = MutableSharedFlow<UIEvent>(replay = 0, extraBufferCapacity = 64)
  protected val uiEvents = _uiEvents.asSharedFlow()

  private val _stateFlow = MutableStateFlow(initialState)
  val stateFlow: StateFlow<UIState> = _stateFlow.asStateFlow()

  // Navigation events can be observed externally, keep public
  private val _navEvents = MutableSharedFlow<NavEvent>(replay = 0, extraBufferCapacity = 64)
  val navEvents = _navEvents.asSharedFlow()

  private val _uiEffects = Channel<UIEffect>(capacity = Channel.BUFFERED)
  val uiEffects = _uiEffects.receiveAsFlow()

  private val cleanupActions = mutableListOf<() -> Unit>()

  fun emitUIEvent(event: UIEvent) {
    if (!_uiEvents.tryEmit(event)) {
      viewModelScope.launch { _uiEvents.emit(event) }
    }
  }

  protected fun emitNavEvent(event: NavEvent) {
    if (!_navEvents.tryEmit(event)) {
      viewModelScope.launch { _navEvents.emit(event) }
    }
  }

  protected fun emitUIEffect(effect: UIEffect) {
    val offered = _uiEffects.trySend(effect).isSuccess
    if (!offered) {
      viewModelScope.launch { _uiEffects.send(effect) }
    }
  }

  protected fun updateState(update: UIState.() -> UIState) {
    _stateFlow.value = update(_stateFlow.value)
  }

  protected fun setState(newState: UIState) {
    _stateFlow.value = newState
  }

  protected val currentState: UIState get() = _stateFlow.value

  protected fun registerCleanup(action: () -> Unit) {
    cleanupActions += action
  }

  override fun onCleared() {
    cleanupActions.forEach { runCatching { it() } }
    cleanupActions.clear()
    _uiEffects.close()
    super.onCleared()
  }

  protected fun <T : Any> registerListener(listener: T): String {
    val token = GlobalListenerRegistry.register(listener)
    registerCleanup { GlobalListenerRegistry.unregister(token) }
    return token
  }

  /** Global exception handler applied to all viewModel launches unless overridden */
  private val globalExceptionHandler = CoroutineExceptionHandler { _, throwable ->
    onGlobalCoroutineException(throwable)
  }

  /** Override to map exceptions to UI state/effects/logging */
  protected open fun onGlobalCoroutineException(throwable: Throwable) {
    println("[BaseViewModel] Unhandled coroutine exception: $throwable")
    // Show a generic full-screen error via the global popup layer
    GlobalPopupCenter.showNow(fullScreenPopup(dedupKey = "global_error_default") {
      DefaultGlobalError(
        message = "Something went wrong", onDismiss = { GlobalPopupCenter.dismiss(dedupKey = "global_error_default") })
    })
  }

  /** Helper to report non-fatal errors from subclasses */
  @Suppress("UNUSED_PARAMETER")
  protected fun reportGlobalError(
    message: String = "Something went wrong",
    throwable: Throwable,
    fullScreen: Boolean = true,
  ) {
    println("[BaseViewModel] reportGlobalError: $message | cause=$throwable")
    viewModelScope.launch {
      if (fullScreen) {
        GlobalPopupCenter.show(fullScreenPopup(dedupKey = message.take(40)) {
          DefaultGlobalError(message = message) { GlobalPopupCenter.dismiss(dedupKey = message.take(40)) }
        })
      } else {
        GlobalSnackbarCenter.showSnackbar(message)
      }
    }
  }

  /** Preferred launcher for ViewModels: Default dispatcher + global exception handler */
  protected fun launch(block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch(Dispatchers.Default + globalExceptionHandler) { block() }

  /** Helper to launch with global handler on Main (when UI-bound work is needed) */
  protected fun launchMain(block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch(globalExceptionHandler) { block() }

  /** Helper to launch with global handler on IO */
  protected fun launchIO(block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch(Dispatchers.IO + globalExceptionHandler) { block() }

  /** Helper to launch without handler (fire-and-forget or when you handle try/catch locally) */
  protected fun launchRaw(block: suspend CoroutineScope.() -> Unit): Job =
    viewModelScope.launch { block() }

  /** Convenience popup helpers for subclasses */
  protected fun showSnackbar(message: String) {
    GlobalSnackbarCenter.showSnackbar(message)
  }
  
  protected fun showSuccessSnackbar(message: String) {
      com.app.partssearchapp.global.showSuccessSnackbar(message)
  }
  
  protected fun showErrorSnackbar(message: String) {
      com.app.partssearchapp.global.showErrorSnackbar(message)
  }
  
  protected fun showCustomSnackbar(content: @Composable () -> Unit, duration: Long = 3000L) {
    GlobalSnackbarCenter.showCustomContent(duration = duration, content = content)
  }

  protected fun showFullScreenError(message: String, dedup: Boolean = false) {
    viewModelScope.launch { GlobalPopupCenter.showFullScreenError(message, dedup = dedup) }
  }

  companion object {
    inline fun <reified T : Any> getListener(token: String): T =
      GlobalListenerRegistry.getTyped<T>(token)
  }
}
