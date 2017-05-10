package de.troido.bledemo.util

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.support.v4.content.LocalBroadcastManager

inline fun <reified S : Service> Context.startService(): ComponentName =
        startService(Intent(this, S::class.java))

inline fun <reified S : Service> Context.stopService(): Boolean =
        stopService(Intent(this, S::class.java))

val Context.localBroadcastManager: LocalBroadcastManager
    get() = LocalBroadcastManager.getInstance(this)
