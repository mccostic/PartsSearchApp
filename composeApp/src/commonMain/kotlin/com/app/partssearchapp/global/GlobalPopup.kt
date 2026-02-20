package com.app.partssearchapp.global

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlin.random.Random
import kotlin.time.ExperimentalTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/** Unified global popup system for full-screen errors and overlays (snackbars handled separately) */
object GlobalPopupCenter {
    // Public state (collect in UI). Null = nothing showing
    private val _current = MutableStateFlow<PopupSpec?>(null)
    val current: StateFlow<PopupSpec?> = _current.asStateFlow()

    // Backwards-compatibility flow (emits only non-null specs)
    @Deprecated("Use current StateFlow instead")
    val flow: Flow<PopupSpec> = current.filterNotNull()

    private val mutex = Mutex()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    /** Show the spec - only supports FullScreen and Overlay kinds */
    suspend fun show(spec: PopupSpec) {
        mutex.withLock {
            // De-dup: if a popup with same dedupKey is already current, drop
            spec.dedupKey?.let { key ->
                if (_current.value?.dedupKey == key) return
            }

            // Replace current popup
            setCurrentLocked(spec)
        }
    }

    /** Fire & forget variant returning success (always true for now) */
    fun showNow(spec: PopupSpec): Boolean {
        scope.launch { show(spec) }
        return true
    }

    /** Dismiss current popup if id/dedupKey match (or unconditional if both null) */
    fun dismiss(id: Long? = null, dedupKey: String? = null) {
        scope.launch { dismissInternal(id, dedupKey) }
    }

    private suspend fun dismissInternal(id: Long?, dedupKey: String?) {
        mutex.withLock {
            val cur = _current.value ?: return
            val match = when {
                id != null -> cur.id == id
                dedupKey != null -> cur.dedupKey == dedupKey
                else -> true
            }
            if (match) {
                cur.onDismiss?.invoke()
                _current.value = null
            }
        }
    }

    private fun setCurrentLocked(spec: PopupSpec) {
        _current.value = spec
        val duration = spec.durationMillis
        if (duration != null) {
            scope.launch {
                delay(duration)
                if (_current.value?.id == spec.id) {
                    dismissInternal(spec.id, null)
                }
            }
        }
    }

    // Convenience helpers (suspending)
    suspend fun showFullScreenError(message: String, dedup: Boolean = false) = show(
        fullScreenPopup(dedupKey = if (dedup) message else null) {
            DefaultGlobalError(message = message) { dismiss(dedupKey = if (dedup) message else null) }
        }
    )
}

enum class PopupKind { FullScreen, Overlay }

/** Specification for what to show on the global layer */
data class PopupSpec @OptIn(ExperimentalTime::class) constructor(
    val id: Long = Random.nextLong(),
    val kind: PopupKind = PopupKind.Overlay,
    val alignment: Alignment = when (kind) {
        PopupKind.FullScreen -> Alignment.Center
        PopupKind.Overlay -> Alignment.Center
    },
    val scrimColor: Color? = when (kind) {
        PopupKind.FullScreen -> Color(0x80000000)
        else -> null
    },
    val durationMillis: Long? = null,
    val dismissOnTapOutside: Boolean = false,
    // Whether taps outside content should be intercepted (prevent passing through).
    // Default: true for FullScreen & Overlay.
    val blockOutside: Boolean = true,
    val dedupKey: String? = null,
    val onDismiss: (() -> Unit)? = null,
    val content: @Composable () -> Unit,
)

// Convenience builders
fun fullScreenPopup(
    dedupKey: String? = null,
    onDismiss: (() -> Unit)? = null,
    blockOutside: Boolean = true,
    content: @Composable () -> Unit
) = PopupSpec(
    kind = PopupKind.FullScreen,
    dedupKey = dedupKey,
    onDismiss = onDismiss,
    blockOutside = blockOutside,
    content = content
)

fun overlayPopup(
    alignment: Alignment = Alignment.Center,
    scrimColor: Color? = null,
    durationMillis: Long? = null,
    dedupKey: String? = null,
    onDismiss: (() -> Unit)? = null,
    blockOutside: Boolean = true,
    content: @Composable () -> Unit
) = PopupSpec(
    kind = PopupKind.Overlay,
    alignment = alignment,
    scrimColor = scrimColor,
    durationMillis = durationMillis,
    dedupKey = dedupKey,
    onDismiss = onDismiss,
    blockOutside = blockOutside,
    content = content
)

@Composable
fun GlobalPopupLayer(content: @Composable () -> Unit) {
    content()

    val spec by GlobalPopupCenter.current.collectAsState()
    if (spec != null) {
        val theSpec = spec!!
        Popup(alignment = theSpec.alignment) {
            // Outer intercept layer—covers whole area when blocking outside
            val interceptModifier = Modifier
                .let { m ->
                    if (theSpec.kind == PopupKind.FullScreen && theSpec.scrimColor != null) {
                        m.fillMaxSize().background(theSpec.scrimColor)
                    } else if (theSpec.blockOutside) {
                        m.fillMaxSize()
                    } else {
                        m
                    }
                }
                .let { m ->
                    if (theSpec.blockOutside) {
                        // Consume all pointer events so they don't fall through
                        m.pointerInput(theSpec.id) {
                            detectTapGestures(onTap = {
                                if (theSpec.dismissOnTapOutside) GlobalPopupCenter.dismiss(id = theSpec.id)
                            })
                        }.semantics { contentDescription = "GlobalPopupOverlay" }
                    } else {
                        m
                    }
                }

            Box(modifier = interceptModifier, contentAlignment = Alignment.Center) {
                Box(
                    modifier = Modifier
                        .let { inner ->
                            if (!theSpec.blockOutside && theSpec.dismissOnTapOutside) {
                                // If not blocking outside, allow clicking directly on content to dismiss
                                inner.clickable { GlobalPopupCenter.dismiss(id = theSpec.id) }
                            } else {
                                inner
                            }
                        }
                ) {
                    AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                        theSpec.content()
                    }
                }
            }
        }
    }
}

// Simple default full-screen error content for convenience
@Composable
fun DefaultGlobalError(message: String, onDismiss: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "Oops!", style = MaterialTheme.typography.headlineSmall)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 8.dp))
            Button(onClick = onDismiss, modifier = Modifier.padding(top = 16.dp)) { Text("Dismiss") }
        }
    }
}
