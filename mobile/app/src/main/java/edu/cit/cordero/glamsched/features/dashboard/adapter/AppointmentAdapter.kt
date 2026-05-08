package edu.cit.cordero.glamsched.features.dashboard.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.dashboard.AppointmentDto
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentAdapter(
    private var items: List<AppointmentDto>,
    private val artistView: Boolean = false,
    private val onCardClick: (AppointmentDto) -> Unit = {},
    private val onPrimaryAction: (AppointmentDto) -> Unit = {}
) : RecyclerView.Adapter<AppointmentAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView as MaterialCardView
        val accent: View = itemView.findViewById(R.id.vApptAccent)
        val tvServiceName: TextView = itemView.findViewById(R.id.tvApptServiceName)
        val tvStatus: TextView = itemView.findViewById(R.id.tvApptStatus)
        val tvArtistInitial: TextView = itemView.findViewById(R.id.tvApptArtistInitial)
        val tvArtistName: TextView = itemView.findViewById(R.id.tvApptArtistName)
        val tvPrice: TextView = itemView.findViewById(R.id.tvApptPrice)
        val tvDate: TextView = itemView.findViewById(R.id.tvApptDate)
        val tvTime: TextView = itemView.findViewById(R.id.tvApptTime)
        val llActions: LinearLayout = itemView.findViewById(R.id.llApptActions)
        val btnPrimary: MaterialButton = itemView.findViewById(R.id.btnApptPrimary)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvServiceName.text = item.serviceName ?: "Service"

        val status = item.status?.uppercase() ?: "PENDING"
        holder.tvStatus.text = status.lowercase().replaceFirstChar { it.uppercase() }
        applyStatusStyle(holder, status)

        val displayName = if (artistView) (item.clientName ?: "Client") else (item.artistName ?: "Artist")
        holder.tvArtistInitial.text = displayName.firstOrNull()?.uppercase() ?: "?"
        holder.tvArtistName.text = if (artistView) "Booked by $displayName" else "with $displayName"

        val price = item.price
        if (price != null && price > 0.0) {
            holder.tvPrice.visibility = View.VISIBLE
            holder.tvPrice.text = "₱${String.format("%.0f", price)}"
        } else {
            holder.tvPrice.visibility = View.GONE
        }

        holder.tvDate.text = formatDate(item.date)
        holder.tvTime.text = formatTime12hr(item.time)

        configureActions(holder, item, status)

        holder.card.setOnClickListener { onCardClick(item) }
    }

    private fun applyStatusStyle(holder: ViewHolder, status: String) {
        when (status) {
            "CONFIRMED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#2E7D32"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed)
                holder.accent.setBackgroundColor(Color.parseColor("#2E7D32"))
            }
            "PENDING" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#E65100"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                holder.accent.setBackgroundColor(Color.parseColor("#E65100"))
            }
            "CANCELLED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#C62828"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                holder.accent.setBackgroundColor(Color.parseColor("#C62828"))
            }
            "COMPLETED" -> {
                holder.tvStatus.setTextColor(Color.parseColor("#1565C0"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                holder.accent.setBackgroundColor(Color.parseColor("#1565C0"))
            }
            else -> {
                holder.tvStatus.setTextColor(Color.parseColor("#1565C0"))
                holder.tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                holder.accent.setBackgroundColor(holder.itemView.resources.getColor(R.color.garnet, null))
            }
        }
    }

    private fun configureActions(holder: ViewHolder, item: AppointmentDto, status: String) {
        if (artistView && status == "CONFIRMED") {
            holder.llActions.visibility = View.VISIBLE
            holder.btnPrimary.text = "✓  Mark Completed"
            holder.btnPrimary.setBackgroundColor(holder.itemView.resources.getColor(R.color.garnet, null))
            holder.btnPrimary.setOnClickListener { onPrimaryAction(item) }
        } else if (artistView && status == "PENDING") {
            holder.llActions.visibility = View.VISIBLE
            holder.btnPrimary.text = "Confirm Booking"
            holder.btnPrimary.setBackgroundColor(holder.itemView.resources.getColor(R.color.garnet, null))
            holder.btnPrimary.setOnClickListener { onPrimaryAction(item) }
        } else if (!artistView && (status == "PENDING" || status == "CONFIRMED")) {
            holder.llActions.visibility = View.VISIBLE
            holder.btnPrimary.text = "Cancel"
            holder.btnPrimary.setBackgroundColor(Color.parseColor("#C62828"))
            holder.btnPrimary.setOnClickListener { onPrimaryAction(item) }
        } else {
            holder.llActions.visibility = View.GONE
        }
    }

    private fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        return try {
            val src = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dst = SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault())
            dst.format(src.parse(raw)!!)
        } catch (_: Exception) { raw }
    }

    private fun formatTime12hr(raw: String?): String {
        if (raw.isNullOrBlank()) return "—"
        return try {
            val core = if (raw.length >= 5) raw.substring(0, 5) else raw
            val src = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dst = SimpleDateFormat("h:mm a", Locale.getDefault())
            dst.format(src.parse(core)!!)
        } catch (_: Exception) { raw }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<AppointmentDto>) {
        items = newItems
        notifyDataSetChanged()
    }
}
