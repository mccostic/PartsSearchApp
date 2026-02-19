package com.app.partssearchapp

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.app.partssearchapp.di.appModule
import com.app.partssearchapp.global.CustomSnackbarHost
import com.app.partssearchapp.global.GlobalPopupLayer
import org.koin.compose.KoinApplication
import org.koin.core.module.Module
import org.koin.dsl.module

@Composable
@Preview
fun App(platformModule: Module = module { }) {
    KoinApplication(application = {
        modules(appModule, platformModule)
    }) {
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
