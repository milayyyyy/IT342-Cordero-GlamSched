package edu.cit.cordero.glamsched.features.dashboard

import android.content.Intent
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.user.UserProfileActivity
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale

class ArtistHomeFragment : Fragment() {
    private var artistId: Long = 0L
    private var artistName: String = "Artist"
    private var artistEmail: String = ""

    companion object {
        fun newInstance(artistId: Long, name: String, email: String) =
            ArtistHomeFragment().apply {
                arguments = Bundle().apply {
                    putLong("ARTIST_ID", artistId)
                    putString("USER_NAME", name)
                    putString("USER_EMAIL", email)
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_artist_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        artistId = arguments?.getLong("ARTIST_ID") ?: 0L
        artistName = arguments?.getString("USER_NAME") ?: "Artist"
        artistEmail = arguments?.getString("USER_EMAIL") ?: ""

        val firstName = artistName.substringBefore(' ').ifBlank { artistName }
        view.findViewById<TextView>(R.id.tvArtistHomeWelcome).text = "Welcome back, $firstName"
        view.findViewById<TextView>(R.id.tvArtistHomeInitial).text = artistName.firstOrNull()?.uppercase() ?: "A"
        view.findViewById<View>(R.id.btnArtistHomeAvatar).setOnClickListener {
            startActivity(Intent(requireContext(), UserProfileActivity::class.java))
        }
        view.findViewById<View>(R.id.btnArtistHomeNotifications).setOnClickListener {
            Toast.makeText(requireContext(), "No new notifications", Toast.LENGTH_SHORT).show()
        }
        loadAvatar(view)
        loadStats(view)
    }

    private fun loadAvatar(view: View) {
        RetrofitClient.glamApi.getUserById(artistId).enqueue(object : Callback<ApiResponse<UserDto>> {
            override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                val user = response.body()?.data ?: return
                val iv = view.findViewById<ImageView>(R.id.ivArtistHomeAvatar)
                val tv = view.findViewById<TextView>(R.id.tvArtistHomeInitial)
                if (!user.profileImage.isNullOrBlank()) {
                    iv.visibility = View.VISIBLE
                    tv.visibility = View.GONE
                    Glide.with(this@ArtistHomeFragment).load(user.profileImage).circleCrop().into(iv)
                }
            }
            override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {}
        })
    }

    private fun loadStats(view: View) {
        val priceMap = mutableMapOf<Long, Double>()
        RetrofitClient.glamApi.getServicesByArtist(artistId, artistId)
            .enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
                override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                    val services = response.body()?.data ?: emptyList()
                    services.forEach { s -> priceMap[s.id] = s.price ?: 0.0 }
                    view.findViewById<TextView>(R.id.tvHomeServices).text = services.size.toString()
                    loadAppointments(view, priceMap)
                }
                override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                    loadAppointments(view, priceMap)
                }
            })
    }

    private fun loadAppointments(view: View, priceMap: Map<Long, Double>) {
        RetrofitClient.glamApi.getArtistAppointments(artistId)
            .enqueue(object : Callback<ApiResponse<List<AppointmentDto>>> {
                override fun onResponse(call: Call<ApiResponse<List<AppointmentDto>>>, response: Response<ApiResponse<List<AppointmentDto>>>) {
                    val appts = response.body()?.data ?: emptyList()
                    val completed = appts.filter { it.status?.uppercase() == "COMPLETED" }
                    val earnings = completed.sumOf { a -> a.serviceId?.let { priceMap[it] } ?: 0.0 }
                    view.findViewById<TextView>(R.id.tvHomeBookings).text = appts.size.toString()
                    view.findViewById<TextView>(R.id.tvHomeCompleted).text = completed.size.toString()
                    view.findViewById<TextView>(R.id.tvHomeEarnings).text = "₱${String.format("%,.2f", earnings)}"
                    renderBookingHistory(view, appts, priceMap)
                }
                override fun onFailure(call: Call<ApiResponse<List<AppointmentDto>>>, t: Throwable) {
                    renderBookingHistory(view, emptyList(), priceMap)
                }
            })
    }

    private fun renderBookingHistory(root: View, appts: List<AppointmentDto>, priceMap: Map<Long, Double>) {
        val emptyView = root.findViewById<TextView>(R.id.tvArtistHomeHistoryEmpty)
        val container = root.findViewById<LinearLayout>(R.id.llArtistHomeHistoryItems)
        container.removeAllViews()

        val sorted = appts.sortedByDescending { "${it.date ?: ""} ${it.time ?: ""}" }.take(6)
        if (sorted.isEmpty()) {
            emptyView.visibility = View.VISIBLE
            return
        }
        emptyView.visibility = View.GONE

        sorted.forEachIndexed { index, appt ->
            val status = (appt.status ?: "PENDING").uppercase()
            val price = appt.price ?: appt.serviceId?.let { priceMap[it] } ?: 0.0
            val line1 = "${appt.serviceName ?: "Service"} • ${appt.clientName ?: "Client"}"
            val line2 = "${formatDate(appt.date)}  ${formatTime12hr(appt.time)}"
            val prettyStatus = status.lowercase().replaceFirstChar { it.uppercase() }

            val item = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.VERTICAL
                background = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 12f * resources.displayMetrics.density
                    setColor(resources.getColor(R.color.body_bg, null))
                    setStroke((1 * resources.displayMetrics.density).toInt().coerceAtLeast(1), resources.getColor(R.color.input_border, null))
                }
                setPadding(
                    (12 * resources.displayMetrics.density).toInt(),
                    (10 * resources.displayMetrics.density).toInt(),
                    (12 * resources.displayMetrics.density).toInt(),
                    (10 * resources.displayMetrics.density).toInt()
                )
                addView(TextView(requireContext()).apply {
                    text = line1
                    textSize = 14f
                    setTextColor(resources.getColor(R.color.ink_black, null))
                    setTypeface(typeface, Typeface.BOLD)
                })
                addView(TextView(requireContext()).apply {
                    text = line2
                    setPadding(0, (3 * resources.displayMetrics.density).toInt(), 0, 0)
                    textSize = 12f
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                })

                addView(LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = Gravity.CENTER_VERTICAL
                    setPadding(0, (8 * resources.displayMetrics.density).toInt(), 0, 0)

                    addView(TextView(requireContext()).apply {
                        text = prettyStatus
                        textSize = 11f
                        setTypeface(typeface, Typeface.BOLD)
                        val isCompleted = status == "COMPLETED"
                        setTextColor(resources.getColor(if (isCompleted) R.color.white else R.color.garnet, null))
                        background = GradientDrawable().apply {
                            shape = GradientDrawable.RECTANGLE
                            cornerRadius = 999f
                            setColor(resources.getColor(if (isCompleted) R.color.gold_dark else R.color.white, null))
                            setStroke((1 * resources.displayMetrics.density).toInt().coerceAtLeast(1), resources.getColor(R.color.garnet, null))
                        }
                        setPadding(
                            (10 * resources.displayMetrics.density).toInt(),
                            (4 * resources.displayMetrics.density).toInt(),
                            (10 * resources.displayMetrics.density).toInt(),
                            (4 * resources.displayMetrics.density).toInt()
                        )
                    })

                    addView(TextView(requireContext()).apply {
                        text = "  •  ₱${String.format("%,.2f", price)}"
                        textSize = 12f
                        setTextColor(resources.getColor(R.color.garnet, null))
                        setTypeface(typeface, Typeface.BOLD)
                    })
                })
            }
            container.addView(item, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                if (index > 0) topMargin = (8 * resources.displayMetrics.density).toInt()
            })

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
}
