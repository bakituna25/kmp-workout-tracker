package com.example.aigymapp.database

import app.cash.sqldelight.Query
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow

/**
 * Extension functions to convert SQLDelight queries to Flow
 */

fun <T : Any> Query<T>.asFlowList(): Flow<List<T>> {
    return this.asFlow().mapToList(Dispatchers.Default)
}

fun <T : Any> Query<T>.asFlowOne(): Flow<T> {
    return this.asFlow().mapToOne(Dispatchers.Default)
}

fun <T : Any> Query<T>.asFlowOneOrNull(): Flow<T?> {
    return this.asFlow().mapToOneOrNull(Dispatchers.Default)
}

