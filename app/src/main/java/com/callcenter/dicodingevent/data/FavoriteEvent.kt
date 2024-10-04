package com.callcenter.dicodingevent.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FavoriteEvent(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val mediaCover: String? = null
)
