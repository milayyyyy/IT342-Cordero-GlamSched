package edu.cit.cordero.glamsched.features.user

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.tabs.TabLayout
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.features.booking.ReviewDto
import edu.cit.cordero.glamsched.features.booking.adapter.ReviewAdapter
import edu.cit.cordero.glamsched.features.dashboard.UserDto
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import edu.cit.cordero.glamsched.shared.SessionManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class UserProfileActivity : AppCompatActivity() {

    private var userId: Long = 0L
    private var currentUser: UserDto? = null

    private lateinit var ivCover: ImageView
    private lateinit var ivAvatar: ImageView
    private lateinit var tvAvatarInitial: TextView
    private lateinit var reviewsAdapter: ReviewAdapter

    private enum class PendingTarget { PROFILE, COVER }
    private var pendingTarget: PendingTarget? = null

    private val pickImage: ActivityResultLauncher<String> =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri == null) return@registerForActivityResult
            val target = pendingTarget ?: return@registerForActivityResult
            handleImagePicked(uri, target)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        userId = SessionManager.getUserId(this)
        if (userId == 0L) {
            Toast.makeText(this, "Please log in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        ivCover = findViewById(R.id.ivCover)
        ivAvatar = findViewById(R.id.ivAvatar)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)

        findViewById<ImageButton>(R.id.btnUserProfileBack).setOnClickListener { finish() }

        findViewById<View>(R.id.btnEditCover).setOnClickListener {
            pendingTarget = PendingTarget.COVER
            pickImage.launch("image/*")
        }
        findViewById<View>(R.id.btnEditAvatar).setOnClickListener {
            pendingTarget = PendingTarget.PROFILE
            pickImage.launch("image/*")
        }

        findViewById<MaterialButton>(R.id.btnEditDetails).setOnClickListener { showEditDetailsDialog() }
        findViewById<TextView>(R.id.btnEditBio).setOnClickListener { showEditBioDialog() }

        reviewsAdapter = ReviewAdapter(emptyList())
        findViewById<RecyclerView>(R.id.rvUserReviews).apply {
            layoutManager = LinearLayoutManager(this@UserProfileActivity)
            adapter = reviewsAdapter
        }

        setupTabs()
        loadUser()
        loadStats()
        loadReviews()
    }

    private fun setupTabs() {
        val tabAbout = findViewById<View>(R.id.tabAbout)
        val tabDetails = findViewById<View>(R.id.tabDetails)
        val tabReviews = findViewById<View>(R.id.tabReviews)
        findViewById<TabLayout>(R.id.tabLayoutProfile).addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    tabAbout.visibility = if (tab.position == 0) View.VISIBLE else View.GONE
                    tabDetails.visibility = if (tab.position == 1) View.VISIBLE else View.GONE
                    tabReviews.visibility = if (tab.position == 2) View.VISIBLE else View.GONE
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            }
        )
    }

    private fun loadReviews() {
        val role = SessionManager.getUserRole(this)
        val tvEmpty = findViewById<TextView>(R.id.tvUserReviewsEmpty)
        if (!role.equals("ARTIST", true)) {
            reviewsAdapter.updateItems(emptyList())
            tvEmpty.text = "Reviews are shown for artist accounts."
            tvEmpty.visibility = View.VISIBLE
            return
        }
        RetrofitClient.glamApi.getReviews(userId).enqueue(object : Callback<ApiResponse<List<ReviewDto>>> {
            override fun onResponse(call: Call<ApiResponse<List<ReviewDto>>>, response: Response<ApiResponse<List<ReviewDto>>>) {
                val reviews = response.body()?.data ?: emptyList()
                reviewsAdapter.updateItems(reviews)
                tvEmpty.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
                if (reviews.isEmpty()) tvEmpty.text = "No reviews yet."
            }
            override fun onFailure(call: Call<ApiResponse<List<ReviewDto>>>, t: Throwable) {
                reviewsAdapter.updateItems(emptyList())
                tvEmpty.text = "Could not load reviews."
                tvEmpty.visibility = View.VISIBLE
            }
        })
    }

    private fun loadUser() {
        RetrofitClient.glamApi.getUserById(userId).enqueue(object : Callback<ApiResponse<UserDto>> {
            override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                response.body()?.data?.let { bind(it) }
            }
            override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun loadStats() {
        val role = SessionManager.getUserRole(this)
        // Followers / following from /users/{id}/stats
        RetrofitClient.glamApi.getUserStats(userId).enqueue(object : Callback<ApiResponse<Map<String, Any>>> {
            override fun onResponse(call: Call<ApiResponse<Map<String, Any>>>, response: Response<ApiResponse<Map<String, Any>>>) {
                val data = response.body()?.data ?: return
                val followers = (data["followers"] as? Double)?.toLong() ?: 0L
                val following = (data["following"] as? Double)?.toLong() ?: 0L
                findViewById<TextView>(R.id.tvProfileFollowers).text =
                    "$followers ${if (followers == 1L) "follower" else "followers"} · $following following"
                findViewById<TextView>(R.id.tvStat1).text = followers.toString()
                findViewById<TextView>(R.id.tvStat2).text = following.toString()
            }
            override fun onFailure(call: Call<ApiResponse<Map<String, Any>>>, t: Throwable) {}
        })

        // Bookings count from appointments list
        RetrofitClient.glamApi.getAppointments(userId).enqueue(object : Callback<ApiResponse<List<edu.cit.cordero.glamsched.features.dashboard.AppointmentDto>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<edu.cit.cordero.glamsched.features.dashboard.AppointmentDto>>>,
                response: Response<ApiResponse<List<edu.cit.cordero.glamsched.features.dashboard.AppointmentDto>>>
            ) {
                val count = response.body()?.data?.size ?: 0
                findViewById<TextView>(R.id.tvStat3).text = count.toString()
                findViewById<TextView>(R.id.tvStat3Label).text = if (role.equals("ARTIST", true)) "Services" else "Bookings"
            }
            override fun onFailure(
                call: Call<ApiResponse<List<edu.cit.cordero.glamsched.features.dashboard.AppointmentDto>>>,
                t: Throwable
            ) {}
        })
    }

    private fun bind(user: UserDto) {
        currentUser = user
        val name = user.name ?: "User"
        findViewById<TextView>(R.id.tvProfileName).text = name
        findViewById<TextView>(R.id.tvProfileRole).text =
            "${(user.role ?: "Client").lowercase().replaceFirstChar { it.uppercase() }} · GlamSched"
        findViewById<TextView>(R.id.tvDetailEmail).text = user.email ?: "—"
        findViewById<TextView>(R.id.tvDetailPhone).text =
            user.phone?.takeIf { it.isNotBlank() } ?: "—"
        findViewById<TextView>(R.id.tvDetailAddress).text =
            user.address?.takeIf { it.isNotBlank() } ?: "—"
        findViewById<TextView>(R.id.tvBio).text =
            user.bio?.takeIf { it.isNotBlank() } ?: "Add a short bio to introduce yourself."

        tvAvatarInitial.text = name.first().uppercase()
        if (!user.profileImage.isNullOrBlank()) {
            ivAvatar.visibility = View.VISIBLE
            tvAvatarInitial.visibility = View.GONE
            Glide.with(this).load(user.profileImage).circleCrop().into(ivAvatar)
        } else {
            ivAvatar.visibility = View.GONE
            tvAvatarInitial.visibility = View.VISIBLE
        }

        if (!user.coverImage.isNullOrBlank()) {
            Glide.with(this).load(user.coverImage).centerCrop().into(ivCover)
        }
    }

    private fun handleImagePicked(uri: Uri, target: PendingTarget) {
        try {
            val input = contentResolver.openInputStream(uri) ?: return
            val original = BitmapFactory.decodeStream(input)
            input.close()
            if (original == null) {
                Toast.makeText(this, "Could not read image", Toast.LENGTH_SHORT).show()
                return
            }
            // Resize to keep base64 payload reasonable
            val maxDim = if (target == PendingTarget.COVER) 1280 else 720
            val scaled = scaleBitmap(original, maxDim)
            val baos = ByteArrayOutputStream()
            scaled.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            val dataUri = "data:image/jpeg;base64,$base64"

            if (target == PendingTarget.PROFILE) uploadProfileImage(dataUri)
            else uploadCoverImage(dataUri)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scaleBitmap(src: Bitmap, maxDim: Int): Bitmap {
        val w = src.width
        val h = src.height
        if (w <= maxDim && h <= maxDim) return src
        val ratio = w.toFloat() / h.toFloat()
        val (nw, nh) = if (w > h) maxDim to (maxDim / ratio).toInt()
            else (maxDim * ratio).toInt() to maxDim
        return Bitmap.createScaledBitmap(src, nw, nh, true)
    }

    private fun uploadProfileImage(dataUri: String) {
        Toast.makeText(this, "Uploading photo...", Toast.LENGTH_SHORT).show()
        RetrofitClient.glamApi.updateUserPhoto(userId, mapOf("profileImage" to dataUri))
            .enqueue(object : Callback<ApiResponse<UserDto>> {
                override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                    if (response.isSuccessful) {
                        Glide.with(this@UserProfileActivity).load(dataUri).circleCrop().into(ivAvatar)
                        ivAvatar.visibility = View.VISIBLE
                        tvAvatarInitial.visibility = View.GONE
                        Toast.makeText(this@UserProfileActivity, "Profile photo updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                    Toast.makeText(this@UserProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun uploadCoverImage(dataUri: String) {
        Toast.makeText(this, "Uploading cover...", Toast.LENGTH_SHORT).show()
        RetrofitClient.glamApi.updateUserCover(userId, mapOf("coverImage" to dataUri))
            .enqueue(object : Callback<ApiResponse<UserDto>> {
                override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                    if (response.isSuccessful) {
                        Glide.with(this@UserProfileActivity).load(dataUri).centerCrop().into(ivCover)
                        Toast.makeText(this@UserProfileActivity, "Cover photo updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                    Toast.makeText(this@UserProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun showEditBioDialog() {
        val ctx = this
        val container = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }
        val et = EditText(ctx).apply {
            hint = "Tell people about yourself..."
            setText(currentUser?.bio ?: "")
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE
            minLines = 3
            maxLines = 6
        }
        container.addView(et)

        AlertDialog.Builder(ctx)
            .setTitle("Edit Bio")
            .setView(container)
            .setPositiveButton("Save") { _, _ ->
                saveProfile(mapOf("bio" to et.text.toString()))
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showEditDetailsDialog() {
        val container = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val tilName = container.findViewById<TextInputLayout>(R.id.tilEditName)
        val etName = container.findViewById<EditText>(R.id.etEditName)
        val etPhone = container.findViewById<EditText>(R.id.etEditPhone)
        val etAddress = container.findViewById<EditText>(R.id.etEditAddress)
        etName.setText(currentUser?.name ?: "")
        etPhone.setText(currentUser?.phone ?: "")
        etAddress.setText(currentUser?.address ?: "")

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(container, 48, 12, 48, 0)
            .setPositiveButton("Save", null)
            .setNegativeButton("Cancel", null)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                        tilName.error = null
                val name = etName.text.toString().trim()
                if (name.isBlank()) {
                            tilName.error = "Name cannot be empty"
                            return@setOnClickListener
                }
                saveProfile(
                    mapOf(
                        "name" to name,
                        "phone" to etPhone.text.toString().trim(),
                        "address" to etAddress.text.toString().trim()
                    )
                )
                        dialog.dismiss()
                    }
                }
                dialog.show()
            }
    }

    private fun saveProfile(updates: Map<String, Any?>) {
        RetrofitClient.glamApi.updateUserProfile(userId, updates)
            .enqueue(object : Callback<ApiResponse<UserDto>> {
                override fun onResponse(call: Call<ApiResponse<UserDto>>, response: Response<ApiResponse<UserDto>>) {
                    if (response.isSuccessful) {
                        val data = response.body()?.data
                        if (data != null) {
                            bind(data)
                            (updates["name"] as? String)?.takeIf { it.isNotBlank() }?.let {
                                SessionManager.saveSession(
                                    context = this@UserProfileActivity,
                                    userId = userId,
                                    userName = it,
                                    userEmail = SessionManager.getUserEmail(this@UserProfileActivity),
                                    userRole = SessionManager.getUserRole(this@UserProfileActivity)
                                )
                            }
                        }
                        Toast.makeText(this@UserProfileActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@UserProfileActivity, "Update failed", Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ApiResponse<UserDto>>, t: Throwable) {
                    Toast.makeText(this@UserProfileActivity, "Network error", Toast.LENGTH_SHORT).show()
                }
            })
    }
}
