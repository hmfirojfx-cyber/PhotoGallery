package com.example.photogallery

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.photogallery.adapter.PhotoAdapter
import com.example.photogallery.databinding.ActivityMainBinding
import com.example.photogallery.model.Photo
import com.google.android.material.chip.Chip
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: FirebaseFirestore
    private lateinit var photoAdapter: PhotoAdapter
    private val photoList = mutableListOf<Photo>()
    private val filteredList = mutableListOf<Photo>()
    private var currentCategory: String = "All"
    private var currentSearchQuery: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseFirestore.getInstance()
        try {
            db.firestoreSettings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()
        } catch (e: Exception) {
            // Already initialized
        }

        setupRecyclerView()
        setupSearchAndFilter()
        checkNetworkStatus()
        fetchPhotosRealtime()
        fetchCategoriesForChips()

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun setupRecyclerView() {
        photoAdapter = PhotoAdapter(this, filteredList) { photo ->
            val intent = Intent(this, FullScreenViewerActivity::class.java)
            intent.putExtra("PHOTO_DATA", photo)
            startActivity(intent)
        }
        binding.recyclerView.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerView.adapter = photoAdapter
    }

    private fun setupSearchAndFilter() {
        // Search Query Listener
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                currentSearchQuery = query ?: ""
                filterPhotos()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                filterPhotos()
                return true
            }
        })
    }

    private fun fetchCategoriesForChips() {
        db.collection("categories").addSnapshotListener { snapshots, _ ->
            if (snapshots != null) {
                binding.chipGroupCategories.removeAllViews()
                
                // Add "All" default chip
                val allChip = Chip(this).apply {
                    text = "All"
                    isCheckable = true
                    isChecked = true
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            currentCategory = "All"
                            filterPhotos()
                        }
                    }
                }
                binding.chipGroupCategories.addView(allChip)

                // Add dynamic category chips
                for (doc in snapshots) {
                    val catName = doc.getString("name") ?: continue
                    val chip = Chip(this).apply {
                        text = catName
                        isCheckable = true
                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                currentCategory = catName
                                filterPhotos()
                            }
                        }
                    }
                    binding.chipGroupCategories.addView(chip)
                }
            }
        }
    }

    private fun filterPhotos() {
        filteredList.clear()
        for (photo in photoList) {
            val matchesCategory = currentCategory == "All" || photo.category.equals(currentCategory, ignoreCase = true)
            val matchesSearch = photo.title.contains(currentSearchQuery, ignoreCase = true) || 
                                photo.description.contains(currentSearchQuery, ignoreCase = true)

            if (matchesCategory && matchesSearch) {
                filteredList.add(photo)
            }
        }
        photoAdapter.notifyDataSetChanged()
        binding.tvEmptyState.visibility = if (filteredList.isEmpty()