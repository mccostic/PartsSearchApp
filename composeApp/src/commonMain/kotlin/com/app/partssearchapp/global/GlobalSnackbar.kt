package com.app.partssearchapp.global

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Dedicated snackbar management using Material3's SnackbarHostState */
object GlobalSnackbarCenter {
  private val _commands = MutableSharedFlow<SnackbarCommand>(extraBufferCapacity = 64)
  val commands = _commands.asSharedFlow()
  
  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private val mutex = Mutex()
  private val pendingMessages = ArrayDeque<SnackbarCommand>()
  private var isShowing = false

  /** Show a snackbar with the given message and duration */
  fun showSnackbar(
    message: String,
    actionLabel: String? = null,
    withDismissAction: Boolean = false,
    duration: SnackbarDuration = SnackbarDuration.Short
  ) {
    val command = SnackbarCommand.SimpleMessage(
      message = message,
      actionLabel = actionLabel,
      withDismissAction = withDismissAction,
      duration = duration
    )
    
    scope.launch {
      mutex.withLock {
        if (isShowing) {
          pendingMessages.addLast(command)
        } else {
          isShowing = true
          _commands.tryEmit(command)
        }
      }
    }
  }

  /** Show a custom snackbar with custom visuals */
  fun showCustomSnackbar(
    visuals: SnackbarVisuals
  ) {
    val command = SnackbarCommand.CustomVisuals(visuals)
    
    scope.launch {
      mutex.withLock {
        if (isShowing) {
          pendingMessages.addLast(command)
        } else {
          isShowing = true
          _commands.tryEmit(command)
        }
      }
    }
  }

  /** Show a fully custom snackbar with custom composable content */
  fun showCustomContent(
    duration: Long = 3000L,
    content: @Composable () -> Unit
  ) {
    val command = SnackbarCommand.CustomContent(
      duration = duration,
      content = content
    )
    
    scope.launch {
      mutex.withLock {
        if (isShowing) {
          pendingMessages.addLast(command)
        } else {
          isShowing = true
          _commands.tryEmit(command)
        }
      }
    }
  }

  /** Called internally when a snackbar is dismissed to show next pending one */
  internal suspend fun onSnackbarDismissed() {
    mutex.withLock {
      val next = pendingMessages.removeFirstOrNull()
      if (next != null) {
        _commands.tryEmit(next)
      } else {
        isShowing = false
      }
    }
  }
}

/** Command to show a snackbar */
sealed class SnackbarCommand {
  data class SimpleMessage(
    val message: String,
    val actionLabel: String? = null,
    val withDismissAction: Boolean = false,
    val duration: SnackbarDuration = SnackbarDuration.Short
  ) : SnackbarCommand()

  data class CustomVisuals(
    val visuals: SnackbarVisuals
  ) : SnackbarCommand()

  data class CustomContent(
    val duration: Long = 3000L,
    val content: @Composable () -> Unit
  ) : SnackbarCommand()
}

/** Custom SnackbarHost that can handle both regular snackbars and custom content */
@Composable
fun CustomSnackbarHost() {
  val snackbarHostState = remember { SnackbarHostState() }
  var customContent by remember { mutableStateOf<(@Composable () -> Unit)?>(null) }
  
  // Handle regular snackbar commands
  LaunchedEffect(Unit) {
    GlobalSnackbarCenter.commands.collect { command ->
      try {
        when (command) {
          is SnackbarCommand.SimpleMessage -> {
            customContent = null // Clear custom content
            snackbarHostState.showSnackbar(
              message = command.message,
              actionLabel = command.actionLabel,
              withDismissAction = command.withDismissAction,
              duration = command.duration
            )
          }
          is SnackbarCommand.CustomVisuals -> {
            customContent = null // Clear custom content
            snackbarHostState.showSnackbar(command.visuals)
          }
          is SnackbarCommand.CustomContent -> {
            // Show custom content instead of regular snackbar
            customContent = command.content
            delay(command.duration)
            customContent = null
          }
        }
      } finally {
        GlobalSnackbarCenter.onSnackbarDismissed()
      }
    }
  }
  
  // Show either regular snackbar host or custom content
  if (customContent != null) {
    customContent?.invoke()
  } else {
    SnackbarHost(snackbarHostState) { snackbarData ->
      Snackbar(snackbarData)
    }
  }
}

/** Compatibility wrapper over the old GlobalSnackbarManager API */
object GlobalSnackbarManager {
  fun show(spec: SnackbarSpec) {
    if (spec.content != {}) {
      // If custom content is provided, use it
      GlobalSnackbarCenter.showCustomContent(
        duration = spec.durationMillis,
        content = spec.content
      )
    } else {
      // Use simple message
      GlobalSnackbarCenter.showSnackbar(
        message = spec.message ?: "No message",
        duration = when (spec.durationMillis) {
          in 0..1500 -> SnackbarDuration.Short
          in 1501..4000 -> SnackbarDuration.Long
          else -> SnackbarDuration.Indefinite
        }
      )
    }
  }
}

/** Data class kept for source compatibility */
data class SnackbarSpec(
  val message: String? = null,
  val durationMillis: Long = 3000L,
  val content: @Composable () -> Unit = {},
)

// Convenience helper functions for different snackbar types
fun showSuccessSnackbar(message: String) {
  GlobalSnackbarCenter.showCustomContent {
    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer
      )
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.CheckCircle,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = message,
          color = MaterialTheme.colorScheme.onPrimaryContainer
        )
      }
    }
  }
}

fun showErrorSnackbar(message: String) {
  GlobalSnackbarCenter.showCustomContent {
    Card(
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.errorContainer
      )
    ) {
      Row(
        modifier = Modifier.padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Error,
          contentDescription = null,
          tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = message,
          color = MaterialTheme.colorScheme.onErrorContainer
        )
      }
    }
  }
}
