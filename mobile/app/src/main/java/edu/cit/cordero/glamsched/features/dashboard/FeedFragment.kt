package edu.cit.cordero.glamsched.features.dashboard

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.booking.ArtistProfileActivity
import edu.cit.cordero.glamsched.features.booking.BookingActivity
import edu.cit.cordero.glamsched.features.dashboard.adapter.ServicePostAdapter
import edu.cit.cordero.glamsched.features.user.UserProfileActivity
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import edu.cit.cordero.glamsched.shared.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FeedFragment : Fragment() {

    private var userId: Long = 0L
    private lateinit var adapter: ServicePostAdapter
    private var allServices = listOf<ServiceDto>()
    private var selectedCategory = "All"

    companion object {
        fun newInstance(userId: Long) = FeedFragment().apply {
            arguments = Bundle().apply { putLong("USER_ID", userId) }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_feed, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        userId = arguments?.getLong("USER_ID") ?: 0L

        val recyclerView = view.findViewById<RecyclerView>(R.id.rvFeed)
        val tvEmpty = view.findViewById<View>(R.id.tvFeedEmpty)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)

        adapter = ServicePostAdapter(
            items = emptyList(),
            onLoveClick = { service -> toggleLove(service, tvEmpty) },
            onBookClick = { service ->
                val intent = Intent(requireContext(), BookingActivity::class.java).apply {
                    putExtra("SERVICE_ID", service.id)
                    putExtra("ARTIST_ID", service.artistId ?: 0L)
                    putExtra("SERVICE_NAME", service.name ?: "")
                    putExtra("ARTIST_NAME", service.artistName ?: "")
                    putExtra("SERVICE_PRICE", service.price ?: 0.0)
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
            },
            onFollowClick = { service -> toggleFollow(service, tvEmpty) },
            onArtistClick = { service ->
                val intent = Intent(requireContext(), ArtistProfileActivity::class.java).apply {
                    putExtra("ARTIST_ID", service.artistId ?: 0L)
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
            }
        )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) = applyFilter(s.toString(), selectedCategory, tvEmpty)
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        setupCategoryChips(view, tvEmpty)
        setupWelcomeHeader(view)
        loadServices(tvEmpty)
    }

    override fun onResume() {
        super.onResume()
        view?.let { setupWelcomeHeader(it) }
        view?.let { refreshAvatar(it) }
    }

    private fun setupWelcomeHeader(view: View) {
        val name = SessionManager.getUserName(requireContext()).ifBlank { "Glam User" }
        val firstName = name.substringBefore(' ').ifBlank { name }
        view.findViewById<TextView>(R.id.tvFeedWelcome)?.text = "Welcome back, $firstName"

        val avatarBtn = view.findViewById<View>(R.id.btnFeedAvatar)
        avatarBtn?.setOnClickListener {
            startActivity(Intent(requireContext(), UserProfileActivity::class.java))
        }

        view.findViewById<View>(R.id.btnFeedNotifications)?.setOnClickListener {
            Toast.makeText(requireContext(), "No new notifications", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<TextView>(R.id.tvFeedAvatarInitial)?.text = name.first().uppercase()
        refreshAvatar(view)
    }

    private fun refreshAvatar(view: View) {
        val ivAvatar = view.findViewById<ImageView>(R.id.ivFeedAvatar) ?: return
        val tvInitial = view.findViewById<TextView>(R.id.tvFeedAvatarInitial)
        val uid = SessionManager.getUserId(requireContext())
        if (uid == 0L) return
        RetrofitClient.glamApi.getUserById(uid).enqueue(object : Callback<ApiResponse<UserDto>> {
            override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                val data = response.body()?.data ?: return
                if (!isAdded) return
                if (!data.profileImage.isNullOrBlank()) {
                    ivAvatar.visibility = View.VISIBLE
                    tvInitial?.visibility = View.GONE
                    Glide.with(this@FeedFragment).load(data.profileImage).circleCrop().into(ivAvatar)
                } else {
                    ivAvatar.visibility = View.GONE
                    tvInitial?.visibility = View.VISIBLE
                }
            }
            override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {}
        })
    }

    private fun setupCategoryChips(view: View, tvEmpty: View) {
        val categories = listOf("All", "Hair", "Makeup", "Nails", "Skincare", "Lashes", "Brows", "Waxing", "Massage", "Other")
        val chipIds = listOf(
            R.id.chipAll, R.id.chipHair, R.id.chipMakeup, R.id.chipNails, R.id.chipSkincare,
            R.id.chipLashes, R.id.chipBrows, R.id.chipWaxing, R.id.chipMassage, R.id.chipOther
        )
        chipIds.zip(categories).forEach { (id, cat) ->
            view.findViewById<TextView>(id)?.setOnClickListener {
                selectedCategory = cat
                applyFilter(view.findViewById<EditText>(R.id.etSearch).text.toString(), cat, tvEmpty)
                updateChipStates(view, id)
            }
        }
    }

    private fun updateChipStates(view: View, selectedId: Int) {
        val chipIds = listOf(
            R.id.chipAll, R.id.chipHair, R.id.chipMakeup, R.id.chipNails, R.id.chipSkincare,
            R.id.chipLashes, R.id.chipBrows, R.id.chipWaxing, R.id.chipMassage, R.id.chipOther
        )
        chipIds.forEach { id ->
            view.findViewById<TextView>(id)?.apply {
                if (id == selectedId) {
                    setBackgroundResource(R.drawable.bg_chip_pill_selected)
                    setTextColor(resources.getColor(R.color.white, null))
                    setTypeface(null, Typeface.BOLD)
                } else {
                    setBackgroundResource(R.drawable.bg_chip_pill_unselected)
                    setTextColor(resources.getColor(R.color.ink_black, null))
                    setTypeface(null, Typeface.NORMAL)
                }
            }
        }
    }

    private fun applyFilter(query: String, category: String, tvEmpty: View) {
        val filtered = allServices.filter { service ->
            val matchesQuery = query.isBlank() ||
                service.name?.contains(query, ignoreCase = true) == true ||
                service.artistName?.contains(query, ignoreCase = true) == true ||
                service.description?.contains(query, ignoreCase = true) == true
            val matchesCategory = category == "All" || service.category.equals(category, ignoreCase = true)
            matchesQuery && matchesCategory
        }
        adapter.updateItems(filtered)
        tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun loadServices(tvEmpty: View) {
        RetrofitClient.glamApi.getServices(userId).enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                val data = response.body()?.data ?: emptyList()
                allServices = data
                adapter.updateItems(data)
                tvEmpty.visibility = if (data.isEmpty()) View.VISIBLE else View.GONE
            }
            override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                if (isAdded) Toast.makeText(requireContext(), "Failed to load feed", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun toggleLove(service: ServiceDto, tvEmpty: View) {
        RetrofitClient.glamApi.toggleReaction(service.id, userId).enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
            override fun onResponse(call: Call<ApiResponse<Map<String, Any>>>, response: Response<ApiResponse<Map<String, Any>>>) {
                loadServices(tvEmpty)
            }
            override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {}
        })
    }

    private fun toggleFollow(service: ServiceDto, tvEmpty: View) {
        val artistId = service.artistId ?: return
        RetrofitClient.glamApi.toggleFollow(artistId, userId).enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
            override fun onResponse(call: Call<ApiResponse<Map<String, Any>>>, response: Response<ApiResponse<Map<String, Any>>>) {
                val followed = response.body()?.data?.get("followedByMe") as? Boolean ?: false
                val msg = if (followed) "Following ${service.artistName}" else "Unfollowed ${service.artistName}"
                if (isAdded) Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
                loadServices(tvEmpty)
            }
            override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {}
        })
    }
}

