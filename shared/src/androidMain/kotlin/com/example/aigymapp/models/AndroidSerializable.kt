package com.example.aigymapp.models

import java.io.Serializable

/**
 * Extension to make WorkoutSession compatible with Android Intent extras
 */
actual interface AndroidSerializable : Serializable

// Extension to mark WorkoutSession as Android Serializable
fun WorkoutSession.asAndroidSerializable(): Any = this as Serializable
fun WorkoutTemplate.asAndroidSerializable(): Any = this as Serializable

