package com.callcenter.dicodingevent

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse

class EventViewModel : ViewModel() {

    private val repository = EventRepository()

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> get() = _events

    private val _event = MutableLiveData<Event?>()
    val event: LiveData<Event?> get() = _event

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> get() = _errorMessage

    fun fetchEvents(status: Int) {
        _isLoading.value = true
        _errorMessage.value = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getEvents(status).awaitResponse()
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _events.value = response.body()?.listEvents ?: emptyList()
                    } else {
                        _events.value = emptyList()
                        _errorMessage.value = "Terjadi kesalahan API: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _events.value = emptyList()
                    _errorMessage.value = "Tidak ada koneksi internet atau kesalahan server"
                    Log.e("EventViewModel", "Error: ${e.message}", e)
                }
            }
        }
    }

    fun searchEvents(status: Int, query: String) {
        _isLoading.value = true
        _errorMessage.value = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.searchEvents(status, query).awaitResponse()
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    if (response.isSuccessful) {
                        _events.value = response.body()?.listEvents ?: emptyList()
                    } else {
                        _events.value = emptyList()
                        _errorMessage.value = "Terjadi kesalahan API: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _events.value = emptyList()
                    _errorMessage.value = "Tidak ada koneksi internet atau kesalahan server"
                    Log.e("EventViewModel", "Error: ${e.message}", e)
                }
            }
        }
    }

    fun fetchEventDetails(eventId: Int) {
        _isLoading.value = true
        Log.d("EventViewModel", "Loading started")
        _errorMessage.value = null

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getEventDetails(eventId).awaitResponse()
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    Log.d("EventViewModel", "Loading ended")
                    if (response.isSuccessful) {
                        val eventResponse = response.body()
                        _event.value = eventResponse?.event
                        Log.d("EventViewModel", "Event details fetched successfully: ${eventResponse?.event}")
                    } else {
                        Log.e("EventViewModel", "Failed to fetch event details: ${response.message()}")
                        _errorMessage.value = "Terjadi kesalahan API: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _isLoading.value = false
                    _event.value = null
                    _errorMessage.value = "Tidak ada koneksi internet atau kesalahan server"
                    Log.e("EventViewModel", "Error: ${e.message}", e)
                }
            }
        }
    }

    fun register(link: String) {
        Intent(Intent.ACTION_VIEW, Uri.parse(link))
    }
}
