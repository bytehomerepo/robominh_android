package com.danh.feature_setting

import android.app.UiModeManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import java.util.Locale

class FragmentSetting : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupPreference()
    }

    private fun setupPreference() {
        val nightModePref = findPreference<SwitchPreferenceCompat>(KEY_PREF_DARK_MODE)
        nightModePref?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { preference, newValue ->
                changeTheme(preference, newValue)
                true
            }
        nightModePref?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val isDark = newValue.toString().toBoolean()
                ThemeManager.saveTheme(requireContext(), isDark)
                ThemeManager.applyTheme(isDark)
                requireActivity().recreate()
                true
            }
        nightModePref?.onPreferenceChangeListener =
            Preference.OnPreferenceChangeListener { _, newValue ->
                val isDark = newValue.toString().toBoolean()
                ThemeManager.saveTheme(requireContext(), isDark)
                ThemeManager.applyTheme(isDark)
                requireActivity().recreate()
                true
            }
    }

    private fun changeTheme(preference: Preference, newValue: Any) {
        val sharedPreference = preference.sharedPreferences
        val oldNightMode = sharedPreference?.getBoolean(KEY_PREF_DARK_MODE, false) ?: false
        val newNightMode = newValue.toString().toBoolean()
        if (oldNightMode != newNightMode) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val uiMode = if (newNightMode) {
                    UiModeManager.MODE_NIGHT_YES
                } else {
                    UiModeManager.MODE_NIGHT_NO
                }
                val uiManager = requireContext().getSystemService(UiModeManager::class.java)
                uiManager.setApplicationNightMode(uiMode)
            } else {
                val uiMode = if (newNightMode) {
                    AppCompatDelegate.MODE_NIGHT_YES
                } else {
                    AppCompatDelegate.MODE_NIGHT_NO
                }
                AppCompatDelegate.setDefaultNightMode(uiMode)
            }
        }
    }

    companion object {
        const val KEY_PREF_DARK_MODE = "dark_mode"
    }
}