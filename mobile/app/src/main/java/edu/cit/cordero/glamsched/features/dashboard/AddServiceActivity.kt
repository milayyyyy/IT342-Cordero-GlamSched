package edu.cit.cordero.glamsched.features.dashboard

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import edu.cit.cordero.glamsched.R
import edu.cit.cordero.glamsched.shared.ApiResponse
import edu.cit.cordero.glamsched.shared.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

class AddServiceActivity : AppCompatActivity() {
    private var artistId: Long = 0L
    private var serviceId: Long = 0L
    private var isEditMode: Boolean = false
    private var selectedCategory: String = ""
    private val selectedPhotos = mutableListOf<String>()

    private lateinit var tilName: TextInputLayout
    private lateinit var tilPrice: TextInputLayout
    private lateinit var tvPhotoCount: TextView
    private lateinit var llSelectedPhotos: LinearLayout

    private val pickPhotos = registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris: List<Uri> ->
        if (uris.isEmpty()) return@registerForActivityResult
        var added = 0
        for (uri in uris) {
            if (selectedPhotos.size >= 10) break
            val size = getFileSize(uri)
            if (size > 2L * 1024 * 1024) continue
            toDataUri(uri)?.let {
                selectedPhotos.add(it)
                added++
            }
        }
        if (added == 0) toast("No valid photos added (max 2MB each)")
        renderPhotoPreviews()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_service)

        artistId = intent.getLongExtra("ARTIST_ID", 0L)
        serviceId = intent.getLongExtra("SERVICE_ID", 0L)
        isEditMode = serviceId > 0L
        if (artistId == 0L) {
            finish()
            return
        }

        tilName = findViewById(R.id.tilServiceName)
        tilPrice = findViewById(R.id.tilServicePrice)
        tvPhotoCount = findViewById(R.id.tvPhotoCount)
        llSelectedPhotos = findViewById(R.id.llSelectedPhotos)

        findViewById<ImageButton>(R.id.btnAddServiceBack).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnCancelAddService).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnChooseServicePhotos).setOnClickListener { pickPhotos.launch("image/*") }
        findViewById<MaterialButton>(R.id.btnCreateService).setOnClickListener { submit() }

        setupCategoryChips()
        hydrateFromIntent()
        renderPhotoPreviews()
    }

    private fun hydrateFromIntent() {
        if (!isEditMode) return
        findViewById<TextView>(R.id.tvAddServiceTitle).text = "Edit Service"
        findViewById<MaterialButton>(R.id.btnCreateService).text = "Save Changes"
        findViewById<EditText>(R.id.etServiceName).setText(intent.getStringExtra("SERVICE_NAME") ?: "")
        findViewById<EditText>(R.id.etServiceDescription).setText(intent.getStringExtra("SERVICE_DESCRIPTION") ?: "")
        val price = intent.getDoubleExtra("SERVICE_PRICE", 0.0)
        if (price > 0.0) findViewById<EditText>(R.id.etServicePrice).setText(String.format("%.2f", price))
        selectedCategory = intent.getStringExtra("SERVICE_CATEGORY") ?: "Other"
        val photos = intent.getStringArrayListExtra("SERVICE_PHOTOS") ?: arrayListOf()
        selectedPhotos.clear()
        selectedPhotos.addAll(photos.filter { it.isNotBlank() }.take(10))
    }

    private fun setupCategoryChips() {
        val idMap = mapOf(
            "Hair" to R.id.chipCatHair,
            "Makeup" to R.id.chipCatMakeup,
            "Nails" to R.id.chipCatNails,
            "Skincare" to R.id.chipCatSkincare,
            "Lashes" to R.id.chipCatLashes,
            "Brows" to R.id.chipCatBrows,
            "Waxing" to R.id.chipCatWaxing,
            "Massage" to R.id.chipCatMassage,
            "Other" to R.id.chipCatOther
        )
        idMap.forEach { (cat, id) ->
            findViewById<TextView>(id).setOnClickListener {
                selectedCategory = cat
                refreshCategoryChips(idMap)
            }
        }
        if (selectedCategory.isBlank()) selectedCategory = "Other"
        refreshCategoryChips(idMap)
    }

    private fun refreshCategoryChips(idMap: Map<String, Int>) {
        idMap.forEach { (cat, id) ->
            findViewById<TextView>(id).apply {
                if (cat == selectedCategory) {
                    setBackgroundResource(R.drawable.bg_chip_pill_selected)
                    setTextColor(resources.getColor(R.color.white, null))
                } else {
                    setBackgroundResource(R.drawable.bg_chip_pill_unselected)
                    setTextColor(resources.getColor(R.color.ink_black, null))
                }
            }
        }
    }

    private fun submit() {
        val name = findViewById<EditText>(R.id.etServiceName).text.toString().trim()
        val desc = findViewById<EditText>(R.id.etServiceDescription).text.toString().trim()
        val priceText = findViewById<EditText>(R.id.etServicePrice).text.toString().trim()

        tilName.error = null
        tilPrice.error = null

        if (name.isBlank()) {
            tilName.error = "Service name is required"
            return
        }
        if (selectedCategory.isBlank()) {
            toast("Please select a category")
            return
        }
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0.0) {
            tilPrice.error = "Please enter a valid price"
            return
        }

        val body = mapOf(
            "name" to name,
            "description" to desc,
            "price" to price,
            "category" to selectedCategory,
            "duration" to 60,
            "photos" to selectedPhotos
        )

        findViewById<MaterialButton>(R.id.btnCreateService).isEnabled = false
        val callback = object : Callback<ApiResponse<ServiceDto>> {
            override fun onResponse(call: Call<ApiResponse<ServiceDto>>, response: Response<ApiResponse<ServiceDto>>) {
                if (response.isSuccessful && (response.body()?.success == true || response.body()?.data != null)) {
                    if (isEditMode) {
                        updateServicePhotosThenFinish()
                    } else {
                        findViewById<MaterialButton>(R.id.btnCreateService).isEnabled = true
                        toast("Service created")
                        setResult(RESULT_OK)
                        finish()
                    }
                } else {
                    findViewById<MaterialButton>(R.id.btnCreateService).isEnabled = true
                    toast(response.body()?.error?.message ?: "Could not create service")
                }
            }

            override fun onFailure(call: Call<ApiResponse<ServiceDto>>, t: Throwable) {
                findViewById<MaterialButton>(R.id.btnCreateService).isEnabled = true
                toast(if (isEditMode) "Failed to update service" else "Failed to create service")
            }
        }
        if (isEditMode) {
            RetrofitClient.glamApi.updateService(serviceId, body).enqueue(callback)
        } else {
            RetrofitClient.glamApi.createService(artistId, body).enqueue(callback)
        }
    }

    private fun updateServicePhotosThenFinish() {
        RetrofitClient.glamApi.updateServicePhotos(serviceId, mapOf("photos" to selectedPhotos))
            .enqueue(object : Callback<ApiResponse<ServiceDto>> {
                override fun onResponse(call: Call<ApiResponse<ServiceDto>>, response: Response<ApiResponse<ServiceDto>>) {
                    findViewById<MaterialButton>(R.id.btnCreateService).isEnabled = true
                    toast("Service updated")
                    setResult(RESULT_OK)
                    finish()
                }

                override fun onFailure(call: Call<ApiResponse<ServiceDto>>, t: Throwable) {
                    findViewById<MaterialButton>(R.id.btnCreateService).isEnabled = true
                    toast("Service updated, but photos were not saved")
                    setResult(RESULT_OK)
                    finish()
                }
            })
    }

    private fun getFileSize(uri: Uri): Long {
        contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.SIZE)
            if (idx >= 0 && c.moveToFirst()) return c.getLong(idx)
        }
        return Long.MAX_VALUE
    }

    private fun toDataUri(uri: Uri): String? {
        return runCatching {
            val input = contentResolver.openInputStream(uri) ?: return null
            val bmp = BitmapFactory.decodeStream(input) ?: return null
            input.close()
            val baos = ByteArrayOutputStream()
            bmp.compress(Bitmap.CompressFormat.JPEG, 80, baos)
            val b64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)
            "data:image/jpeg;base64,$b64"
        }.getOrNull()
    }

    private fun renderPhotoPreviews() {
        tvPhotoCount.text = "SERVICE PHOTOS (${selectedPhotos.size}/10)"
        llSelectedPhotos.removeAllViews()
        selectedPhotos.forEachIndexed { index, data ->
            val iv = ImageView(this).apply {
                layoutParams = LinearLayout.LayoutParams(
                    (72 * resources.displayMetrics.density).toInt(),
                    (72 * resources.displayMetrics.density).toInt()
                ).apply { rightMargin = (8 * resources.displayMetrics.density).toInt() }
                scaleType = ImageView.ScaleType.CENTER_CROP
                setOnClickListener {
                    selectedPhotos.removeAt(index)
                    renderPhotoPreviews()
                }
            }
            com.bumptech.glide.Glide.with(this).load(data).into(iv)
            llSelectedPhotos.addView(iv)
        }
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
