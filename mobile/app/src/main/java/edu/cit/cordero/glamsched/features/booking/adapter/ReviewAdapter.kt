package edu.cit.cordero.glamsched.features.booking.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.booking.ReviewDto

class ReviewAdapter(
    private var items: List<ReviewDto>
) : RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvInitial: TextView = itemView.findViewById(R.id.tvReviewerInitial)
        val tvName: TextView = itemView.findViewById(R.id.tvReviewerName)
        val tvStars: TextView = itemView.findViewById(R.id.tvReviewStars)
        val tvDate: TextView = itemView.findViewById(R.id.tvReviewDate)
        val tvComment: TextView = itemView.findViewById(R.id.tvReviewComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        val name = item.clientName ?: "Client"
        holder.tvInitial.text = name.firstOrNull()?.uppercase() ?: "?"
        holder.tvName.text = name

        val rating = item.rating.coerceIn(1, 5)
        holder.tvStars.text = "★".repeat(rating) + "☆".repeat(5 - rating)

        holder.tvComment.text = item.comment ?: ""
        holder.tvComment.visibility = if (item.comment.isNullOrBlank()) View.GONE else View.VISIBLE

        // Show date portion only (trim time if ISO format)
        val dateStr = item.createdAt?.let {
            if (it.contains("T")) it.substringBefore("T") else it.take(10)
        } ?: ""
        holder.tvDate.text = dateStr
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ReviewDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
