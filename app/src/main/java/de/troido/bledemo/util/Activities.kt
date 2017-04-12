package de.troido.bledemo.util

import android.app.Activity
import android.content.Context
import android.content.Intent

inline fun <reified A : Activity> Context.start(): Unit =
        startActivity(Intent(this, A::class.java))
