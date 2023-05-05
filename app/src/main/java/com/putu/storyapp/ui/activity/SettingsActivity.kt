package com.putu.storyapp.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import com.putu.storyapp.R
import com.putu.storyapp.databinding.ActivitySettingsBinding
import com.putu.storyapp.extra.UserPreferences
import com.putu.storyapp.viewmodel.SettingsViewModel
import com.putu.storyapp.viewmodel.ViewModelFactory

class SettingsActivity : AppCompatActivity() {

    private var _settingsBind: ActivitySettingsBinding? = null
    private val settingsBind get() = _settingsBind

    private lateinit var settingsViewModel: SettingsViewModel

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _settingsBind = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(settingsBind?.root)

        supportActionBar?.title = getString(R.string.settings_menu)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        setUpAction()
        setUpViewModel()
    }

    private fun setUpAction () {
        settingsBind?.cvLanguageSetting?.setOnClickListener {
            startActivity(Intent(Settings.ACTION_LOCALE_SETTINGS))
        }

        settingsBind?.cvLogout?.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            val alert = builder.create()
            builder
                .setTitle(R.string.logout_alert_title)
                .setMessage(R.string.logout_alert_message)
                .setPositiveButton(R.string.logout_alert_positive_button) { _, _ ->
                    settingsViewModel.logout()
                    finishAndRemoveTask()
                }

                .setNegativeButton(R.string.logout_alert_negative_button) {_, _ ->
                    alert.cancel()
                }
                .show()
        }
    }

    private fun setUpViewModel() {
        settingsViewModel = ViewModelProvider(this, ViewModelFactory(UserPreferences.getInstance(dataStore)))[SettingsViewModel::class.java]
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return super.onSupportNavigateUp()
    }
}