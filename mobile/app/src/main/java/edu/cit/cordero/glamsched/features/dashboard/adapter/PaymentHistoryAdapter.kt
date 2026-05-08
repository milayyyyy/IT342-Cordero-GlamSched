package edu.cit.cordero.glamsched.features.dashboard.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.dashboard.AppointmentDto
import java.text.SimpleDateFormat
import java.util.Locale

class PaymentHistoryAdapter(
    private var items: List<AppointmentDto>
) : RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvService: TextView = itemView.findViewById(R.id.tvPaymentService)
        val tvArtist: TextView = itemView.findViewById(R.id.tvPaymentArtist)
        val tvDateTime: TextView = itemView.findViewById(R.id.tvPaymentDateTime)
        val tvMethod: TextView = itemView.findViewById(R.id.tvPaymentMethod)
        val tvAmount: TextView = itemView.findViewById(R.id.tvPaymentAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_payment_history, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvService.text = item.serviceName ?: "Service"
        holder.tvArtist.text = "with ${item.artistName ?: "Artist"}"
        holder.tvDateTime.text = "${formatDate(item.date)} • ${formatTime12hr(item.time)}"
        holder.tvMethod.text = (item.paymentMethod?.takeIf { it.isNotBlank() } ?: "Not specified")
        holder.tvAmount.text = item.price?.let { "₱${String.format("%.2f", it)}" } ?: "Amount pending"
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<AppointmentDto>) {
        items = newItems
        notifyDataSetChanged()
    }

    private fun formatDate(raw: String?): String {
        if (raw.isNullOrBlank()) return "Date TBD"
        return try {
            val src = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val dst = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            dst.format(src.parse(raw)!!)
        } catch (_: Exception) { raw }
    }

    private fun formatTime12hr(raw: String?): String {
        if (raw.isNullOrBlank()) return "Time TBD"
        return try {
            val core = if (raw.length >= 5) raw.substring(0, 5) else raw
            val src = SimpleDateFormat("HH:mm", Locale.getDefault())
            val dst = SimpleDateFormat("h:mm a", Locale.getDefault())
            dst.format(src.parse(core)!!)
        } catch (_: Exception) { raw }
    }
}
