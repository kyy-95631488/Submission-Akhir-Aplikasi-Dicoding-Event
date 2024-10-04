package com.callcenter.dicodingevent

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.appcompat.widget.SwitchCompat // Updated import
import androidx.appcompat.app.AppCompatActivity

class SettingsFragment : Fragment() {

    private lateinit var darkModeSwitch: SwitchCompat // Change to SwitchCompat
    private lateinit var dailyReminderSwitch: SwitchCompat // Change to SwitchCompat
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Set up toolbar
        val toolbar: Toolbar = view.findViewById(R.id.toolbar_settings)
        (activity as? AppCompatActivity)?.setSupportActionBar(toolbar)
        (activity as? AppCompatActivity)?.supportActionBar?.title = "Settings"

        sharedPreferences = requireActivity().getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Dark Mode
        darkModeSwitch = view.findViewById(R.id.switch_mode)
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        darkModeSwitch.isChecked = isDarkMode

        darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            sharedPreferences.edit().putBoolean("dark_mode", isChecked).apply()
        }

        // Daily Reminder
        dailyReminderSwitch = view.findViewById(R.id.switch_daily_reminder)
        val isDailyReminderEnabled = sharedPreferences.getBoolean("daily_reminder", false)
        dailyReminderSwitch.isChecked = isDailyReminderEnabled

        dailyReminderSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPreferences.edit().putBoolean("daily_reminder", isChecked).apply()
            if (isChecked) {
                scheduleDailyReminder(requireContext())
            } else {
                cancelDailyReminder(requireContext())
            }
        }

        return view
    }

    private fun scheduleDailyReminder(context: Context) {
        // Schedule a daily reminder using WorkManager, AlarmManager, or a JobScheduler
        DailyReminderScheduler.scheduleDailyReminder(context)
    }

    private fun cancelDailyReminder(context: Context) {
        // Cancel the scheduled reminder
        DailyReminderScheduler.cancelDailyReminder(context)
    }
}
