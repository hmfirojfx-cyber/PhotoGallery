package com.example.photogallery

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.photogallery.databinding.ActivityFullScreenViewerBinding
import com.example.photogallery.model.Photo
import java.io.OutputStream

class FullScreenViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullScreenViewerBinding
    private lateinit var photo: Photo

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFullScreenViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        photo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            intent.getSerializableExtra("PHOTO_DATA", Photo::class.java) ?: Photo()
        } else {
            @Suppress("DEPRECATION")
            intent.getSerializableExtra("PHOTO_DATA") as? Photo ?: Photo()
        }

        binding.tvPhotoTitle.text = photo.title
        binding.tvPhotoDesc.text = photo.description

        // Glide image load with pinch-to-zoom PhotoView support
        Glide.with(this)
            .load(photo.imageUrl)
            .into(binding.ivFullScreen)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnShare.setOnClickListener {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Check out: ${photo.title}\n${photo.imageUrl}")
                type = "text/plain"
            }
            startActivity(Intent.createChooser(intent, "Share Photo"))
        }

        binding.btnDownload.setOnClickListener {
            downloadImage(photo.imageUrl, photo.title)
        }

        binding.btnFavorite.setOnClickListener {
            val prefs = getSharedPreferences("Favorites", MODE_PRIVATE)
            val isFav = prefs.getBoolean(photo.id, false)
            prefs.edit().putBoolean(photo.id, !isFav).apply()
            Toast.makeText(this, if (!isFav) "Added to Favorites ❤️" else "Removed from Favorites", Toast.LENGTH_SHORT).show()
        }
    }

    private fun downloadImage(url: String, title: String) {
        Glide.with(this).asBitmap().load(url).into(object : CustomTarget<Bitmap>() {
            override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                saveToGallery(resource, title)
            }
            override fun onLoadCleared(placeholder: Drawable?) {}
        })
    }

    private fun saveToGallery(bitmap: Bitmap, title: String) {
        val filename = "${title}_${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val resolver = contentResolver
                val values = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/PhotoGallery")
                }
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) fos = resolver.openOutputStream(uri)
            } else {
                val dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                fos = java.io.FileOutputStream(java.io.File(dir, filename))
            }
            fos?.use {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it)
                Toast.makeText(this, "Downloaded successfully!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Download failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}