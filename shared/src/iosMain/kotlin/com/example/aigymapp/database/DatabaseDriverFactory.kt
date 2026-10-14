package com.example.aigymapp.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        // Note: iOS does not support runtime migration like Android
        // If schema changes, user must delete and reinstall the app
        return NativeSqliteDriver(AppDatabase.Schema, "aigym.db")
    }
}

