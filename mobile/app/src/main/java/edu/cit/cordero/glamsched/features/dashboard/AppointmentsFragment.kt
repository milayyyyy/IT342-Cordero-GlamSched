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

class AppointmentsFragment : Fragment() {

    private var userId: Long = 0L
    private lateinit var adapter: AppointmentAdapter
    private var allAppointments = listOf<AppointmentDto>()
    private var showUpcoming = true
    private val priceMap = mutableMapOf<Long, Double>()

    companion object {
        fun newInstance(userId: Long) = AppointmentsFragment().apply {
            arguments = Bundle().apply { putLong("USER_ID", userId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_appointments, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = arguments?.getLong("USER_ID") ?: 0L

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvAppointments)
        val emptyView = view.findViewById<View>(R.id.tvAppointmentsEmpty)
        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayoutAppointments)

        adapter = AppointmentAdapter(
            items = emptyList(),
            artistView = false,
            onCardClick = { appt -> showDetailsDialog(appt) },
            onPrimaryAction = { appt -> confirmCancel(appt) }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                showUpcoming = tab.position == 0
                applyFilter(emptyView)
            }
            override fun onTabUnselected(tab: TabLayout.Tab) {}
            override fun onTabReselected(tab: TabLayout.Tab) {}
        })

        loadPriceMapThenAppointments(emptyView)
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadAppointments(it.findViewById(R.id.tvAppointmentsEmpty)) }
    }

    private fun applyFilter(emptyView: View) {
        val filtered = allAppointments.filter { appt ->
            val status = appt.status?.uppercase() ?: ""
            if (showUpcoming) status == "PENDING" || status == "CONFIRMED"
            else status == "COMPLETED" || status == "CANCELLED"
        }
        adapter.updateItems(filtered)
        emptyView.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
        updateStats()
    }

    private fun updateStats() {
        val root = view ?: return
        val upcoming = allAppointments.count { (it.status?.uppercase() ?: "").let { s -> s == "PENDING" || s == "CONFIRMED" } }
        val completed = allAppointments.count { it.status?.uppercase() == "COMPLETED" }
        val total = allAppointments.size
        root.findViewById<TextView>(R.id.tvStatUpcoming).text = upcoming.toString()
        root.findViewById<TextView>(R.id.tvStatCompleted).text = completed.toString()
        root.findViewById<TextView>(R.id.tvStatTotal).text = total.toString()
    }

    private fun loadPriceMapThenAppointments(emptyView: View) {
        RetrofitClient.glamApi.getServices(userId).enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                response.body()?.data?.forEach { svc -> priceMap[svc.id] = svc.price ?: 0.0 }
                loadAppointments(emptyView)
            }
            override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                loadAppointments(emptyView)
            }
        })
    }

    private fun loadAppointments(emptyView: View) {
        RetrofitClient.glamApi.getAppointments(userId).enqueue(object : Callback<ApiResponse<List<AppointmentDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<AppointmentDto>>>, response: Response<ApiResponse<List<AppointmentDto>>>) {
                val raw = response.body()?.data ?: emptyList()
                allAppointments = raw.map { appt ->
                    appt.copy(price = appt.serviceId?.let { priceMap[it] } ?: appt.price)
                }
                applyFilter(emptyView)
            }
            override fun onFailure(call: Call<ApiResponse<List<AppointmentDto>>>, t: Throwable) {
                if (isAdded) Toast.makeText(requireContext(), "Failed to load appointments", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showDetailsDialog(appt: AppointmentDto) {
        if (!isAdded) return
        val message = buildString {
            append("Service: ").append(appt.serviceName ?: "—").append("\n")
            append("Artist: ").append(appt.artistName ?: "—").append("\n")
            append("Date: ").append(formatDate(appt.date)).append("\n")
            append("Time: ").append(formatTime12hr(appt.time)).append("\n")
            val price = appt.price
            if (price != null && price > 0.0) append("Price: ₱").append(String.format("%.2f", price)).append("\n")
            append("Status: ").append((appt.status ?: "").lowercase().replaceFirstChar { it.uppercase() }).append("\n")
            if (!appt.notes.isNullOrBlank()) append("Notes: ").append(appt.notes)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Appointment Details")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    private fun confirmCancel(appt: AppointmentDto) {
        if (!isAdded) return
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Appointment?")
            .setMessage("This will cancel \"${appt.serviceName ?: "this booking"}\". You can rebook later.")
            .setPositiveButton("Yes, Cancel") { _, _ -> updateStatus(appt.id, "CANCELLED") }
            .setNegativeButton("Keep It", null)
            .show()
    }

    private fun updateStatus(id: Long, status: String) {
        RetrofitClient.glamApi.updateAppointmentStatus(id, status).enqueue(object : Callback<ApiResponse<AppointmentDto>> {
            override fun onResponse(call: Call<ApiResponse<AppointmentDto>>, response: Response<ApiResponse<AppointmentDto>>) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Booking cancelled", Toast.LENGTH_SHORT).show()
                view?.let { loadAppointments(it.findViewById(R.id.tvAppointmentsEmpty)) }
            }
            override fun onFailure(call: Call<ApiResponse<AppointmentDto>>, t: Throwable) {
                if (!isAdded) return
                Toast.makeText(requireContext(), "Could not update. Try again.", Toast.LENGTH_SHORT).show()
            }
        })
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
