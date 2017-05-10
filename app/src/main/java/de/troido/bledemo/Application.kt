package de.troido.bledemo

import android.content.Context
import android.content.SharedPreferences

class Application : android.app.Application() {

    companion object {
        const val PREFERENCES = "preferences"
    }

    override fun onCreate() {
        super.onCreate()
    }
}

val Context.sharedPreferences: SharedPreferences
    get() = getSharedPreferences(
            Application.PREFERENCES,
            Context.MODE_PRIVATE
    )
