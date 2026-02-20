package com.app.partssearchapp.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSBundle
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSSearchPathForDirectoriesInDomains
import platform.Foundation.NSUserDomainMask

actual class DatabaseDriverFactory {
    @OptIn(ExperimentalForeignApi::class)
    actual fun createDriver(): SqlDriver {
        val dbName = "vpic_lite.db"
        val documentsPath = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).first() as String
        val dbPath = "$documentsPath/$dbName"

        val fileManager = NSFileManager.defaultManager
        if (!fileManager.fileExistsAtPath(dbPath)) {
            val bundlePath = NSBundle.mainBundle.pathForResource("vpic_lite", "db")
            if (bundlePath != null) {
                fileManager.copyItemAtPath(bundlePath, dbPath, null)
            }
        }

        return NativeSqliteDriver(
            schema = VpicDatabase.Schema,
            name = dbName,
        )
    }
}
