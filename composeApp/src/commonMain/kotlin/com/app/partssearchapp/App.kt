package com.app.partssearchapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import com.app.partssearchapp.di.ApplicationComponent
import com.app.partssearchapp.di.create
import com.app.partssearchapp.global.CustomSnackbarHost
import com.app.partssearchapp.global.GlobalPopupLayer
import org.jetbrains.compose.ui.tooling.preview.Preview

// CompositionLocal for accessing the ApplicationComponent throughout the app
val LocalApplicationComponent = compositionLocalOf<ApplicationComponent> {
  error("ApplicationComponent not provided")
}

@Composable
@Preview
fun App() {
  val applicationComponent = remember { ApplicationComponent::class.create() }

  CompositionLocalProvider(LocalApplicationComponent provides applicationComponent) {
    MaterialTheme {
      Scaffold(
        snackbarHost = { CustomSnackbarHost() }
      ) { _ ->
        GlobalPopupLayer {
            Navigator()
        }
      }
    }
  }
}