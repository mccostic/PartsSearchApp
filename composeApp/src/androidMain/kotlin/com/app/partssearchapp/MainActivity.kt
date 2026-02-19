package com.app.partssearchapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.app.partssearchapp.database.DatabaseDriverFactory
import org.koin.dsl.module

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val platformModule = module {
            single { DatabaseDriverFactory(this@MainActivity) }
        }

        setContent {
            App(platformModule)
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}
