package com.callcenter.dicodingevent

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private val PERMISSION_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

        // Apply the theme based on the saved preference
        val isDarkMode = sharedPreferences.getBoolean("dark_mode", false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        setContentView(R.layout.activity_main)

        // Setup bottom navigation
        val bottomNavigationView: BottomNavigationView = findViewById(R.id.bottom_nav)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_upcoming_events -> {
                    switchToEventFragment(1) // Upcoming events
                    true
                }
                R.id.action_past_events -> {
                    switchToEventFragment(0) // Past events
                    true
                }
                R.id.action_favorite -> {
                    switchToFavoriteFragment() // Favorites
                    true
                }
                R.id.action_settings -> {
                    switchToSettingsFragment() // Settings
                    true
                }
                else -> false
            }
        }

        // Load the default fragment on startup
        if (savedInstanceState == null) {
            switchToEventFragment(1) // Default to upcoming events
        }

        // Check and request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermissionAndProceed()
        } else {
            // For Android versions below 13, fetch event and show notification
            fetchLatestEventAndNotify()
        }
    }

    private fun switchToEventFragment(status: Int) {
        val fragment = EventListFragment.newInstance(status)
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun switchToSettingsFragment() {
        val fragment = SettingsFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    private fun switchToFavoriteFragment() {
        val fragment = FavoriteFragment.newInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .commit()
    }

    // Check if notification permission is granted, if not request it
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkNotificationPermissionAndProceed() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            // Permission is already granted, proceed to fetch and notify
            fetchLatestEventAndNotify()
        } else {
            // Request permission
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // Handle permission result
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, fetch event and show notification
                fetchLatestEventAndNotify()
            } else {
                // Handle the case where the permission was denied
                Log.w("MainActivity", "Notification permission denied by the user")
            }
        }
    }

    // Fetch latest event from API and show notification
    private fun fetchLatestEventAndNotify() {
        val eventRepository = EventRepository()
        eventRepository.getEvents(-1).enqueue(object : Callback<EventResponse> {
            override fun onResponse(call: Call<EventResponse>, response: Response<EventResponse>) {
                if (response.isSuccessful) {
                    val event = response.body()?.events?.firstOrNull()
                    event?.let {
                        showNotification(it.name, it.description)
                    }
                } else {
                    Log.e("MainActivity", "Failed to fetch events: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<EventResponse>, t: Throwable) {
                Log.e("MainActivity", "Error fetching events", t)
            }
        })
    }

    // Show notification with event details
    private fun showNotification(title: String, content: String) {
        val channelId = "event_notification_channel"
        val channelName = "Event Notification"
        val notificationId = 1

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(notificationChannel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Check if the notification permission is granted
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            Log.w("MainActivity", "Notification permission not granted, cannot display notification")
            return
        }

        // Show the notification
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }
}
