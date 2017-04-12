package de.troido.bledemo.util

import android.app.Activity
import android.support.annotation.StringRes
import android.widget.Toast

inline fun Activity.toast(duration: Int, text: String) =
        Toast.makeText(this, text, duration).show()

inline fun Activity.shortToast(text: String) = toast(Toast.LENGTH_SHORT, text)
inline fun Activity.longToast(text: String) = toast(Toast.LENGTH_LONG, text)

inline fun Activity.toast(duration: Int, @StringRes text: Int) =
        Toast.makeText(this, text, duration).show()

inline fun Activity.shortToast(@StringRes text: Int) = toast(Toast.LENGTH_SHORT, text)
inline fun Activity.longToast(@StringRes text: Int) = toast(Toast.LENGTH_LONG, text)
