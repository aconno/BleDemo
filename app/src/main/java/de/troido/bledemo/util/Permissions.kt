package de.troido.bledemo.util

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

inline fun Activity.checkPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
