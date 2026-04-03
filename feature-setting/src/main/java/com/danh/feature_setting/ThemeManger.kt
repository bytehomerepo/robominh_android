package com.danh.feature_setting

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
object ThemeManager {

    private const val PREFS_NAME = "app_prefs"
    const val KEY_DARK_MODE = "dark_mode"
    fun applyThemeFromPrefs(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDark = prefs.getBoolean(KEY_DARK_MODE, false)
        applyTheme(isDark)
    }

    fun applyTheme(isDark: Boolean) {
        val mode = if (isDark) AppCompatDelegate.MODE_NIGHT_YES
        else AppCompatDelegate.MODE_NIGHT_NO
        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun saveTheme(context: Context, isDark: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK_MODE, isDark)
            .apply()
    }
}