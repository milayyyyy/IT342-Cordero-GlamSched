package edu.cit.cordero.glamsched.features.dashboard.adapter

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.dashboard.ServiceDto

class ServicePostAdapter(
    private var items: List<ServiceDto>,
    private val showLovedBanner: Boolean = false,
    private val onLoveClick: (ServiceDto) -> Unit = {},
    private val onBookClick: (ServiceDto) -> Unit = {},
    private val onFollowClick: (ServiceDto) -> Unit = {},
    private val onArtistClick: (ServiceDto) -> Unit = {},
    private val artistManageMode: Boolean = false,
    private val onEditClick: (ServiceDto) -> Unit = {},
    private val onDeleteClick: (ServiceDto) -> Unit = {}
) : RecyclerView.Adapter<ServicePostAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lovedBanner: View = itemView.findViewById(R.id.lovedBanner)
        val tvArtistInitial: TextView = itemView.findViewById(R.id.tvArtistInitial)
        val tvArtistName: TextView = itemView.findViewById(R.id.tvArtistName)
        val tvArtistCategory: TextView = itemView.findViewById(R.id.tvArtistCategory)
        val btnFollow: Button = itemView.findViewById(R.id.btnFollow)
        val vpPhotos: ViewPager2 = itemView.findViewById(R.id.vpPhotos)
        val llDots: LinearLayout = itemView.findViewById(R.id.llDots)
        val tvReactionCount: TextView = itemView.findViewById(R.id.tvReactionCount)
        val btnLove: Button = itemView.findViewById(R.id.btnLove)
        val tvServiceName: TextView = itemView.findViewById(R.id.tvServiceName)
        val tvServiceDesc: TextView = itemView.findViewById(R.id.tvServiceDesc)
        val tvServiceCategory: TextView = itemView.findViewById(R.id.tvServiceCategory)
        val tvServicePrice: TextView = itemView.findViewById(R.id.tvServicePrice)
        val btnBookNow: Button = itemView.findViewById(R.id.btnBookNow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_service_post, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.lovedBanner.visibility = if (showLovedBanner) View.VISIBLE else View.GONE

        val artistName = item.artistName ?: "Artist"
        holder.tvArtistInitial.text = artistName.firstOrNull()?.uppercase() ?: "?"
        holder.tvArtistName.text = artistName
        holder.tvArtistCategory.text = "${item.category ?: "Beauty"} Artist"

        // Artist click → open profile
        holder.tvArtistName.setOnClickListener { onArtistClick(item) }
        holder.tvArtistInitial.setOnClickListener { onArtistClick(item) }

        // Follow button
        holder.btnFollow.text = if (item.followedByMe) "Following" else "Follow"
        holder.btnFollow.setOnClickListener { onFollowClick(item) }

        // Photo carousel
        val photos = item.photos?.filter { it.isNotBlank() } ?: emptyList()
        if (photos.isNotEmpty()) {
            val pagerAdapter = PhotoPagerAdapter(photos)
            holder.vpPhotos.adapter = pagerAdapter
            holder.vpPhotos.visibility = View.VISIBLE
            setupDots(holder.llDots, photos.size, 0)
            holder.vpPhotos.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(pos: Int) {
                    setupDots(holder.llDots, photos.size, pos)
                }
            })
        } else {
            // Show placeholder via a single-item adapter with no URL
            val pagerAdapter = PhotoPagerAdapter(listOf(""))
            holder.vpPhotos.adapter = pagerAdapter
            holder.vpPhotos.visibility = View.VISIBLE
            holder.llDots.visibility = View.GONE
        }

        val n = item.reactionCount
        holder.tvReactionCount.text = when {
            n <= 0L -> "Be the first to ♥ this"
            n == 1L -> "❤  1 love"
            else -> "❤  $n loves"
        }
        holder.btnLove.text = if (item.likedByMe) "♥  Loved" else "♥  Love"

        holder.tvServiceName.text = item.name ?: ""
        holder.tvServiceDesc.text = item.description ?: ""
        holder.tvServiceCategory.text = item.category ?: "Beauty"
        holder.tvServicePrice.text = "₱${String.format("%.0f", item.price ?: 0.0)}"

        // Tap description or service name → show details dialog
        val openDetails = View.OnClickListener {
            if (!item.description.isNullOrBlank()) {
                showDetailsDialog(holder.itemView.context, item)
            }
        }
        holder.tvServiceDesc.setOnClickListener(openDetails)
        holder.tvServiceName.setOnClickListener(openDetails)

        if (artistManageMode) {
            holder.btnFollow.visibility = View.GONE
            holder.btnLove.text = "✎  Edit"
            holder.btnBookNow.text = "🗑  Delete"
            holder.btnLove.setOnClickListener { onEditClick(item) }
            holder.btnBookNow.setOnClickListener { onDeleteClick(item) }
        } else {
            holder.btnFollow.visibility = View.VISIBLE
            holder.btnLove.setOnClickListener { onLoveClick(item) }
            holder.btnBookNow.setOnClickListener { onBookClick(item) }
        }
    }

    private fun showDetailsDialog(context: Context, item: ServiceDto) {
        val dialog = Dialog(context)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_service_details, null)
        dialog.setContentView(view)
        dialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            val screenWidth = context.resources.displayMetrics.widthPixels
            val horizontalInsetPx = (20 * context.resources.displayMetrics.density).toInt()
            val targetWidth = (screenWidth - (horizontalInsetPx * 2)).coerceAtLeast(0)
            setLayout(
                targetWidth,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setGravity(Gravity.CENTER)
        }

        view.findViewById<TextView>(R.id.tvDialogServiceName).text =
            item.name ?: "Service"
        view.findViewById<TextView>(R.id.tvDialogCategory).text =
            (item.category ?: "Beauty").uppercase()
        view.findViewById<TextView>(R.id.tvDialogArtist).text =
            "by ${item.artistName ?: "Artist"}"
        view.findViewById<TextView>(R.id.tvDialogDescription).text =
            item.description ?: ""
        view.findViewById<TextView>(R.id.tvDialogPrice).text =
            "₱${String.format("%.0f", item.price ?: 0.0)}"
        view.findViewById<TextView>(R.id.tvDialogLoves).text =
            item.reactionCount.toString()

        val btnBook = view.findViewById<MaterialButton>(R.id.btnDialogBook)
        btnBook.setOnClickListener {
            dialog.dismiss()
            onBookClick(item)
        }

        view.findViewById<ImageButton>(R.id.btnDialogClose).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun setupDots(llDots: LinearLayout, count: Int, active: Int) {
        if (count <= 1) { llDots.visibility = View.GONE; return }
        llDots.visibility = View.VISIBLE
        llDots.removeAllViews()
        val ctx = llDots.context
        for (i in 0 until count) {
            val dot = ImageView(ctx)
            val size = if (i == active) 8 else 6
            val px = (size * ctx.resources.displayMetrics.density).toInt()
            val params = LinearLayout.LayoutParams(px, px).apply { setMargins(4, 0, 4, 0) }
            dot.layoutParams = params
            dot.setImageResource(android.R.drawable.presence_online)
            dot.setColorFilter(
                ContextCompat.getColor(ctx, if (i == active) R.color.gold else R.color.white)
            )
            dot.alpha = if (i == active) 1f else 0.6f
            llDots.addView(dot)
        }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<ServiceDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}

