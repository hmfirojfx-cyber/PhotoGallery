package com.example.adminphotogallery

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.adminphotogallery.databinding.ActivityAdminDashboardBinding
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminDashboardBinding
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = FirebaseFirestore.getInstance()

        binding.btnUpload.setOnClickListener {
            val title = binding.etTitle.text.toString().trim()
            val url = binding.etUrl.text.toString().trim()
            val category = binding.etCategory.text.toString().trim()
            if (title.isNotEmpty() && url.isNotEmpty()) {
                val map = hashMapOf("title" to title, "imageUrl" to url, "category" to category, "isPublished" to true, "createdAt" to System.currentTimeMillis())
                db.collection("photos").add(map).addOnSuccessListener {
                    Toast.makeText(this, "Uploaded!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}