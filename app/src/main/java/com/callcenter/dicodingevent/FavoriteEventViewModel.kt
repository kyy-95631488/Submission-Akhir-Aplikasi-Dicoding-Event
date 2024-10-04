package com.callcenter.dicodingevent

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.callcenter.dicodingevent.data.FavoriteEvent
import com.callcenter.dicodingevent.data.FavoriteEventDao
import com.callcenter.dicodingevent.data.AppDatabase

class FavoriteEventViewModel(application: Application) : AndroidViewModel(application) {

    private val favoriteEventDao: FavoriteEventDao = AppDatabase.getDatabase(application).favoriteEventDao()
    val favoriteEvents: LiveData<List<FavoriteEvent>> = favoriteEventDao.getAllFavoriteEvents()

}
