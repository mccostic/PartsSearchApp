package com.app.partssearchapp

import androidx.compose.ui.window.ComposeUIViewController
import com.app.partssearchapp.database.DatabaseDriverFactory
import org.koin.dsl.module

fun MainViewController() = ComposeUIViewController {
    val platformModule = module {
        single { DatabaseDriverFactory() }
    }
    App(platformModule)
}
