package com.app.partssearchapp.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import java.io.FileOutputStream

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        val dbName = "vpic_lite.db"
        val dbFile = context.getDatabasePath(dbName)

        if (!dbFile.exists()) {
            dbFile.parentFile?.mkdirs()
            context.assets.open(dbName).use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }
        }

        return AndroidSqliteDriver(
            schema = VpicDatabase.Schema,
            context = context,
            name = dbName,
        )
    }
}
