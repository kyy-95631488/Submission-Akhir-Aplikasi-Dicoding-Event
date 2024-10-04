package com.callcenter.dicodingevent

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.callcenter.dicodingevent.data.AppDatabase
import com.callcenter.dicodingevent.data.FavoriteEvent
import com.callcenter.dicodingevent.databinding.ActivityEventDetailBinding
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.text.Html
import android.text.method.LinkMovementMethod
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private val viewModel: EventViewModel by viewModels()
    private var isFavorite: Boolean = false
    private lateinit var currentEvent: FavoriteEvent

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_event_detail)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Set up the toolbar
        val toolbar = binding.toolbar
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true) // Show the back button

        if (isNetworkAvailable()) {
            val eventId = intent.getIntExtra("EVENT_ID", -1)
            if (eventId != -1) {
                // Show the loading spinner
                binding.loadingSpinner.visibility = View.VISIBLE

                // Fetch the event details using the ViewModel
                viewModel.fetchEventDetails(eventId)

                viewModel.event.observe(this, { event ->
                    if (event != null) {
                        binding.loadingSpinner.visibility = View.GONE
                        updateEventDetails(event)
                    }
                })

                viewModel.errorMessage.observe(this, { errorMessage ->
                    if (errorMessage != null) {
                        binding.loadingSpinner.visibility = View.GONE
                        showErrorState()
                        Snackbar.make(binding.root, errorMessage, Snackbar.LENGTH_LONG).show()
                    }
                })

                viewModel.isLoading.observe(this, { isLoading ->
                    if (isLoading) {
                        binding.loadingSpinner.visibility = View.VISIBLE
                    } else {
                        binding.loadingSpinner.visibility = View.GONE
                    }
                })
            }
        } else {
            showNoInternetMessage()
        }
    }

    private fun updateEventDetails(event: Event) {
        binding.event = event
        binding.eventTitle.text = event.name
        binding.eventDescription.text = Html.fromHtml(event.description, Html.FROM_HTML_MODE_COMPACT)
        binding.eventDescription.movementMethod = LinkMovementMethod.getInstance()
        Picasso.get().load(event.mediaCover).into(binding.eventImage)

        binding.eventBeginTime.text = getString(R.string.event_begin_time, event.beginTime)
        binding.eventEndTime.text = getString(R.string.event_end_time, event.endTime)

        val remainingQuota = event.quota - event.registrants
        binding.eventRemainingQuota.text = getString(R.string.event_remaining_quota, remainingQuota)
        binding.eventCityName.text = getString(R.string.event_city_name, event.cityName)
        binding.eventOwnerName.text = getString(R.string.event_owner_name, event.ownerName)
        binding.eventCategory.text = getString(R.string.event_category, event.category)

        val eventEndTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(event.endTime)?.time
        val currentTime = System.currentTimeMillis()

        if (eventEndTime != null && currentTime > eventEndTime) {
            // Event has passed: hide the register button and show "Event Ditutup"
            binding.registerButton.visibility = View.GONE
            binding.kuotaHabisText.text = getString(R.string.event_closed)
            binding.kuotaHabisText.visibility = View.VISIBLE
        } else if (remainingQuota > 0) {
            // Event is still open and quota is available
            binding.registerButton.visibility = View.VISIBLE
            binding.kuotaHabisText.visibility = View.GONE
            binding.registerButton.setOnClickListener {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(event.link))
                startActivity(intent)
            }
        } else {
            // No remaining quota
            binding.registerButton.visibility = View.GONE
            binding.kuotaHabisText.text = getString(R.string.kuota_habis)
            binding.kuotaHabisText.visibility = View.VISIBLE
        }

        // Initialize the current event
        currentEvent = FavoriteEvent(
            id = event.id.toString(),
            name = event.name,
            mediaCover = event.mediaCover
        )

        // Check if the event is already in favorites
        checkIfEventIsFavorite(currentEvent.id)

        // Handle favorite button click
        binding.favoriteButton.setOnClickListener {
            if (isFavorite) {
                removeEventFromFavorites(currentEvent)
                Snackbar.make(binding.root, "Event removed from favorites!", Snackbar.LENGTH_SHORT).show()
            } else {
                addEventToFavorites(currentEvent)
                Snackbar.make(binding.root, "Event added to favorites!", Snackbar.LENGTH_SHORT).show()
            }
            isFavorite = !isFavorite
            updateFavoriteButton()
        }

        updateFavoriteButton()
    }

    private fun showErrorState() {
        binding.materialCardView.visibility = View.GONE
        binding.eventTitle.visibility = View.GONE
        binding.eventDescription.visibility = View.GONE
        binding.eventImage.visibility = View.GONE
        binding.eventBeginTime.visibility = View.GONE
        binding.eventEndTime.visibility = View.GONE
        binding.eventRemainingQuota.visibility = View.GONE
        binding.eventCityName.visibility = View.GONE
        binding.eventOwnerName.visibility = View.GONE
        binding.eventCategory.visibility = View.GONE
        binding.registerButton.visibility = View.GONE
        binding.noInternetText.visibility = View.VISIBLE
    }

    private fun showNoInternetMessage() {
        showErrorState()
        binding.noInternetText.text = getString(R.string.no_internet_message)
    }

    private fun addEventToFavorites(event: FavoriteEvent) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.favoriteEventDao().insert(event)
        }
    }

    private fun removeEventFromFavorites(event: FavoriteEvent) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            db.favoriteEventDao().delete(event)
        }
    }

    private fun checkIfEventIsFavorite(eventId: String) {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch(Dispatchers.IO) {
            val event = db.favoriteEventDao().getEventById(eventId)
            isFavorite = event != null
            updateFavoriteButton()
        }
    }

    private fun updateFavoriteButton() {
        if (isFavorite) {
            binding.favoriteButton.setImageResource(R.drawable.ic_favorite)
        } else {
            binding.favoriteButton.setImageResource(R.drawable.ic_favorite_border)
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
