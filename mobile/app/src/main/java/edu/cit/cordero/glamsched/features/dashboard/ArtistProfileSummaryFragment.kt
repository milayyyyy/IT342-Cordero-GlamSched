package edu.cit.cordero.glamsched.features.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import androidx.fragment.app.Fragment
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class ArtistProfileSummaryFragment : Fragment() {

    private var artistId: Long = 0L
    private var artistName: String = "Artist"
    private var artistEmail: String = ""
    private var pendingImageTarget: String? = null

    companion object {
        fun newInstance(artistId: Long, name: String, email: String) =
            ArtistProfileSummaryFragment().apply {
                arguments = Bundle().apply {
                    putLong("ARTIST_ID", artistId)
                    putString("USER_NAME", name)
                    putString("USER_EMAIL", email)
                }
            }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_artist_profile_summary, container, false)

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            val target = pendingImageTarget ?: return@registerForActivityResult
            val context = context ?: return@registerForActivityResult
            if (uri == null) return@registerForActivityResult
            val input = context.contentResolver.openInputStream(uri) ?: return@registerForActivityResult
            val bmp = BitmapFactory.decodeStream(input)
            input.close()
            if (bmp == null) return@registerForActivityResult
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val dataUri = "data:image/jpeg;base64,$b64"

            if (target == "avatar") {
                RetrofitClient.glamApi.updateUserPhoto(artistId, mapOf("profileImage" to dataUri))
                    .enqueue(object : Callback<ApiResponse<UserDto>> {
                        override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                            view?.let { bindProfileImage(it, dataUri) }
                        }
                        override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                            if (isAdded) Toast.makeText(requireContext(), "Failed to update profile photo", Toast.LENGTH_SHORT).show()
                        }
                    })
            } else {
                RetrofitClient.glamApi.updateUserCover(artistId, mapOf("coverImage" to dataUri))
                    .enqueue(object : Callback<ApiResponse<UserDto>> {
                        override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                            view?.findViewById<ImageView>(R.id.ivArtistCover)?.let {
                                Glide.with(this@ArtistProfileSummaryFragment).load(dataUri).centerCrop().into(it)
                            }
                        }
                        override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                            if (isAdded) Toast.makeText(requireContext(), "Failed to update cover photo", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        artistId = arguments?.getLong("ARTIST_ID") ?: 0L
        artistName = arguments?.getString("USER_NAME") ?: "Artist"
        artistEmail = arguments?.getString("USER_EMAIL") ?: ""

        view.findViewById<TextView>(R.id.tvArtistInitial).text =
            artistName.firstOrNull()?.uppercase() ?: "?"
        view.findViewById<TextView>(R.id.tvArtistName).text = artistName
        view.findViewById<TextView>(R.id.tvArtistEmail).text = artistEmail

        view.findViewById<View>(R.id.btnArtistEditAvatar).setOnClickListener {
            pendingImageTarget = "avatar"
            pickImageLauncher.launch("image/*")
        }
        view.findViewById<View>(R.id.btnArtistEditCover).setOnClickListener {
            pendingImageTarget = "cover"
            pickImageLauncher.launch("image/*")
        }

        loadProfileMedia(view)
        loadStats(view)
    }

    private fun loadProfileMedia(view: View) {
        RetrofitClient.glamApi.getUserById(artistId).enqueue(object : Callback<ApiResponse<UserDto>> {
            override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                val user = response.body()?.data ?: return
                view.findViewById<TextView>(R.id.tvArtistName).text = user.name ?: artistName
                view.findViewById<TextView>(R.id.tvArtistEmail).text = user.email ?: artistEmail
                if (!user.coverImage.isNullOrBlank()) {
                    Glide.with(this@ArtistProfileSummaryFragment)
                        .load(user.coverImage)
                        .centerCrop()
                        .into(view.findViewById(R.id.ivArtistCover))
                }
                bindProfileImage(view, user.profileImage)
            }

            override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {}
        })
    }

    private fun bindProfileImage(view: View, image: String?) {
        val ivAvatar = view.findViewById<ImageView>(R.id.ivArtistAvatar)
        val tvInitial = view.findViewById<TextView>(R.id.tvArtistInitial)
        if (!image.isNullOrBlank()) {
            ivAvatar.visibility = View.VISIBLE
            tvInitial.visibility = View.GONE
            Glide.with(this).load(image).circleCrop().into(ivAvatar)
        } else {
            ivAvatar.visibility = View.GONE
            tvInitial.visibility = View.VISIBLE
        }
    }

    private fun loadStats(view: View) {
        val tvBookings = view.findViewById<TextView>(R.id.tvStatBookings)
        val tvCompleted = view.findViewById<TextView>(R.id.tvStatCompleted)
        val tvServices = view.findViewById<TextView>(R.id.tvStatServices)
        val tvEarnings = view.findViewById<TextView>(R.id.tvTotalEarnings)

        val priceMap = mutableMapOf<Long, Double>()
        RetrofitClient.glamApi.getServicesByArtist(artistId, artistId)
            .enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
                override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                    val services = response.body()?.data ?: emptyList()
                    services.forEach { svc -> priceMap[svc.id] = svc.price ?: 0.0 }
                    tvServices.text = services.size.toString()
                    fetchAppointments(tvBookings, tvCompleted, tvEarnings, priceMap)
                }
                override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                    fetchAppointments(tvBookings, tvCompleted, tvEarnings, priceMap)
                }
            })
    }

    private fun fetchAppointments(
        tvBookings: TextView,
        tvCompleted: TextView,
        tvEarnings: TextView,
        priceMap: Map<Long, Double>
    ) {
        RetrofitClient.glamApi.getArtistAppointments(artistId)
            .enqueue(object : Callback<ApiResponse<List<AppointmentDto>>> {
                override fun onResponse(call: Call<ApiResponse<List<AppointmentDto>>>, response: Response<ApiResponse<List<AppointmentDto>>>) {
                    val appts = response.body()?.data ?: emptyList()
                    val completed = appts.filter { it.status?.uppercase() == "COMPLETED" }
                    val earnings = completed.sumOf { it.serviceId?.let { id -> priceMap[id] } ?: 0.0 }
                    tvBookings.text = appts.size.toString()
                    tvCompleted.text = completed.size.toString()
                    tvEarnings.text = "₱${String.format("%,.2f", earnings)}"
                }
                override fun onFailure(call: Call<ApiResponse<List<AppointmentDto>>>, t: Throwable) {}
            })
    }
}
