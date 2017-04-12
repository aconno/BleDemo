package de.troido.bledemo.util

import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.view.View

inline fun View.snack(duration: Int, text: String) =
        Snackbar.make(this, text, duration).show()

inline fun View.snack(duration: Int, @StringRes text: Int) =
        Snackbar.make(this, text, duration).show()

inline fun View.shortSnack(text: String) = snack(Snackbar.LENGTH_SHORT, text)

inline fun View.shortSnack(@StringRes text: Int) = snack(Snackbar.LENGTH_SHORT, text)

inline fun View.longSnack(text: String) = snack(Snackbar.LENGTH_LONG, text)

inline fun View.longSnack(@StringRes text: Int) = snack(Snackbar.LENGTH_LONG, text)

inline fun View.indefiniteSnack(text: String) = snack(Snackbar.LENGTH_INDEFINITE, text)

inline fun View.indefiniteSnack(@StringRes text: Int) = snack(Snackbar.LENGTH_INDEFINITE, text)
