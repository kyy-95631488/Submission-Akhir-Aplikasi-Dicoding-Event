package com.callcenter.dicodingevent

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.callcenter.dicodingevent.databinding.FragmentFavoriteBinding
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo

class FavoriteFragment : Fragment() {

    private var _binding: FragmentFavoriteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoriteEventViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FavoriteEventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.id.toInt())
            startActivity(intent)
        }

        binding.recyclerViewMain.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewMain.adapter = adapter

        showLoadingSpinner()

        if (!isNetworkAvailable()) {
            hideLoadingSpinner()
            showNoConnectionMessage()
        } else {
            viewModel.favoriteEvents.observe(viewLifecycleOwner) { events ->
                hideLoadingSpinner()
                if (events.isNullOrEmpty()) {
                    showEmptyFavoritesMessage()
                } else {
                    hideEmptyFavoritesMessage()
                    adapter.submitList(events)
                }
            }
        }
    }

    private fun showEmptyFavoritesMessage() {
        binding.recyclerViewMain.visibility = View.GONE
        binding.emptyFavoritesMessage.visibility = View.VISIBLE
    }

    private fun hideEmptyFavoritesMessage() {
        binding.recyclerViewMain.visibility = View.VISIBLE
        binding.emptyFavoritesMessage.visibility = View.GONE
    }

    private fun showNoConnectionMessage() {
        binding.recyclerViewMain.visibility = View.GONE
        binding.noConnectionMessage.apply {
            visibility = View.VISIBLE
            text = getString(R.string.no_connection_message)
        }
    }

    private fun showLoadingSpinner() {
        binding.progressBarCenter.visibility = View.VISIBLE
    }

    private fun hideLoadingSpinner() {
        binding.progressBarCenter.visibility = View.GONE
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        @JvmStatic
        fun newInstance() = FavoriteFragment()
    }
}

