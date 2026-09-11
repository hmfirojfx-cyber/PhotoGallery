package com.example.photogallery.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.photogallery.databinding.ItemPhotoBinding
import com.example.photogallery.model.Photo

class PhotoAdapter(
    private val context: Context,
    private var photoList: List<Photo>,
    private val onItemClick: (Photo) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    inner class PhotoViewHolder(val binding: ItemPhotoBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemPhotoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photo = photoList[position]
        holder.binding.tvTitle.text = photo.title
        holder.binding.tvCategory.text = photo.category

        Glide.with(context)
            .load(photo.imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_dialog_alert)
            .into(holder.binding.ivThumbnail)

        holder.itemView.setOnClickListener { onItemClick(photo) }
    }

    override fun getItemCount(): Int = photoList.size

    fun updateList(newList: List<Photo>) {
        photoList = newList
        notifyDataSetChanged()
    }
}
