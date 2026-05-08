package edu.cit.cordero.glamsched.features.booking

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.booking.adapter.ReviewAdapter
import edu.cit.cordero.glamsched.features.dashboard.ServiceDto
import edu.cit.cordero.glamsched.features.dashboard.UserDto
import edu.cit.cordero.glamsched.features.dashboard.adapter.ServicePostAdapter
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ArtistProfileActivity : AppCompatActivity() {

    private var artistId: Long = 0L
    private var userId: Long = 0L
    private var userDisplayName: String = "Client"
    private var followedByMe = false
    private var followerCount = 0L
    private lateinit var adapter: ServicePostAdapter
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artist_profile)

        artistId = intent.getLongExtra("ARTIST_ID", 0L)
        userId = intent.getLongExtra("USER_ID", 0L)
        userDisplayName = intent.getStringExtra("USER_NAME") ?: "Client"

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        // Posts RecyclerView
        val rvTimeline = findViewById<RecyclerView>(R.id.rvTimeline)
        val tvEmpty = findViewById<TextView>(R.id.tvEmpty)

        adapter = ServicePostAdapter(
            items = emptyList(),
            onBookClick = { service ->
                val intent = Intent(this, BookingActivity::class.java).apply {
                    putExtra("SERVICE_ID", service.id)
                    putExtra("ARTIST_ID", service.artistId ?: 0L)
                    putExtra("SERVICE_NAME", service.name ?: "")
                    putExtra("ARTIST_NAME", service.artistName ?: "")
                    putExtra("SERVICE_PRICE", service.price ?: 0.0)
                    putExtra("USER_ID", userId)
                }
                startActivity(intent)
            },
            onLoveClick = { service -> toggleLove(service) },
            onFollowClick = { _ -> toggleFollow() }
        )
        rvTimeline.layoutManager = LinearLayoutManager(this)
        rvTimeline.adapter = adapter
        rvTimeline.isNestedScrollingEnabled = false

        // Reviews RecyclerView
        val rvReviews = findViewById<RecyclerView>(R.id.rvReviews)
        reviewAdapter = ReviewAdapter(emptyList())
        rvReviews.layoutManager = LinearLayoutManager(this)
        rvReviews.adapter = reviewAdapter
        rvReviews.isNestedScrollingEnabled = false

        setupTabs()
        loadProfile()
        loadTimeline(tvEmpty)
    }

    private fun setupTabs() {
        val tabPosts = findViewById<TextView>(R.id.tabPosts)
        val tabReviews = findViewById<TextView>(R.id.tabReviews)
        val layoutPosts = findViewById<LinearLayout>(R.id.layoutPosts)
        val layoutReviews = findViewById<LinearLayout>(R.id.layoutReviews)
        val tabIndicator = findViewById<View>(R.id.tabIndicatorPosts)

        val garnet = resources.getColor(R.color.garnet, null)
        val secondary = resources.getColor(R.color.text_secondary, null)

        tabPosts.setOnClickListener {
            tabPosts.setTextColor(garnet)
            tabReviews.setTextColor(secondary)
            tabIndicator.setBackgroundColor(garnet)
            layoutPosts.visibility = View.VISIBLE
            layoutReviews.visibility = View.GONE
        }

        tabReviews.setOnClickListener {
            tabReviews.setTextColor(garnet)
            tabPosts.setTextColor(secondary)
            tabIndicator.setBackgroundColor(resources.getColor(android.R.color.transparent, null))
            layoutPosts.visibility = View.GONE
            layoutReviews.visibility = View.VISIBLE
            loadReviews()
        }
    }

    private fun loadReviews() {
        val progress = findViewById<ProgressBar>(R.id.progressReviews)
        val tvEmpty = findViewById<TextView>(R.id.tvEmptyReviews)
        progress.visibility = View.VISIBLE
        tvEmpty.visibility = View.GONE

        RetrofitClient.glamApi.getReviews(artistId)
            .enqueue(object : Callback<ApiResponse<List<ReviewDto>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<ReviewDto>>>,
                    response: Response<ApiResponse<List<ReviewDto>>>
                ) {
                    progress.visibility = View.GONE
                    val reviews = response.body()?.data ?: emptyList()
                    reviewAdapter.updateItems(reviews)
                    if (reviews.isEmpty()) {
                        tvEmpty.visibility = View.VISIBLE
                    }
                    // Show "Write a review" button only if user hasn't reviewed yet
                    val alreadyReviewed = reviews.any { it.clientId == userId }
                    if (!alreadyReviewed && userId != artistId) {
                        showWriteReviewButton()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<List<ReviewDto>>>, t: Throwable) {
                    progress.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                }
            })
    }

    private fun showWriteReviewButton() {
        val layoutReviews = findViewById<LinearLayout>(R.id.layoutReviews)
        // Add write-review button dynamically if not already added
        if (layoutReviews.findViewWithTag<View>("btn_write_review") != null) return

        val btn = com.google.android.material.button.MaterialButton(this).apply {
            text = "✏ Write a Review"
            tag = "btn_write_review"
            setBackgroundColor(resources.getColor(R.color.garnet, null))
            setTextColor(resources.getColor(R.color.white, null))
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = android.view.Gravity.CENTER_HORIZONTAL
                topMargin = 16
                bottomMargin = 16
            }
            layoutParams = params
            setOnClickListener { showReviewDialog() }
        }
        layoutReviews.addView(btn)
    }

    private fun showReviewDialog() {
        val dialogView = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val ratingBar = android.widget.RatingBar(this).apply {
            numStars = 5
            rating = 5f
            stepSize = 1f
            layoutParams = android.widget.LinearLayout.LayoutParams(
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        val etComment = android.widget.EditText(this).apply {
            hint = "Share your experience..."
            minLines = 3
            maxLines = 5
            inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_MULTI_LINE
        }

        dialogView.addView(android.widget.TextView(this).apply { text = "Rating" })
        dialogView.addView(ratingBar)
        dialogView.addView(android.widget.TextView(this).apply {
            text = "Comment"
            setPadding(0, 16, 0, 4)
        })
        dialogView.addView(etComment)

        android.app.AlertDialog.Builder(this)
            .setTitle("Write a Review")
            .setView(dialogView)
            .setPositiveButton("Submit") { _, _ ->
                val rating = ratingBar.rating.toInt().coerceIn(1, 5)
                val comment = etComment.text.toString().trim()
                if (comment.isBlank()) {
                    Toast.makeText(this, "Please write a comment", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                submitReview(rating, comment)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun submitReview(rating: Int, comment: String) {
        val review = ReviewDto(
            artistId = artistId,
            clientId = userId,
            clientName = userDisplayName,
            rating = rating,
            comment = comment
        )
        RetrofitClient.glamApi.addReview(review)
            .enqueue(object : Callback<ApiResponse<ReviewDto>> {
                override fun onResponse(
                    call: Call<ApiResponse<ReviewDto>>,
                    response: Response<ApiResponse<ReviewDto>>
                ) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@ArtistProfileActivity, "Review submitted!", Toast.LENGTH_SHORT).show()
                        loadReviews()
                    } else {
                        Toast.makeText(this@ArtistProfileActivity, "Failed to submit review", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<ReviewDto>>, t: Throwable) {
                    Toast.makeText(this@ArtistProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun loadProfile() {
        val tvName = findViewById<TextView>(R.id.tvArtistName)
        val tvInitial = findViewById<TextView>(R.id.tvArtistInitial)
        val tvSpecialty = findViewById<TextView>(R.id.tvArtistSpecialty)
        val tvFollowers = findViewById<TextView>(R.id.tvFollowerCount)
        val btnFollow = findViewById<MaterialButton>(R.id.btnFollow)

        RetrofitClient.glamApi.getArtistProfile(artistId, userId)
            .enqueue(object : Callback<ApiResponse<UserDto>> {
                override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                    val artist = response.body()?.data ?: return
                    tvName.text = artist.name ?: "Artist"
                    tvInitial.text = artist.name?.firstOrNull()?.uppercase() ?: "?"
                    tvSpecialty.text = "Beauty Artist"
                    followerCount = artist.followerCount
                    tvFollowers.text = followerCount.toString()
                    followedByMe = artist.followedByMe
                    updateFollowButton(btnFollow)
                }
                override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {}
            })

        btnFollow.setOnClickListener { toggleFollow() }
    }

    private fun loadTimeline(tvEmpty: TextView) {
        val progress = findViewById<ProgressBar>(R.id.progressTimeline)
        val tvServiceCount = findViewById<TextView>(R.id.tvServiceCount)
        progress.visibility = View.VISIBLE

        RetrofitClient.glamApi.getServicesByArtist(artistId, userId)
            .enqueue(object : Callback<ApiResponse<List<ServiceDto>>> {
                override fun onResponse(call: Call<ApiResponse<List<ServiceDto>>>, response: Response<ApiResponse<List<ServiceDto>>>) {
                    progress.visibility = View.GONE
                    val services = response.body()?.data ?: emptyList()
                    tvServiceCount.text = services.size.toString()
                    adapter.updateItems(services)
                    tvEmpty.visibility = if (services.isEmpty()) View.VISIBLE else View.GONE
                }
                override fun onFailure(call: Call<ApiResponse<List<ServiceDto>>>, t: Throwable) {
                    progress.visibility = View.GONE
                    tvEmpty.visibility = View.VISIBLE
                }
            })
    }

    private fun toggleFollow() {
        RetrofitClient.glamApi.toggleFollow(artistId, userId)
            .enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
                override fun onResponse(call: Call<ApiResponse<Map<String, Any>>>, response: Response<ApiResponse<Map<String, Any>>>) {
                    val result = response.body()?.data ?: return
                    followedByMe = result["followedByMe"] as? Boolean ?: !followedByMe
                    followerCount = (result["followerCount"] as? Double)?.toLong() ?: followerCount
                    runOnUiThread {
                        findViewById<TextView>(R.id.tvFollowerCount).text = followerCount.toString()
                        val btn = findViewById<MaterialButton>(R.id.btnFollow)
                        updateFollowButton(btn)
                        val name = findViewById<TextView>(R.id.tvArtistName).text
                        Toast.makeText(this@ArtistProfileActivity,
                            if (followedByMe) "Following $name" else "Unfollowed $name",
                            Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {}
            })
    }

    private fun toggleLove(service: ServiceDto) {
        RetrofitClient.glamApi.toggleReaction(service.id, userId)
            .enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
                override fun onResponse(call: Call<ApiResponse<Map<String, Any>>>, response: Response<ApiResponse<Map<String, Any>>>) {}
                override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {}
            })
    }

    private fun updateFollowButton(btn: MaterialButton) {
        if (followedByMe) {
            btn.text = "✓ Following"
            btn.setBackgroundColor(resources.getColor(R.color.input_border, null))
            btn.setTextColor(resources.getColor(R.color.ink_black, null))
        } else {
            btn.text = "Follow"
            btn.setBackgroundColor(resources.getColor(R.color.garnet, null))
            btn.setTextColor(resources.getColor(R.color.white, null))
        }
    }
}

