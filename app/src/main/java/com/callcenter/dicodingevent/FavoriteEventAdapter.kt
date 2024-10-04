package com.callcenter.dicodingevent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.callcenter.dicodingevent.data.FavoriteEvent
import com.callcenter.dicodingevent.databinding.ItemFavoriteEventBinding

class FavoriteEventAdapter(private val onClick: (FavoriteEvent) -> Unit) : ListAdapter<FavoriteEvent, FavoriteEventViewHolder>(FavoriteEventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteEventViewHolder {
        val binding = ItemFavoriteEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteEventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteEventViewHolder, position: Int) {
        val event = getItem(position)
        holder.bind(event)
        holder.itemView.setOnClickListener { onClick(event) }
    }
}

class FavoriteEventViewHolder(private val binding: ItemFavoriteEventBinding) : RecyclerView.ViewHolder(binding.root) {

    fun bind(event: FavoriteEvent) {
        binding.event = event
        binding.executePendingBindings()

        // Load the image using Glide
        Glide.with(binding.eventImage.context)
            .load(event.mediaCover) // Assuming `mediaCover` is the URL to the image
            .placeholder(R.drawable.default_featured_image_png) // Optional placeholder image
            .error(R.drawable.rounded_button) // Optional error image
            .into(binding.eventImage)
    }
}

class FavoriteEventDiffCallback : DiffUtil.ItemCallback<FavoriteEvent>() {
    override fun areItemsTheSame(oldItem: FavoriteEvent, newItem: FavoriteEvent): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FavoriteEvent, newItem: FavoriteEvent): Boolean {
        return oldItem == newItem
    }
}
