package com.example.aigymapp

import androidx.compose.ui.window.ComposeUIViewController
import com.example.aigymapp.database.DatabaseDriverFactory
import com.example.aigymapp.ui.App

/**
 * iOS Entry Point
 * Creates DatabaseDriverFactory and passes it to the App composable
 */
fun MainViewController() = ComposeUIViewController {
    // iOS DatabaseDriverFactory'yi oluştur
    val databaseDriverFactory = DatabaseDriverFactory()
    
    // App composable'ına geçir
    App(databaseDriverFactory = databaseDriverFactory)
}
