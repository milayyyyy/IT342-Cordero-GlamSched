package edu.cit.cordero.glamsched.features.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.dashboard.adapter.AppointmentAdapter
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class ArtistBookingsFragment : Fragment() {

    private var artistId: Long = 0L
    private lateinit var adapter: AppointmentAdapter
    private var allAppointments = listOf<AppointmentDto>()
    private var selectedStatus = "PENDING"
    private val priceMap = mutableMapOf<Long, Double>()

    companion object {
        fun newInstance(artistId: Long) = ArtistBookingsFragment().apply {
            arguments = Bundle().apply { putLong("ARTIST_ID", artistId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_artist_bookings, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        artistId = arguments?.getLong("ARTIST_ID") ?: 0L

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvArtistBookings)
        val tvEmpty = view.findViewById<TextView>(R.id.tvArtistBookingsEmpty)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutArtistBookings)

        adapter = AppointmentAdapter(
            items = emptyList(),
            artistView = true,
            onCardClick = { appt -> showDetailsDialog(appt) },
            onPrimaryAction = { appt -> handlePrimary(appt) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                selectedStatus = when (tab.position) {
                    0 -> "PENDING"
                    1 -> "CONFIRMED"
                    2 -> "CANCELLED"
                    else -> "COMPLETED"
                }
                applyFilter(view)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadPriceMapThenAppointments(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadAppointments(it) }
    }

    private fun applyFilter(root: View) {
        val tvEmpty = root.findViewById<TextView>(R.id.tvArtistBookingsEmpty)
        val tvCount = root.findViewById<TextView>(R.id.tvArtistBookingsCount)
        val filtered = allAppointments.filter { appt ->
            val status = appt.status?.uppercase() ?: ""
            status == selectedStatus
        }
        adapter.updateItems(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        tvCount.text = filtered.size.toString()
    }

    private fun loadPriceMapThenAppointments(root: View) {
        RetrofitClient.glamApi.getServices(artistId).enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                response.body()?.data?.forEach { svc -> priceMap[svc.id] = svc.price ?: 0.0 }
                loadAppointments(root)
            }
            override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                loadAppointments(root)
            }
        })
    }

    private fun loadAppointments(root: View) {
        RetrofitClient.glamApi.getArtistAppointments(artistId).enqueue(object : Callback<ApiResponse<List<AppointmentDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<AppointmentDto>>>, response: Response<ApiResponse<List<AppointmentDto>>>) {
                val raw = response.body()?.data ?: emptyList()
                allAppointments = raw.map { appt ->
                    appt.copy(price = appt.serviceId?.let { priceMap[it] } ?: appt.price)
                }
                applyFilter(root)
            }
            override fun onFailure(call: Call<ApiResponse<List<AppointmentDto>>>, t: Throwable) {
                if (isAdded) Toast.makeText(requireContext(), "Failed to load bookings", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun handlePrimary(appt: AppointmentDto) {
        if (!isAdded) return
        val status = appt.status?.uppercase() ?: ""
        when (status) {
            "PENDING" -> showConfirmDialog(appt, "Confirm this booking?", "CONFIRMED",
                "Booking confirmed!")
            "CONFIRMED" -> showConfirmDialog(appt, "Mark this booking as completed?", "COMPLETED",
                "Marked as completed!")
            else -> {}
        }
    }

    private fun showConfirmDialog(appt: AppointmentDto, title: String, newStatus: String, successMsg: String) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage("${appt.serviceName ?: "Service"} for ${appt.clientName ?: "client"}")
            .setPositiveButton("Yes") { _, _ -> updateStatus(appt.id, newStatus, successMsg) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateStatus(id: Long, status: String, successMsg: String) {
        RetrofitClient.glamApi.updateAppointmentStatus(id, status).enqueue(object : Callback<ApiResponse<AppointmentDto>> {
            override fun onResponse(call: Call<ApiResponse<AppointmentDto>>, response: Response<ApiResponse<AppointmentDto>>) {
                if (!isAdded) return
                Toast.makeText(requireContext(), successMsg, Toast.LENGTH_SHORT).show()
                view?.let { loadAppointments(it) }
            }
            override fun onFailure(call: Call<ApiResponse<AppointmentDto>>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Could not update", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDetailsDialog(appt: AppointmentDto) {
        if (!isAdded) return
        val msg = buildString {
            append("Service: ").append(appt.serviceName ?: "—").append("\n")
            append("Client: ").append(appt.clientName ?: "—").append("\n")
            append("Date: ").append(formatDate(appt.date)).append("\n")
            append("Time: ").append(formatTime12hr(appt.time)).append("\n")
            val price = appt.price
            if (price != null && price > 0.0) append("Price: ₱").append(String.format("%.2f", price)).append("\n")
            append("Status: ").append((appt.status ?: "").lowercase().replaceFirstChar { it.uppercase() }).append("\n")
            if (!appt.notes.isNullOrBlank()) append("Notes: ").append(appt.notes)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Booking Details")
            .setMessage(msg)
            .setPositiveButton("Close", null)
            .show()
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
}
