package de.troido.bledemo.util

import android.app.Activity
import android.content.pm.PackageManager
import android.support.v4.content.ContextCompat

inline fun Activity.checkPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) ==
                PackageManager.PERMISSION_GRANTED
