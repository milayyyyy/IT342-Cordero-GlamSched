package edu.cit.cordero.glamsched.features.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.cit.cordero.glamsched.R

class PhotoPagerAdapter(private val photos: List<String>) :
    RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(val imageView: ImageView) : RecyclerView.ViewHolder(imageView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val iv = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_photo_page, parent, false) as ImageView
        return PhotoViewHolder(iv)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        Glide.with(holder.imageView.context)
            .load(photos[position])
            .centerCrop()
            .placeholder(R.drawable.bg_post_image)
            .error(R.drawable.bg_post_image)
            .into(holder.imageView)
    }

    override fun getItemCount() = photos.size
}
