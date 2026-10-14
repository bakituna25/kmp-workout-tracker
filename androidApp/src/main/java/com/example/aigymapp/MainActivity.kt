package com.example.aigymapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.aigymapp.database.DatabaseDriverFactory
import com.example.aigymapp.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App(
                databaseDriverFactory = DatabaseDriverFactory(applicationContext)
            )
        }
    }
}
